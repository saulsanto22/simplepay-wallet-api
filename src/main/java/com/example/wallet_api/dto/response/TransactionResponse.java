package com.example.wallet_api.dto.response;

import com.example.wallet_api.entity.Transaction;
import com.example.wallet_api.entity.TransactionStatus;
import com.example.wallet_api.entity.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long sourceWalletId;
    private Long targetWalletId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;

    // Mapper dari Entity Transaction ke TransactionResponse
    public static TransactionResponse fromEntity(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .sourceWalletId(tx.getSourceWallet() != null ? tx.getSourceWallet().getId() : null)
                .targetWalletId(tx.getTargetWallet().getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
