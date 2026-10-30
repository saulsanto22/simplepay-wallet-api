package com.example.wallet_api.service;

import com.example.wallet_api.dto.request.CreateWalletRequest;
import com.example.wallet_api.dto.response.WalletResponse;
import com.example.wallet_api.entity.Wallet;
import com.example.wallet_api.exception.InvalidTransferException;
import com.example.wallet_api.exception.WalletNotFoundException;
import com.example.wallet_api.repository.TransactionRepository;
import com.example.wallet_api.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.wallet_api.dto.request.TopUpRequest;
import com.example.wallet_api.dto.response.TransactionResponse;
import com.example.wallet_api.entity.Transaction;
import com.example.wallet_api.entity.TransactionStatus;
import com.example.wallet_api.entity.TransactionType;
import java.util.List;
import com.example.wallet_api.dto.request.TransferRequest;
import com.example.wallet_api.exception.DuplicateResourceException;
import com.example.wallet_api.exception.InsufficientBalanceException;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // 1. Fitur Buka Akun Dompet Baru
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        // Cek apakah user_id sudah memiliki dompet sebelumnya
        if (walletRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new DuplicateResourceException("User dengan ID " + request.getUserId() + " sudah memiliki dompet!");
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(request.getUserId());
        wallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.save(wallet);
        return WalletResponse.fromEntity(savedWallet);
    }

    // 2. Fitur Cek Saldo & Detail Dompet
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Dompet dengan ID " + id + " tidak ditemukan!"));

        return WalletResponse.fromEntity(wallet);
    }

    // 3. Fitur Top-Up Saldo Dompet
    @Transactional
    public WalletResponse topUp(Long walletId, TopUpRequest request) {
        // Ambil dompet dengan lock agar saldo aman
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Dompet dengan ID " + walletId + " tidak ditemukan!"));

        // Tambah saldo: wallet.balance = wallet.balance + amount
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet updatedWallet = walletRepository.save(wallet);

        // Catat buku kas transaksi (CREDIT)
        Transaction tx = new Transaction();
        tx.setTargetWallet(wallet);
        tx.setSourceWallet(null);
        tx.setAmount(request.getAmount());
        tx.setType(TransactionType.CREDIT);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setDescription("Top-up saldo");
        transactionRepository.save(tx);

        return WalletResponse.fromEntity(updatedWallet);
    }

    // 4. Fitur Riwayat Mutasi Dompet (Untuk Dashboard Frontend)
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException("Dompet dengan ID " + walletId + " tidak ditemukan!");
        }

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }

    // 5. Fitur Transfer Saldo Antar Dompet (ACID & Anti-Deadlock)
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        // 1. Validasi tidak boleh transfer ke diri sendiri
        if (request.getSourceWalletId().equals(request.getTargetWalletId())) {
            throw new InvalidTransferException("Tidak dapat melakukan transfer ke dompet sendiri!");
        }

        // 2. Lock Ordering untuk mencegah Deadlock
        Long firstId = Math.min(request.getSourceWalletId(), request.getTargetWalletId());
        Long secondId = Math.max(request.getSourceWalletId(), request.getTargetWalletId());

        Wallet firstWallet = walletRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new WalletNotFoundException("Dompet dengan ID " + firstId + " tidak ditemukan!"));
        Wallet secondWallet = walletRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new WalletNotFoundException("Dompet dengan ID " + secondId + " tidak ditemukan!"));

        Wallet sourceWallet = request.getSourceWalletId().equals(firstWallet.getId()) ? firstWallet : secondWallet;
        Wallet targetWallet = request.getTargetWalletId().equals(firstWallet.getId()) ? firstWallet : secondWallet;

        // 3. Validasi kecukupan saldo
        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Saldo dompet pengirim tidak mencukupi! Saldo saat ini: Rp " + sourceWallet.getBalance());
        }

        // 4. Mutasi Saldo: Kurangi pengirim, tambah penerima
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.getAmount()));
        targetWallet.setBalance(targetWallet.getBalance().add(request.getAmount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);

        // // 5. Catat mutasi buku kas (Ledger)
        // Transaction tx = new Transaction();
        // tx.setSourceWallet(sourceWallet);
        // tx.setTargetWallet(targetWallet);
        // tx.setAmount(request.getAmount());
        // tx.setType(TransactionType.DEBIT);
        // tx.setStatus(TransactionStatus.SUCCESS);
        // tx.setDescription(request.getDescription() != null ? request.getDescription()
        // : "Transfer antar dompet");

        // Transaction savedTx = transactionRepository.save(tx);
        // return TransactionResponse.fromEntity(savedTx);

        
        // 5. Catat mutasi buku kas berpasangan (Double-Entry Ledger)
        String baseDesc = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription()
                : "Transfer antar dompet";

        // 5a. Jurnal DEBIT (Uang keluar untuk pengirim)
        Transaction debitTx = new Transaction();
        debitTx.setSourceWallet(sourceWallet);
        debitTx.setTargetWallet(targetWallet);
        debitTx.setAmount(request.getAmount());
        debitTx.setType(TransactionType.DEBIT);
        debitTx.setStatus(TransactionStatus.SUCCESS);
        debitTx.setDescription(baseDesc + " (Ke Dompet #" + targetWallet.getId() + ")");

        // 5b. Jurnal CREDIT (Uang masuk untuk penerima)
        Transaction creditTx = new Transaction();
        creditTx.setSourceWallet(sourceWallet);
        creditTx.setTargetWallet(targetWallet);
        creditTx.setAmount(request.getAmount());
        creditTx.setType(TransactionType.CREDIT);
        creditTx.setStatus(TransactionStatus.SUCCESS);
        creditTx.setDescription(baseDesc + " (Dari Dompet #" + sourceWallet.getId() + ")");

        Transaction savedDebitTx = transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

        return TransactionResponse.fromEntity(savedDebitTx);
    }

}
