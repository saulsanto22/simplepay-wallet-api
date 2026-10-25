// Konfigurasi Base URL REST API Spring Boot kita
const API_BASE = '/api/wallets';

// State aplikasi
let activeWalletId = null;
let savedWallets = JSON.parse(localStorage.getItem('simplepay_wallets')) || [1, 2];

// Format Rupiah rapi (contoh: Rp 50.000)
function formatRupiah(number) {
    return new Intl.NumberFormat('id-ID', {
        style: 'currency',
        currency: 'IDR',
        minimumFractionDigits: 0
    }).format(number);
}

// Format Tanggal (contoh: 24 Okt 2026, 17:48)
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('id-ID', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Inisialisasi saat halaman pertama kali dimuat
window.addEventListener('DOMContentLoaded', () => {
    renderWalletDropdown();
    if (savedWallets.length > 0) {
        activeWalletId = savedWallets[0];
        document.getElementById('walletSelect').value = activeWalletId;
        loadActiveWalletData();
    }
});

// 1. Render opsi pilihan dompet di dropdown
function renderWalletDropdown() {
    const select = document.getElementById('walletSelect');
    select.innerHTML = '<option value="">-- Pilih Dompet --</option>';
    savedWallets.forEach(id => {
        const opt = document.createElement('option');
        opt.value = id;
        opt.textContent = `Dompet ID: #${id}`;
        select.appendChild(opt);
    });
}

// 2. Ganti dompet aktif saat dropdown dipilih
function switchWallet() {
    const select = document.getElementById('walletSelect');
    activeWalletId = select.value ? parseInt(select.value) : null;
    if (activeWalletId) {
        loadActiveWalletData();
    } else {
        resetCardDisplay();
    }
}

// 3. Ambil data dompet dari Backend: GET /api/wallets/{id}
async function loadActiveWalletData() {
    if (!activeWalletId) return;

    try {
        const response = await fetch(`${API_BASE}/${activeWalletId}`);
        const result = await response.json();

        if (result.success) {
            const wallet = result.data;
            document.getElementById('cardBalanceDisplay').textContent = formatRupiah(wallet.balance);
            document.getElementById('cardUserDisplay').textContent = wallet.userId;
            document.getElementById('cardIdDisplay').textContent = `ID: #${wallet.id}`;
            loadTransactions();
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('Gagal terhubung ke backend: ' + err.message, 'error');
    }
}

// 4. Ambil mutasi dari Backend: GET /api/wallets/{id}/transactions
async function loadTransactions() {
    if (!activeWalletId) return;

    const tbody = document.getElementById('transactionTableBody');
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Memuat riwayat transaksi...</td></tr>';

    try {
        const response = await fetch(`${API_BASE}/${activeWalletId}/transactions`);
        const result = await response.json();

        if (result.success && result.data.length > 0) {
            tbody.innerHTML = '';
            result.data.forEach(tx => {
                const tr = document.createElement('tr');
                const isDebit = tx.type === 'DEBIT';
                const badgeClass = isDebit ? 'badge-debit' : 'badge-credit';
                const sign = isDebit ? '- ' : '+ ';

                tr.innerHTML = `
                    <td>${formatDate(tx.createdAt)}</td>
                    <td><span class="badge ${badgeClass}">${tx.type}</span></td>
                    <td style="font-weight: 700; color: ${isDebit ? 'var(--danger)' : 'var(--success)'}">
                        ${sign}${formatRupiah(tx.amount)}
                    </td>
                    <td>${tx.description || '-'}</td>
                    <td><span class="badge badge-success">${tx.status}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Belum ada riwayat transaksi pada dompet ini.</td></tr>';
        }
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Gagal mengambil transaksi.</td></tr>';
    }
}

// 5. Form Submit: Buat Dompet Baru (POST /api/wallets)
async function handleCreateWallet(event) {
    event.preventDefault();
    const userId = document.getElementById('newUserIdInput').value.trim();

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId })
        });
        const result = await response.json();

        if (result.success) {
            showToast('Dompet berhasil dibuat untuk ' + userId, 'success');
            const newId = result.data.id;
            if (!savedWallets.includes(newId)) {
                savedWallets.push(newId);
                localStorage.setItem('simplepay_wallets', JSON.stringify(savedWallets));
            }
            renderWalletDropdown();
            document.getElementById('walletSelect').value = newId;
            activeWalletId = newId;
            loadActiveWalletData();
            closeModal('createWalletModal');
            document.getElementById('newUserIdInput').value = '';
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('Terjadi kesalahan: ' + err.message, 'error');
    }
}

// 6. Form Submit: Top-Up Saldo (POST /api/wallets/{id}/topup)
async function handleTopUp(event) {
    event.preventDefault();
    if (!activeWalletId) {
        showToast('Silakan pilih dompet terlebih dahulu!', 'error');
        return;
    }

    const amount = parseFloat(document.getElementById('topupAmountInput').value);

    try {
        const response = await fetch(`${API_BASE}/${activeWalletId}/topup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: amount })
        });
        const result = await response.json();

        if (result.success) {
            showToast(`Top-Up ${formatRupiah(amount)} Berhasil!`, 'success');
            loadActiveWalletData();
            closeModal('topupModal');
            document.getElementById('topupAmountInput').value = '';
        } else {
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('Gagal top-up: ' + err.message, 'error');
    }
}

// 7. Form Submit: Transfer Uang (POST /api/wallets/transfer)
async function handleTransfer(event) {
    event.preventDefault();
    if (!activeWalletId) {
        showToast('Silakan pilih dompet pengirim terlebih dahulu!', 'error');
        return;
    }

    const targetId = parseInt(document.getElementById('transferTargetIdInput').value);
    const amount = parseFloat(document.getElementById('transferAmountInput').value);
    const desc = document.getElementById('transferDescInput').value.trim();

    try {
        const response = await fetch(`${API_BASE}/transfer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sourceWalletId: activeWalletId,
                targetWalletId: targetId,
                amount: amount,
                description: desc
            })
        });
        const result = await response.json();

        if (result.success) {
            showToast(`Transfer ${formatRupiah(amount)} ke Dompet #${targetId} Berhasil!`, 'success');
            loadActiveWalletData();
            closeModal('transferModal');
            document.getElementById('transferTargetIdInput').value = '';
            document.getElementById('transferAmountInput').value = '';
            document.getElementById('transferDescInput').value = '';
        } else {
            // Tampilkan pesan error resmi dari Spring Boot (misal: saldo tidak cukup)
            showToast(result.message, 'error');
        }
    } catch (err) {
        showToast('Gagal transfer: ' + err.message, 'error');
    }
}

// Helper: Buka & Tutup Modal Pop-up
function openModal(modalId) {
    document.getElementById(modalId).classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

// Helper: Tampilkan Notifikasi Toast
function showToast(message, type = 'success') {
    const toast = document.getElementById('toastNotification');
    toast.textContent = message;
    toast.className = `toast toast-${type}`;
    toast.classList.remove('hidden');

    setTimeout(() => {
        toast.classList.add('hidden');
    }, 4000);
}

// Helper: Reset Tampilan Kartu
function resetCardDisplay() {
    document.getElementById('cardBalanceDisplay').textContent = 'Rp 0';
    document.getElementById('cardUserDisplay').textContent = '-';
    document.getElementById('cardIdDisplay').textContent = 'ID: #--';
    document.getElementById('transactionTableBody').innerHTML = '<tr><td colspan="5" class="empty-state">Pilih dompet aktif terlebih dahulu.</td></tr>';
}
