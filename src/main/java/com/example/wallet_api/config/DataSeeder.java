package com.example.wallet_api.config;

import com.example.wallet_api.dto.request.CreateWalletRequest;
import com.example.wallet_api.dto.request.TopUpRequest;
import com.example.wallet_api.dto.request.TransferRequest;
import com.example.wallet_api.dto.response.WalletResponse;
import com.example.wallet_api.repository.WalletRepository;
import com.example.wallet_api.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @Override
    public void run(String... args) {
        // Hanya isi data awal jika tabel dompet masih kosong
        if (walletRepository.count() == 0) {
            log.info("Menjalankan DataSeeder: Membuat akun dompet contoh...");

            // 1. Buat 2 dompet contoh (Budi = ID #1, Siti = ID #2)
            WalletResponse budi = walletService.createWallet(new CreateWalletRequest("budi-01"));
            WalletResponse siti = walletService.createWallet(new CreateWalletRequest("siti-02"));

            // 2. Isi saldo awal (Top-Up)
            walletService.topUp(budi.getId(), new TopUpRequest(new BigDecimal("500000")));
            walletService.topUp(siti.getId(), new TopUpRequest(new BigDecimal("150000")));

            // 3. Simulasikan 1 transaksi transfer dari Budi ke Siti (Rp 50.000)
            walletService.transfer(new TransferRequest(
                    budi.getId(),
                    siti.getId(),
                    new BigDecimal("50000"),
                    "Bayar makan siang"
            ));

            log.info("DataSeeder selesai! Dompet #1 (budi-01) dan #2 (siti-02) siap digunakan.");
        }
    }
}