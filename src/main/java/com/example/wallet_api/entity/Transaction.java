package com.example.wallet_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi dompet pengirim (bisa null jika Top-Up)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    // Relasi dompet penerima (wajib ada)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_wallet_id", nullable = false)
    private Wallet targetWallet;

    // TODO: Kolom amount (BigDecimal, nullable = false, precision = 19, scale = 4)
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // TODO: Pasang anotasi @Enumerated(EnumType.STRING) di atas type
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    // TODO: Pasang anotasi @Enumerated(EnumType.STRING) di atas status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    // Kolom catatan / deskripsi (opsional)
    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // TODO: Buat method @PrePersist untuk mengisi createdAt dengan waktu saat ini (LocalDateTime.now())
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
