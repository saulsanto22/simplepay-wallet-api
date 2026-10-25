package com.example.wallet_api.dto.response;

import com.example.wallet_api.entity.Wallet;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {

    private Long id;
    private String userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Method pembantu (Mapper) mengubah Entity Wallet menjadi WalletResponse
    public static WalletResponse fromEntity(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
