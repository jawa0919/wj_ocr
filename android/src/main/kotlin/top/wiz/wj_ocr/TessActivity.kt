package top.wiz.wj_ocr

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.edmodo.cropper.CropImageView
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

    private var cameraLayout: RelativeLayout? = null
    private var cropLayout: RelativeLayout? = null
    private var cameraPreview: CameraPreview? = null
    private var cropImageView: CropImageView? = null

    private var source: String? = null
    private var dialog: ProgressDialog? = null

    private fun permissionResult() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            WjOcrPlugin.ocrResult.error("permission.CAMERA error", null, null)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            WjOcrPlugin.ocrResult.error("PERMISSION_STORAGE error", null, null)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE)
        } else {
            Toast.makeText(this, "权限已开启", Toast.LENGTH_SHORT).show()
        }
    }

    val REQUEST_CAMERA = 1001
    val REQUEST_STORAGE = 1002

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA || requestCode == REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionResult()
            } else {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show()
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
        language = intent.getStringExtra("language")
        val tessDataDir = File(getExternalFilesDir(""), tessDirName)
        tessDataFile = File(tessDataDir, "${language}.traineddata")
        if (tessDataFile.exists()) {
            isTess = api.init(tessDataDir.parent, language)
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
        } else {
            Thread(Runnable {
                val copeAssets = ImageUtil.copeAssets(this, tessDirName, tessDataDir.parent)
                if (copeAssets) {
                    isTess = api.init(tessDataDir.parent, language)
                    api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
                } else {
                    WjOcrPlugin.ocrResult.error("拷贝文件失败", null, null)
                    finish()
                }
            }).start()
        }

        // 设置横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.tess_act)

        cameraLayout = findViewById(R.id.take_layout)
        cameraPreview = findViewById(R.id.cameraPreview)
        cameraPreview?.setFocusView(findViewById(R.id.focusView))

        cameraPreview?.setPictureCallback(this)

        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            WjOcrPlugin.ocrResult.error("cancel", null, null)
            finish()
        }
        findViewById<ImageView>(R.id.btn_take).setOnClickListener {
            cameraPreview?.takePicture()
        }
        findViewById<Button>(R.id.btn_album).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_ALBUM)
        }

        cropLayout = findViewById(R.id.cropper_layout)
        cropImageView = findViewById(R.id.cropImageView)
        cropImageView?.setGuidelines(2)

        findViewById<ImageView>(R.id.btn_crop_close).setOnClickListener {
            cameraLayout?.visibility = View.VISIBLE
            cropLayout?.visibility = View.GONE
        }
        findViewById<ImageView>(R.id.btn_crop_check).setOnClickListener {
            cropImageView?.croppedImage?.let { bitmapSaveImageAndStart(it) }
        }
    }

    val REQUEST_ALBUM = 2001

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ALBUM) {
            Log.d(TAG, "onActivityResult: ")

            data?.data?.let {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                cropImageView?.setImageBitmap(bitmap)
                cameraLayout?.visibility = View.GONE
                cameraPreview?.stop()
                cropLayout?.visibility = View.VISIBLE
//                source = ImageUtil.url2path(this, it)
//                Log.d(TAG, "onActivityResult: $source")
//                dialog = ProgressDialog(this)
//                dialog?.setMessage("正在识别...")
//                dialog?.setCancelable(false)
//                dialog?.show()
//                Thread(Runnable {
//                    Log.d(TAG, "Thread Runnable: 字典$tessDataFile 图片$source")
//                    val bitmap = BitmapFactory.decodeFile(source)
//                    Log.d(TAG, "Thread Runnable: bitmap ${bitmap.width}*${bitmap.height}")
//                    val grayBitmap = ImageUtil.convertGray(bitmap)
//                    Log.d(TAG, "Thread Runnable: grayBitmap ${grayBitmap.width}*${grayBitmap.height}")
//                    startTessThread(grayBitmap)
//                }).start()
            }
        }
    }

    override fun onPictureTaken(data: ByteArray, camera: Camera) {
        camera.release()
        Log.d(TAG, "onPictureTaken: ")
        val taken: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        cropImageView?.setImageBitmap(taken)
        cameraLayout?.visibility = View.GONE
        cropLayout?.visibility = View.VISIBLE

//        val width: Int = taken.width
//        val height: Int = taken.height
//        Log.d(TAG, "Bitmap: $width*$height")
//        val dateTaken = System.currentTimeMillis()
//        val fileName: String = DateFormat.format("yyyyMMdd_kkmmss", dateTaken).toString() + ".jpg"
//        val fileDir = "$externalCacheDir/"
//        source = ImageUtil.insertImage(contentResolver, taken, fileName, fileDir, true)
//        taken.recycle()
//        Log.d(TAG, "onPictureTaken: $source")
//        dialog = ProgressDialog(this)
//        dialog?.setMessage("正在识别...")
//        dialog?.setCancelable(false)
//        dialog?.show()
//        Thread(Runnable {
//            Log.d(TAG, "Thread Runnable: 字典$tessDataFile 图片$source")
//            val bitmap = BitmapFactory.decodeFile(source)
//            Log.d(TAG, "Thread Runnable: bitmap ${bitmap.width}*${bitmap.height}")
//            val grayBitmap = ImageUtil.convertGray(bitmap)
//            Log.d(TAG, "Thread Runnable: grayBitmap ${grayBitmap.width}*${grayBitmap.height}")
//            startTessThread(grayBitmap)
//        }).start()
    }

    private fun bitmapSaveImageAndStart(croppedImage: Bitmap) {
        Log.d(TAG, "bitmapSaveImageAndStart: ")
        val width: Int = croppedImage.width
        val height: Int = croppedImage.height
        Log.d(TAG, "Bitmap: $width*$height")
        val dateTaken = System.currentTimeMillis()
        val fileName: String = DateFormat.format("yyyyMMdd_kkmmss", dateTaken).toString() + ".jpg"
        val fileDir = "$externalCacheDir/"
        source = ImageUtil.insertImage(contentResolver, croppedImage, fileName, fileDir, true)
        croppedImage.recycle()
        Log.d(TAG, "onPictureTaken: $source")
        dialog = ProgressDialog(this)
        dialog?.setMessage("正在识别...")
        dialog?.setCancelable(false)
        dialog?.show()
        Thread(Runnable {
            Log.d(TAG, "Thread Runnable: 字典$tessDataFile 图片$source")
            val bitmap = BitmapFactory.decodeFile(source)
            Log.d(TAG, "Thread Runnable: bitmap ${bitmap.width}*${bitmap.height}")
            val grayBitmap = ImageUtil.convertGray(bitmap)
            Log.d(TAG, "Thread Runnable: grayBitmap ${grayBitmap.width}*${grayBitmap.height}")
            startTessThread(grayBitmap)
        }).start()
    }

    private fun startTessThread(grayBitmap: Bitmap) {
        if (isTess) {
            api.setImage(grayBitmap)
            val result = api.utF8Text
            Log.d(TAG, "startTessThread: \n\n$result\n\n")
            api.end()
            runOnUiThread {
                dialog?.dismiss()
                WjOcrPlugin.ocrResult.success(result)
                finish()
            }
        } else {
            runOnUiThread { dialog?.setMessage("等待加载字典") }
            Thread.sleep(200)
            startTessThread(grayBitmap)
        }
    }
}