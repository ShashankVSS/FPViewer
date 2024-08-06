package com.example.fpviewer

import android.os.Bundle
import android.Manifest
import android.hardware.Camera
import android.widget.Button
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Toast
import com.example.fpviewer.CameraPreview
import com.example.fpviewer.R
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var camera: Camera? = null
    private var cameraId = 0
    private lateinit var preview: CameraPreview

    private val cameraRequestId = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()

        findViewById<Button>(R.id.button_switch_camera).setOnClickListener {
            switchCamera()
        }
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        } else {
            initializeCamera(cameraId)
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraRequestId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraRequestId -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need camera permission to use this app", Toast.LENGTH_SHORT).show()
                } else {
                    initializeCamera(cameraId)
                }
            }
        }
    }

    private fun initializeCamera(id: Int) {
        camera = getCameraInstance(id)
        camera?.let {
            if (::preview.isInitialized) {
                preview.surfaceDestroyed(preview.holder)
                preview.holder.removeCallback(preview)
                (preview.parent as ViewGroup).removeView(preview)
            }
            preview = CameraPreview(this, it)
            findViewById<ViewGroup>(R.id.camera_preview_container).addView(preview)
        }
    }

    private fun switchCamera() {
        releaseCamera()
        cameraId = (cameraId + 1) % Camera.getNumberOfCameras()
        initializeCamera(cameraId)
    }

    private fun getCameraInstance(id: Int): Camera? {
        return try {
            Camera.open(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    private fun releaseCamera() {
        camera?.release()
        camera = null
    }
}
