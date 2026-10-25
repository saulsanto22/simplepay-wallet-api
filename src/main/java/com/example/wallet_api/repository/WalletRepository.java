package com.example.wallet_api.repository;

import com.example.wallet_api.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Derived Query: Spring otomatis membuat SELECT * FROM wallets WHERE user_id = ?
    Optional<Wallet> findByUserId(String userId);

    // Pessimistic Locking: Mengunci baris (SELECT ... FOR UPDATE) agar uang aman dari race condition
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);
}
