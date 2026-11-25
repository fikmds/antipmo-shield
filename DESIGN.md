# DESIGN.md - Anti PMO Shield

## Direction
Utilitas keselamatan yang tenang dan cepat dibaca. Tidak ada dekorasi yang tidak membantu keputusan.

Dial: ENERGY 1 / RHYTHM 1 / MOTION 1.

## Palette
- Light: background `#F7F8F6`, surface `#FFFFFF`, ink `#15201B`, muted `#5F6B65`, border `#DCE3DE`.
- Dark: background `#111614`, surface `#1A211D`, ink `#F2F5F3`, muted `#A8B5AE`, border `#33403A`.
- Accent teal: light `#0F766E`, dark `#5EEAD4`. Alasan: satu warna untuk status aktif dan aksi utama, bukan untuk semua elemen.
- Danger merah: light `#B42318`, dark `#FCA5A5`. Alasan: hanya untuk status dicegat, mode darurat, dan konfirmasi destruktif.

## Typography
Roboto sistem (FontFamily.Default). Alasan: terbaca di layar kecil, tanpa unduhan font.

## Shape and elevation
- Kartu dan panel: radius 12dp, border 1dp, tanpa shadow.
- Tombol dan input: radius 8dp. Chip status boleh 100dp karena berisi status, bukan dekorasi.
- Permukaan datar. Elevasi tidak dipakai sebagai gaya.

## Layout
- Satu tugas per layar. Beranda: status + streak + aksi. Aturan: pengaturan. Riwayat: daftar. AI: percakapan. Darurat: napas.
- Spacing scale: 8, 12, 16, 24dp. Jarak dipakai untuk memisahkan grup, bukan mengisi template.
- Navigasi bawah 4 tab: Beranda, Aturan, Riwayat, AI. Darurat dibuka lewat tombol di Beranda dan dari alert, bukan tab kelima.

## Motion
Hanya ripple standar dan animasi lingkaran napas di mode darurat. Alasan animasi napas: memandu pernapasan, bukan dekorasi.

## Content
Tanpa emoji, tanpa gradient, tanpa teks marketing. Angka hanya berasal dari database atau flow nyata. Teks kosong/gagal menyebut penyebab dan tindakan berikutnya.

## Decisions
- Teal sebagai aksen: menandakan "proteksi aktif" di satu tempat per layar.
- Merah sebagai status: hanya saat akses dicegat atau konfirmasi reset/hapus.
- Border 1dp menggantikan shadow: memisahkan kartu tanpa efek mengambang.
- Teks sistem: prioritas keterbacaan dan ukuran APK.
- 4 tab: mengurangi beban pilihan; darurat tetap satu ketukan dari Beranda.
