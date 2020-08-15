package top.wiz.wj_ocr

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import top.wiz.wj_ocr.WjOcrPlugin.Companion.TAG
import top.wiz.wj_ocr.util.ImageUtil
import java.io.File

class TessActivity : Activity() {

    private val tessDirName: String = "tessdata"
    private val api: TessBaseAPI = TessBaseAPI()
    private var isTess: Boolean = false;

    private lateinit var tessDataFile: File
    private lateinit var language: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = intent.getStringExtra("language");
        val tessDataDir = File(getExternalFilesDir(""), tessDirName)
        tessDataFile = File(tessDataDir, "${language}.traineddata")
        Log.d(TAG, "onCreate: " + tessDataFile.path)
        if (tessDataFile.exists()) {
            isTess = api.init(tessDataDir.parent, language)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO;
        } else {
            Thread(Runnable {
                Log.d(TAG, "Runnable: ${System.currentTimeMillis()}")
                val copeAssets = ImageUtil.copeAssets(this, tessDirName, tessDataDir.parent)
                Log.d(TAG, "Runnable: $copeAssets")
                Log.d(TAG, "Runnable: ${System.currentTimeMillis()}")
                if (copeAssets) {
                    isTess = api.init(tessDataDir.parent, language)
                    api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO;
                } else {
                    WjOcrPlugin.ocrResult.error("拷贝文件失败", null, null)
                    finish()
                }
            }).start()
        }
        permissionResult()
    }

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
}