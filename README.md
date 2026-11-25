# Anti PMO Shield

Aplikasi Android yang memblokir konten dan tautan pemicu relapse. Filter berjalan lewat layanan aksesibilitas. Riwayat dan aturan disimpan lokal dengan SQLite/Room. Tidak ada Firebase, Google Services, atau telemetri.

[![Build APK](https://github.com/fikmds/antipmo-shield/actions/workflows/build-apk.yml/badge.svg)](https://github.com/fikmds/antipmo-shield/actions/workflows/build-apk.yml)

## Unduh

Halaman rilis: https://github.com/fikmds/antipmo-shield/releases/latest

- Release APK: https://github.com/fikmds/antipmo-shield/releases/latest/download/app-release.apk
- Debug APK: https://github.com/fikmds/antipmo-shield/releases/latest/download/app-debug.apk

APK release memakai debug signing selama tidak ada keystore yang dikonfigurasi di CI. Aman untuk instalasi dan pengujian, tapi belum cocok untuk distribusi Play Store.

## Fitur

- Filter teks layar lewat `AccessibilityService`: kata kunci 18+, Terabox, tautan UC Browser, dan aturan regex kustom.
- Deteksi kata yang ditulis terpisah atau disisipi tanda baca, misalnya `b o k e p` dan `t e r a b o x`.
- Whitelist kata dan aplikasi agar teks seperti `javascript` tidak terblokir.
- Debounce pencegatan supaya satu halaman tidak memicu peringatan berulang.
- Mode darurat satu ketukan dari Beranda, dengan napas terkontrol dan pengalihan fisik.
- Riwayat pencegatan dan streak pemulihan dari database lokal.
- AI coach opsional dengan endpoint, model, dan kunci yang diisi pengguna sendiri. Kunci disimpan lokal di `SharedPreferences`, bukan di source.
- Tema mengikuti sistem terang atau gelap.

## Izin

Aplikasi meminta dua izin yang diberikan pengguna lewat Settings Android:

- **Accessibility**: wajib. Dipakai untuk membaca teks di aplikasi lain dan menjalankan filter.
- **Draw over other apps**: opsional. Dipakai agar peringatan bisa tampil di atas aplikasi lain.

Status dihitung dari `Settings.Secure` dan `AccessibilityManager`, lalu digabung. Kartu pengaturan di Beranda bisa ditunda lewat **Nanti saja** agar tidak muncul terus. Jika izin dimatikan manual dari Settings Android, pengguna harus mengaktifkannya kembali; aplikasi tidak bisa memaksa atau memulihkannya diam-diam.

## Build

Build otomatis lewat GitHub Actions:

1. Buka tab **Actions** di repository.
2. Pilih workflow **Build APK**.
3. Jalankan **Run workflow** atau push ke `master`/`main`.
4. Unduh artifact `antipmo-apk` dari run yang hijau.

Build lokal membutuhkan JDK 21, Android SDK Platform 36, dan Android SDK Build Tools terbaru. Gradle wrapper memakai Gradle 9.3.1 dan diunduh otomatis.

```bash
git clone https://github.com/fikmds/antipmo-shield.git
cd antipmo-shield
./gradlew testDebugUnitTest assembleDebug assembleRelease
```

Toolchain yang terverifikasi: AGP 9.1.1, Gradle 9.3.1, Kotlin 2.2.10, KSP 2.3.5, Compose BOM 2024.09.00, JDK 21.

## Signing release

Tanpa konfigurasi, `assembleRelease` memakai debug signing agar APK tetap bisa dipasang. Untuk signing resmi, tambahkan repository secrets:

| Secret | Keterangan |
| --- | --- |
| `KEYSTORE_BASE64` | File `.jks` dalam base64 |
| `STORE_PASSWORD` | Password keystore |
| `KEY_PASSWORD` | Password key |
| `KEY_ALIAS` | Opsional, default `upload` |

Untuk build lokal, pakai variabel environment `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, dan opsional `KEY_ALIAS`. Jangan commit keystore.

## Database

Room SQLite dengan nama `anti_pmo_shield.db`, versi 1. Tidak ada `fallbackToDestructiveMigration`, jadi perubahan schema tidak menghapus data pengguna tanpa migration eksplisit. WAL dan foreign key diaktifkan lewat callback saat database dibuka. Schema diekspor ke `app/schemas/` saat build.

## Struktur

```text
app/src/main/java/com/example/
  data/        Room, preferences, repository, AI client
  service/     AccessibilityService dan filter engine
  ui/          Compose screens, components, theme
  MainActivity Navigation host dan 4 tab
app/src/test/  Unit test filter engine
.github/workflows/build-apk.yml  CI build dan upload APK
DESIGN.md      Arah visual dan palettes
```

Arah desain dan batas visual ada di [DESIGN.md](DESIGN.md).
