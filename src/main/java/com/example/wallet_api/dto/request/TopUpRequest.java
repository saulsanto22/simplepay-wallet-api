package com.example.wallet_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {

    // Validasi: nominal tidak boleh null dan minimal Rp 1.000
    @NotNull(message = "Amount wajib diisi")
    @DecimalMin(value = "1000.00", message = "Minimal top-up adalah Rp 1.000")
    private BigDecimal amount;
}
