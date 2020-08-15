package top.wiz.wj_ocr

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import top.wiz.wj_ocr.WjOcrPlugin.Companion.TAG
import top.wiz.wj_ocr.camera.CameraPreview
import top.wiz.wj_ocr.util.ImageUtil
import java.io.File

class TessActivity : Activity(), Camera.PictureCallback {

    private val tessDirName: String = "tessdata"
    private val api: TessBaseAPI = TessBaseAPI()
    private var isTess: Boolean = false

    private lateinit var tessDataFile: File
    private lateinit var language: String

    private var cameraPreview: CameraPreview? = null

    private var source: String? = null
    private var dialog: ProgressDialog? = null

    private fun permissionResult() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            WjOcrPlugin.ocrResult.error("permission.CAMERA error", null, null);
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            WjOcrPlugin.ocrResult.error("PERMISSION_STORAGE error", null, null);
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE);
        } else {
            Toast.makeText(this, "权限已开启", Toast.LENGTH_SHORT).show();
        }
    }

    val REQUEST_CAMERA = 1001
    val REQUEST_STORAGE = 1002

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA || requestCode == REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionResult();
            } else {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionResult()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "设备没有摄像头", Toast.LENGTH_SHORT).show()
            WjOcrPlugin.ocrResult.error("设备没有摄像头", null, null)
            finish()
        }
        language = intent.getStringExtra("language");
        val tessDataDir = File(getExternalFilesDir(""), tessDirName)
        tessDataFile = File(tessDataDir, "${language}.traineddata")
        if (tessDataFile.exists()) {
            isTess = api.init(tessDataDir.parent, language)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO;
        } else {
            Thread(Runnable {
                val copeAssets = ImageUtil.copeAssets(this, tessDirName, tessDataDir.parent)
                if (copeAssets) {
                    isTess = api.init(tessDataDir.parent, language)
                    api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO;
                } else {
                    WjOcrPlugin.ocrResult.error("拷贝文件失败", null, null)
                    finish()
                }
            }).start()
        }

        // 设置横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tess_act)

        cameraPreview = findViewById(R.id.cameraPreview);
        cameraPreview?.setFocusView(findViewById(R.id.focusView))

        cameraPreview?.setPictureCallback(this)

        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            WjOcrPlugin.ocrResult.error("cancel", null, null);
            finish();
        }
        findViewById<ImageView>(R.id.btn_take).setOnClickListener {
            cameraPreview?.takePicture();
        }
        findViewById<Button>(R.id.btn_album).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_ALBUM)
        }
    }

    val REQUEST_ALBUM = 2001

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ALBUM) {
            Log.d(TAG, "onActivityResult: ")
//            source = ImageUtil.url2path(this, data.data)
//            Log.d(TAG, "onActivityResult: $source")
//            dialog = ProgressDialog(this)
//            dialog.setMessage("正在识别...")
//            dialog.setCancelable(false)
//            dialog.show()
//            Thread(this).start()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        camera?.release()
        Log.d(TAG, "onPictureTaken: ")
    }
}