package com.example.wallet_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    // Validasi: userId wajib diisi dan tidak boleh cuma spasi
    @NotBlank(message = "User ID wajib diisi")
    private String userId;
}
