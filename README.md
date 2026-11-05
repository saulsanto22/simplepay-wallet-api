# ⚡ SimplePay — Core Wallet & Transaction Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20In--Memory-blue.svg)](https://www.h2database.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](Dockerfile)
[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF.svg)](.github/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**SimplePay** adalah sistem dompet digital (*Core Wallet & Transaction API*) dengan arsitektur berlapis (*Clean Layered Architecture*) yang berfokus pada **integritas data finansial**, **keamanan konkurensi tinggi**, **idempotensi transaksi**, dan **pencegahan race condition**.

Proyek ini dilengkapi dengan antarmuka web interaktif bergaya *Neobank Dark Mode* yang disajikan langsung oleh backend Spring Boot, pengujian konkurensi otomatis (JUnit 5), serta konfigurasi *containerization* (Docker) dan *CI Pipeline* (GitHub Actions).

---

## 🌟 Fitur Utama & Keunggulan Arsitektur

1. **Anti Double-Spending (Pessimistic Locking)**
   - Menggunakan `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) pada query dompet untuk mencegah pemotongan ganda saat banyak request transfer masuk di milidetik yang sama.
2. **Anti Deadlock (Lock Ordering Algorithm)**
   - Mengurutkan ID akun yang dikunci (`Math.min` dan `Math.max`) saat transfer antar akun untuk mencegah *circular lock deadlock*.
3. **Idempotency Key Protection (Anti Double-Request / Retry Safe)**
   - Mendukung `idempotencyKey` (`UUID v4`) dari sisi Frontend hingga Database untuk menjamin transaksi transfer tidak terpotong dua kali akibat klik ganda atau *network retry* (`409 Conflict`).
4. **True Double-Entry Ledger Pattern (Pembukuan Berpasangan)**
   - Setiap transaksi transfer antar dompet otomatis mencatat 2 jurnal berpasangan secara atomik: `DEBIT` (saldo keluar pada pengirim) dan `CREDIT` (saldo masuk pada penerima).
5. **Presisi Finansial Tanpa Celah**
   - Menggunakan `BigDecimal` (`precision = 19, scale = 4`) untuk meniadakan bug pembulatan biner (*floating point errors*).
6. **Enkapsulasi Kontrak, DTO & Centralized Error Handling**
   - Entity database terisolasi menggunakan DTO Request/Response dengan Bean Validation (`@NotBlank`, `@DecimalMin`, `@NotNull`) dan `@RestControllerAdvice` (`ApiResponse<T>`).
7. **Automated Concurrency & Integration Testing**
   - Dilengkapi pengujian otomatis multi-threading (`ExecutorService` & `CountDownLatch`) untuk memverifikasi ketahanan *Pessimistic Locking* dan *Idempotency Key*.
8. **Auto Data Seeder & Interactive Neobank Dashboard**
   - `DataSeeder` (`CommandLineRunner`) otomatis menyiapkan akun dompet contoh beserta mutasi awal saat aplikasi dijalankan, terhubung langsung dengan dashboard Frontend di `src/main/resources/static/`.

---

## 🏗️ Peta Arsitektur (Layered Architecture)

```
[ Client: Browser Dashboard / Hoppscotch / Postman ]
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Web Layer: WalletController                       │
│  (Routing HTTP, DTO Mapping, @Valid)               │
└────────────────────────┬───────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Business Layer: WalletService                     │
│  (@Transactional, Lock Ordering, Idempotency,      │
│   Double-Entry Ledger DEBIT/CREDIT)                │
└────────────────────────┬───────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Data Access Layer: JpaRepository                  │
│  (WalletRepository with Pessimistic Write Lock)    │
│  (TransactionRepository with Ledger & Key Queries) │
└────────────────────────┬───────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────┐
│  Domain Entities & Database: H2 Engine             │
│  (wallets, transactions)                           │
└────────────────────────────────────────────────────┘
```

---

## 🚀 Cara Menjalankan Proyek

### Opsi 1: Menjalankan Secara Lokal (Maven Wrapper)
**Prasyarat:** Java JDK 21 & Git

1. Clone repositori ini:
   ```bash
   git clone https://github.com/<username-kamu>/wallet-api.git
   cd wallet-api
   ```
2. Jalankan aplikasi:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Buka dashboard visual di browser:
   👉 **`http://localhost:8080/index.html`**

### Opsi 2: Menjalankan Menggunakan Docker Compose
```bash
docker compose up --build
```

### Menjalankan Pengujian Otomatis (Unit & Concurrency Test)
```bash
./mvnw test
```

---

## 📖 Dokumentasi Endpoint API

Base URL: `http://localhost:8080/api/wallets`

| Method | Endpoint | Deskripsi |
| :--- | :--- | :--- |
| `GET` | `/api/wallets` | Mengambil seluruh daftar akun dompet |
| `POST` | `/api/wallets` | Mendaftarkan akun dompet baru (`409 Conflict` jika duplikat) |
| `GET` | `/api/wallets/{id}` | Mengambil detail dan saldo terkini dompet |
| `POST` | `/api/wallets/{id}/topup` | Mengisi saldo dompet (Top-Up) |
| `POST` | `/api/wallets/transfer` | Mentransfer saldo antar dompet (ACID, Lock, & Idempotency Key) |
| `GET` | `/api/wallets/{id}/transactions` | Mengambil riwayat mutasi dompet (`DEBIT` / `CREDIT`) |

### Contoh Payload Transfer (`POST /api/wallets/transfer`):
```json
{
  "sourceWalletId": 1,
  "targetWalletId": 2,
  "amount": 50000,
  "description": "Bayar makan siang",
  "idempotencyKey": "36b8f84d-df4e-4d49-b662-bcde71a8764f"
}
```

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
- **Testing:** JUnit 5, Spring Boot Test, Java Concurrency (`ExecutorService`, `CountDownLatch`)
- **DevOps & CI/CD:** Docker (Multi-Stage Build), Docker Compose, GitHub Actions
- **Frontend UI:** HTML5 Semantic, Modern Vanilla CSS, Vanilla JavaScript (Fetch API, Async/Await)
