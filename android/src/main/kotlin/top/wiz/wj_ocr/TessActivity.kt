package top.wiz.wj_ocr

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class TessActivity : Activity() {
    val TAG = "TessActivity"

    private val tessDirName: String = "tessdata"
//    private val api: TessBaseAPI = TessBaseAPI()

    private lateinit var tessDataFile: File
    private lateinit var language: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = intent.getStringExtra("language");
        val tessDataDir = File(externalCacheDir, tessDirName)
        tessDataFile = File(tessDataDir, "${language}.traineddata")
        Log.d(TAG, "onCreate: " + tessDataFile.path)
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