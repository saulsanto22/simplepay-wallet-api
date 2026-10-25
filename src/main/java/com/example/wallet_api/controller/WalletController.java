package com.example.wallet_api.controller;

import com.example.wallet_api.dto.request.CreateWalletRequest;
import com.example.wallet_api.dto.request.TopUpRequest;
import com.example.wallet_api.dto.request.TransferRequest;
import com.example.wallet_api.dto.response.ApiResponse;
import com.example.wallet_api.dto.response.TransactionResponse;
import com.example.wallet_api.dto.response.WalletResponse;
import com.example.wallet_api.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Menghindari CORS error saat dipanggil dari Frontend nanti
public class WalletController {

    private final WalletService walletService;

    // 1. POST /api/wallets (Buka Akun Dompet)
    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dompet berhasil dibuat!", response));
    }

    // 2. GET /api/wallets/{id} (Cek Saldo)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(@PathVariable Long id) {
        WalletResponse response = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.success("Detail dompet berhasil diambil", response));
    }

    // 3. POST /api/wallets/{id}/topup (Isi Saldo)
    @PostMapping("/{id}/topup")
    public ResponseEntity<ApiResponse<WalletResponse>> topUp(
            @PathVariable Long id,
            @Valid @RequestBody TopUpRequest request) {
        WalletResponse response = walletService.topUp(id, request);
        return ResponseEntity.ok(ApiResponse.success("Top-up saldo berhasil!", response));
    }

    // 4. POST /api/wallets/transfer (Transfer Uang)
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = walletService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer saldo berhasil!", response));
    }

    // 5. GET /api/wallets/{id}/transactions (Riwayat Mutasi)
    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@PathVariable Long id) {
        List<TransactionResponse> response = walletService.getTransactions(id);
        return ResponseEntity.ok(ApiResponse.success("Riwayat transaksi berhasil diambil", response));
    }
}
