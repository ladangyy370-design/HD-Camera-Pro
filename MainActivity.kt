package com.example.hdcamerapro

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var status: TextView
    private lateinit var captureButton: Button
    private lateinit var videoButton: Button
    private lateinit var lutButton: Button
    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var isRecording = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var lutIndex = 0

    private val lutNames = listOf("ORIGINAL", "CINEMATIC", "WARM", "COOL")

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (hasPermissions()) startCamera()
            else toast("Izin kamera dan mikrofon diperlukan.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        buildUi()
        if (hasPermissions()) startCamera() else permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    private fun buildUi() {
        val base = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF090B0F.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 8, 18, 8)
        }
        val brand = TextView(this).apply {
            text = "HD CAMERA PRO"
            textSize = 18f
            setTextColor(0xFFD6A84B.toInt())
            letterSpacing = 0.12f
        }
        status = TextView(this).apply {
            text = "SIAP"
            textSize = 11f
            setTextColor(0xFFB8C0CC.toInt())
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
        header.addView(brand, LinearLayout.LayoutParams(0, 48, 1f))
        header.addView(status, LinearLayout.LayoutParams(120, 48))
        base.addView(header)

        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        base.addView(previewView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 10, 14, 18)
        }
        val modeHint = TextView(this).apply {
            text = "PHOTO  •  VIDEO"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFFB8C0CC.toInt())
            setPadding(0, 3, 0, 8)
        }
        controls.addView(modeHint)

        lutButton = Button(this).apply {
            text = "FILTER FOTO: ${lutNames[lutIndex]}"
            isAllCaps = false
            setOnClickListener {
                lutIndex = (lutIndex + 1) % lutNames.size
                text = "FILTER FOTO: ${lutNames[lutIndex]}"
            }
        }
        controls.addView(lutButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 48
        ))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        captureButton = Button(this).apply {
            text = "FOTO"
            isAllCaps = false
            setOnClickListener { takePhoto() }
        }
        videoButton = Button(this).apply {
            text = "MULAI VIDEO"
            isAllCaps = false
            setOnClickListener { toggleVideo() }
        }
        val flipButton = Button(this).apply {
            text = "BALIK"
            isAllCaps = false
            setOnClickListener {
                if (!isRecording) {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                    startCamera()
                }
            }
        }
        row.addView(captureButton, LinearLayout.LayoutParams(0, 54, 1f))
        row.addView(videoButton, LinearLayout.LayoutParams(0, 54, 1f))
        row.addView(flipButton, LinearLayout.LayoutParams(0, 54, 1f))
        controls.addView(row)
        base.addView(controls)
        setContentView(base)
    }

    private fun hasPermissions(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HD,
                            androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(
                                Quality.SD
                            )
                        )
                    ).build()
                videoCapture = VideoCapture.withOutput(recorder)

                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing).build()
                provider.unbindAll()
                provider.bindToLifecycle(this, selector, preview, imageCapture, videoCapture)
                status.text = "KAMERA AKTIF"
            } catch (e: Exception) {
                status.text = "KAMERA GAGAL"
                toast("Tidak dapat membuka kamera: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val tempFile = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(tempFile).build()
        captureButton.isEnabled = false
        capture.takePicture(output, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    try {
                        val source = BitmapFactory.decodeFile(tempFile.absolutePath)
                            ?: throw IllegalStateException("Gambar tidak dapat dibaca")
                        val processed = PhotoFilters.apply(source, lutIndex)
                        saveBitmap(processed)
                        if (processed !== source) processed.recycle()
                        source.recycle()
                        tempFile.delete()
                        runOnUiThread {
                            captureButton.isEnabled = true
                            toast("Foto tersimpan. Filter: ${lutNames[lutIndex]}")
                        }
                    } catch (e: Exception) {
                        tempFile.delete()
                        runOnUiThread {
                            captureButton.isEnabled = true
                            toast("Pemrosesan foto gagal: ${e.localizedMessage}")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    tempFile.delete()
                    runOnUiThread {
                        captureButton.isEnabled = true
                        toast("Gagal mengambil foto: ${exception.message}")
                    }
                }
            })
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "HDCP_${timestamp()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HD Camera Pro")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Tidak dapat membuat file galeri")
        try {
            contentResolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IllegalStateException("Gagal menyimpan JPEG")
                }
            } ?: throw IllegalStateException("Tidak dapat membuka penyimpanan")
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            throw e
        }
    }

    private fun toggleVideo() {
        val capture = videoCapture ?: return
        if (activeRecording != null) {
            activeRecording?.stop()
            activeRecording = null
            isRecording = false
            videoButton.text = "MULAI VIDEO"
            status.text = "KAMERA AKTIF"
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "HDCP_${timestamp()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/HD Camera Pro")
        }
        val output = MediaStoreOutputOptions.Builder(
            contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(values).build()

        var pending = capture.output.prepareRecording(this, output)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            pending = pending.withAudioEnabled()
        }
        activeRecording = pending.start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    isRecording = true
                    videoButton.text = "STOP VIDEO"
                    status.text = "MEREKAM"
                }
                is VideoRecordEvent.Finalize -> {
                    isRecording = false
                    activeRecording = null
                    videoButton.text = "MULAI VIDEO"
                    status.text = "KAMERA AKTIF"
                    if (!event.hasError()) toast("Video tersimpan di Movies/HD Camera Pro")
                    else toast("Rekaman gagal disimpan.")
                }
            }
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        activeRecording?.stop()
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}

private object PhotoFilters {
    // Basic lightweight color grades applied after capture, not a full 3D LUT.
    fun apply(source: Bitmap, mode: Int): Bitmap {
        if (mode == 0) return source
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val row = IntArray(width)
        for (y in 0 until height) {
            source.getPixels(row, 0, width, 0, y, width, 1)
            for (x in row.indices) {
                val pixel = row[x]
                val a = pixel ushr 24 and 0xFF
                var r = pixel ushr 16 and 0xFF
                var g = pixel ushr 8 and 0xFF
                var b = pixel and 0xFF
                when (mode) {
                    1 -> { // Cinematic: slightly cooler shadows, warm highlights, gentle contrast
                        r = tone((r - 128) * 1.08 + 128 + if (r > 128) 5 else -3)
                        g = tone((g - 128) * 1.04 + 128)
                        b = tone((b - 128) * 1.08 + 128 + if (b < 128) 4 else -2)
                    }
                    2 -> { // Warm
                        r = tone(r * 1.06 + 5)
                        g = tone(g * 1.01 + 1)
                        b = tone(b * 0.94 - 2)
                    }
                    3 -> { // Cool
                        r = tone(r * 0.94 - 2)
                        g = tone(g * 1.01)
                        b = tone(b * 1.07 + 5)
                    }
                }
                row[x] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
            output.setPixels(row, 0, width, 0, y, width, 1)
        }
        return output
    }

    private fun tone(value: Double): Int = min(255, max(0, value.toInt()))
}
