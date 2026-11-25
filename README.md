# Anti PMO Shield

Android app untuk memblokir konten dan tautan pemicu relapse. Fokus: proteksi cepat, antarmuka minimal, data lokal yang aman.

## Build

Build otomatis lewat GitHub Actions. Buka tab **Actions**, pilih workflow **Build APK**, lalu unduh artifact `antipmo-apk` dari run terbaru. Workflow jalan pada `push` ke `main`/`master` dan bisa dipicu manual lewat `Run workflow`.

Build lokal:

```bash
./gradlew assembleDebug
```

Butuh JDK 21, Android SDK Platform 36, dan Android SDK Build Tools terbaru. Gradle wrapper memakai Gradle 9.3.1 dan akan diunduh otomatis.

## Signing release

Tanpa konfigurasi, `assembleRelease` memakai debug signing supaya APK tetap bisa dipasang untuk pengujian. Untuk signing resmi, set repository secrets:

- `KEYSTORE_BASE64`: file `.jks` dalam base64
- `STORE_PASSWORD`
- `KEY_PASSWORD`
- `KEY_ALIAS` (opsional, default `upload`)

Secara lokal, set variabel environment `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`, dan opsional `KEY_ALIAS`.

## Database

Room SQLite, nama `anti_pmo_shield.db`, versi 1. Mode WAL dan foreign key aktif. Tidak ada `fallbackToDestructiveMigration`, jadi kenaikan versi schema harus disertai migration eksplisit. Schema diekspor ke `app/schemas/` lewat KSP.

## UI

Arah desain ada di [DESIGN.md](DESIGN.md). Tema mengikuti sistem terang/gelap.

## Catatan

Permission Accessibility dan Draw-over-other-apps diberikan pengguna lewat tombol di aplikasi. Tidak ada Firebase, Google Services, atau layanan eksternal selain endpoint AI yang dikonfigurasi pengguna.
