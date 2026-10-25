# ⚡ SimplePay — Core Wallet & Transaction Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20In--Memory-blue.svg)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**SimplePay** adalah sistem dompet digital (Core Wallet & Transaction API) dengan arsitektur berlapis (*Clean Layered Architecture*) yang berfokus pada **integritas data finansial**, **keamanan konkurensi tinggi**, dan **pencegahan race condition**.

Proyek ini dilengkapi dengan antarmuka web interaktif bergaya Neobank Dark Mode yang disajikan langsung oleh backend Spring Boot.

---

## 🌟 Fitur Utama & Keunggulan Arsitektur

1. **Anti Double-Spending (Pessimistic Locking)**
   - Menggunakan `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) pada query dompet untuk mencegah pemotongan ganda saat banyak request transfer masuk di milidetik yang sama.
2. **Anti Deadlock (Lock Ordering Algorithm)**
   - Mengurutkan ID akun yang dikunci (`Math.min` dan `Math.max`) saat transfer antar akun untuk mencegah circular lock deadlock.
3. **Presisi Finansial Tanpa Celah**
   - Menggunakan `BigDecimal` dengan skala 4 desimal untuk meniadakan bug pembulatan biner (*floating point errors*).
4. **Audit Mutasi Buku Kas (Double-Entry Ledger Pattern)**
   - Setiap mutasi saldo otomatis mencatat riwayat transaksi dengan tipe `DEBIT` / `CREDIT` dan status transaksi.
5. **Enkapsulasi Kontrak & DTO**
   - Entity database internal terisolasi dari publik menggunakan DTO Request dan Response dengan validasi Bean Validation (`@NotBlank`, `@DecimalMin`, `@NotNull`).
6. **Centralized Error Handling**
   - Menggunakan `@RestControllerAdvice` dengan format respons JSON seragam (`ApiResponse<T>`).
7. **Interactive Neobank Dashboard**
   - Web frontend modern (Glassmorphism, Dark Mode, visual kartu dompet, toast notifikasi) di `src/main/resources/static/`.

---

## 🏗️ Peta Arsitektur (Layered Architecture)

```
[ Client: Browser / Hoppscotch / Postman ]
                   │
                   ▼
┌──────────────────────────────────────────────┐
│  Web Layer: WalletController                 │
│  (Routing HTTP, DTO Mapping, @Valid)         │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│  Business Layer: WalletService               │
│  (@Transactional, Lock Ordering, Logic ACID) │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│  Data Access Layer: JpaRepository            │
│  (WalletRepository with Pessimistic Lock)    │
│  (TransactionRepository with Ledger Queries) │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│  Domain Entities & Database: H2 Engine       │
│  (wallets, transactions)                     │
└──────────────────────────────────────────────┘
```

---

## 🚀 Cara Menjalankan Proyek

### Prasyarat
- Java JDK 17 atau 21
- Git

### Langkah Menjalankan:
1. Clone repositori ini:
   ```bash
   git clone https://github.com/<username-kamu>/wallet-api.git
   cd wallet-api
   ```
2. Jalankan aplikasi menggunakan Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Buka dashboard visual di browser:
   👉 **`http://localhost:8080/index.html`**

---

## 📖 Dokumentasi Endpoint API

Base URL: `http://localhost:8080/api/wallets`

| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `POST` | `/api/wallets` | Mendaftarkan akun dompet baru |
| `GET` | `/api/wallets/{id}` | Mengambil detail dan saldo terkini dompet |
| `POST` | `/api/wallets/{id}/topup` | Mengisi saldo dompet (Top-Up) |
| `POST` | `/api/wallets/transfer` | Mentransfer saldo antar dompet (ACID & Lock) |
| `GET` | `/api/wallets/{id}/transactions` | Mengambil seluruh riwayat mutasi dompet |

### Format Response Seragam:
```json
{
  "success": true,
  "message": "Transfer saldo berhasil!",
  "data": { ... }
}
```

---

## 🧪 Koleksi API Siap Pakai

Tersedia file koleksi siap import untuk testing:
- **Postman / Insomnia:** `SimplePay_API_Collection.json`
- **Hoppscotch:** `SimplePay_Hoppscotch.json`

---

## 🛠️ Tech Stack
- **Backend Framework:** Spring Boot 4
- **Language:** Java 21
- **ORM / Persistence:** Spring Data JPA / Hibernate
- **Database:** H2 In-Memory Database
- **Boilerplate Reducer:** Project Lombok
- **Frontend UI:** HTML5 Semantic, Modern Vanilla CSS, Vanilla JavaScript (Fetch API)
