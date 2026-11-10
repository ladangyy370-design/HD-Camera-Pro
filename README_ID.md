# HD Camera Pro v0.2 — AndroidIDE starter

## Fitur
- Kamera depan/belakang dengan CameraX.
- Foto JPEG dengan mode CameraX prioritaskan kualitas.
- Pilihan grade warna foto setelah pengambilan: Original, Cinematic, Warm, Cool.
- Rekam video dengan kualitas HD jika perangkat mendukung; fallback ke SD jika tidak.
- Simpan media ke folder galeri HD Camera Pro.
- Minimum Android 8.0 / API 26.

## Penting tentang filter
Filter warna versi ini diterapkan pada foto setelah foto diambil. Ini adalah color grading sederhana berbasis piksel, belum LUT 3D sungguhan dan belum diterapkan langsung pada preview atau video. Filter warna tidak menambah resolusi atau detail optik sensor. Video direkam tanpa filter pada versi ini.

## Build menggunakan AndroidIDE di HP
1. Unduh ZIP dan ekstrak ke penyimpanan internal, misalnya `Download/HD_Camera_Pro_v0_2`.
2. Buka AndroidIDE dan pilih Open Project / Open Folder, lalu pilih folder proyek yang berisi `settings.gradle.kts`.
3. Pastikan AndroidIDE menyediakan JDK 17. Jika diminta, pilih/atur JDK 17.
4. Pastikan koneksi internet aktif saat Gradle pertama kali mengunduh Android Gradle Plugin dan dependensi.
5. Jika AndroidIDE meminta SDK, pasang Android SDK Platform 35 dan Build Tools yang direkomendasikan IDE.
6. Tunggu Gradle Sync selesai. Jangan menutup aplikasi saat proses berlangsung.
7. Buka panel Gradle lalu jalankan `:app:assembleDebug`, atau gunakan terminal proyek:
   `./gradlew assembleDebug`
8. APK debug berada di `app/build/outputs/apk/debug/app-debug.apk`.
9. Instal APK dan izinkan akses kamera serta mikrofon.

## Pengaturan untuk HP RAM 4 GB
- Tutup aplikasi lain sebelum Gradle Sync/build.
- Gunakan satu worker Gradle: `org.gradle.parallel=false`.
- Jangan menjalankan emulator; uji langsung di HP.
- Pastikan ruang kosong beberapa GB untuk SDK, cache Gradle, dan build.
- Jika proses berhenti karena memori, tutup AndroidIDE, buka kembali, lalu build ulang. Proyek ini tetap dapat dibuild di Android Studio PC bila AndroidIDE tidak mendukung versi plugin/SDK.

## Konfigurasi teknis
- Android Gradle Plugin 8.6.1
- Kotlin 2.0.20
- JDK 17
- compileSdk 35, minSdk 26
- CameraX 1.3.4

## Status
Starter kode ini disediakan sebagai sumber proyek. Belum dikompilasi atau diuji pada perangkat fisik dalam sesi ini. Kompatibilitas AndroidIDE bergantung pada versi AndroidIDE, JDK, dan SDK yang terpasang.
