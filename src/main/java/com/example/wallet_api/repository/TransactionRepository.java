package com.example.wallet_api.repository;

import com.example.wallet_api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// TODO: Buat interface TransactionRepository yang meng-extends JpaRepository untuk entity Transaction dengan ID tipe Long
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Mengambil semua transaksi masuk & keluar milik sebuah dompet, diurutkan dari yang terbaru
    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);
}
