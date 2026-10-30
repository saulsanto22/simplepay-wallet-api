package com.example.wallet_api.repository;

import com.example.wallet_api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE (t.sourceWallet.id= :walletId AND t.type = TransactionType.DEBIT) OR (t.targetWallet.id = :walletId AND t.type=TransactionType.CREDIT) ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);
}
