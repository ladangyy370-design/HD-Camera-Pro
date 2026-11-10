# Build APK HD Camera Pro melalui GitHub Actions (tanpa memakai ruang HP)

## Yang dibutuhkan
- Akun GitHub
- Browser di HP
- File ZIP proyek ini

## Langkah di HP
1. Buka https://github.com dan login.
2. Tekan tombol **+** lalu pilih **New repository**.
3. Isi nama repository: `HD-Camera-Pro`, pilih **Public** atau **Private**, lalu buat repository.
4. Di halaman repository pilih **Add file** > **Upload files**.
5. Ekstrak ZIP proyek ini terlebih dahulu jika browser/file manager mendukungnya, lalu unggah seluruh ISI folder `HD_Camera_Pro_Cloud` (termasuk folder tersembunyi `.github`). Jangan mengunggah folder ZIP sebagai satu file.
6. Tekan **Commit changes**.
7. Buka tab **Actions** di repository. Workflow bernama **Build HD Camera Pro APK** akan berjalan otomatis setelah commit. Jika belum berjalan, buka workflow itu lalu tekan **Run workflow**.
8. Tunggu sampai status hijau **Success**. Buka proses build yang selesai, gulir ke bagian **Artifacts**, lalu unduh `HD-Camera-Pro-debug-APK`.
9. Ekstrak file artifact ZIP yang diunduh. Di dalamnya ada `app-debug.apk`. Buka APK untuk memasang; bila diminta, izinkan instalasi dari sumber tersebut.

## Penting
- Build dilakukan di server GitHub Actions, bukan di HP. HP hanya mengunggah source dan mengunduh APK, jadi tidak perlu memasang AndroidIDE, Android SDK, atau Gradle.
- Workflow ini memakai JDK 17 dan Gradle 8.7, sesuai konfigurasi proyek.
- APK yang dihasilkan adalah **debug APK** untuk uji coba, belum APK release yang ditandatangani untuk distribusi.
- Proyek saat ini masih versi awal: filter warna foto sederhana setelah pemotretan; video belum diberi filter. Belum ada AI super-resolution/denoise atau LUT profesional.
- Jika build gagal, buka log job **build** dan kirimkan teks error merah terakhir atau screenshot-nya agar bisa diperbaiki.
