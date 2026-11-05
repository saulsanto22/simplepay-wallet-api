package com.example.wallet_api;

import com.example.wallet_api.dto.request.CreateWalletRequest;
import com.example.wallet_api.dto.request.TopUpRequest;
import com.example.wallet_api.dto.request.TransferRequest;
import com.example.wallet_api.dto.response.TransactionResponse;
import com.example.wallet_api.dto.response.WalletResponse;
import com.example.wallet_api.entity.TransactionType;
import com.example.wallet_api.exception.DuplicateResourceException;
import com.example.wallet_api.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WalletApiApplicationTests {

	@Autowired
	private WalletService walletService;

	@Test
	void shouldRecordDoubleEntryLedgerAndPreventDuplicateIdempotencyKey() {
		// 1. Buat 2 dompet baru & isi saldo pengirim Rp 100.000
		WalletResponse sender = walletService.createWallet(new CreateWalletRequest("test-sender"));
		WalletResponse receiver = walletService.createWallet(new CreateWalletRequest("test-receiver"));
		walletService.topUp(sender.getId(), new TopUpRequest(new BigDecimal("100000")));

		// 2. Kirim transfer Rp 30.000 dengan tiket idempotencyKey "key-unik-001"
		TransferRequest req = new TransferRequest(
				sender.getId(),
				receiver.getId(),
				new BigDecimal("30000"),
				"Tes Transfer",
				"key-unik-001");
		walletService.transfer(req);

		// 3. Pastikan jika request yang SAMA dikirim ke-2 kalinya, ditolak
		// (DuplicateResourceException)
		assertThrows(DuplicateResourceException.class, () -> walletService.transfer(req));

		// 4. Pastikan saldo pengirim hanya terpotong 1 kali (sisa Rp 70.000) & penerima
		// Rp 30.000
		WalletResponse updatedSender = walletService.getWalletById(sender.getId());
		WalletResponse updatedReceiver = walletService.getWalletById(receiver.getId());
		assertEquals(0, new BigDecimal("70000").compareTo(updatedSender.getBalance()));
		assertEquals(0, new BigDecimal("30000").compareTo(updatedReceiver.getBalance()));

		// 5. Pastikan buku kas pengirim bertipe DEBIT dan penerima bertipe CREDIT
		List<TransactionResponse> senderTxs = walletService.getTransactions(sender.getId());
		List<TransactionResponse> receiverTxs = walletService.getTransactions(receiver.getId());
		assertEquals(TransactionType.DEBIT, senderTxs.get(0).getType());
		assertEquals(TransactionType.CREDIT, receiverTxs.get(0).getType());
	}

	@Test
	void shouldPreventOverdraftUnderConcurrentTransfers() throws InterruptedException {
		// 1. Buat dompet pengirim dengan saldo Rp 100.000 & dompet penerima Rp 0
		WalletResponse sender = walletService.createWallet(new CreateWalletRequest("concurrent-sender"));
		WalletResponse receiver = walletService.createWallet(new CreateWalletRequest("concurrent-receiver"));
		walletService.topUp(sender.getId(), new TopUpRequest(new BigDecimal("100000")));

		// 2. Siapkan 10 thread serentak, masing-masing mencoba transfer Rp 20.000
		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					walletService.transfer(new TransferRequest(
							sender.getId(),
							receiver.getId(),
							new BigDecimal("20000"),
							"Concurrent transfer test"));
					successCount.incrementAndGet();
				} catch (Exception ignored) {
					// 5 thread yang kehabisan saldo akan masuk ke catch ini
				} finally {
					latch.countDown();
				}
			});
		}

		// 3. Tunggu semua 10 thread selesai lalu matikan executor
		latch.await();
		executor.shutdown();

		// 4. Verifikasi: Hanya tepat 5 transfer yang boleh sukses!
		assertEquals(5, successCount.get());

		// 5. Saldo pengirim wajib tepat Rp 0 (tidak boleh minus) & penerima Rp 100.000
		WalletResponse finalSender = walletService.getWalletById(sender.getId());
		WalletResponse finalReceiver = walletService.getWalletById(receiver.getId());
		assertEquals(0, BigDecimal.ZERO.compareTo(finalSender.getBalance()));
		assertEquals(0, new BigDecimal("100000").compareTo(finalReceiver.getBalance()));
	}
}