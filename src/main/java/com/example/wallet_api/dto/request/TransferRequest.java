package com.example.wallet_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Source wallet ID wajib diisi")
    private Long sourceWalletId;

    @NotNull(message = "Target wallet ID wajib diisi")
    private Long targetWalletId;

    @NotNull(message = "Amount wajib diisi")
    @DecimalMin(value = "1000.00", message = "Minimal transfer adalah Rp 1.000")
    private BigDecimal amount;

    private String description;
}
