package top.wiz.wj_ocr.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import top.wiz.wj_ocr.WjOcrPlugin.Companion.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageUtil {
    companion object {
        // 加载资源文件夹
        fun copeAssets(context: Context, assetsDirName: String, copyDirPath: String): Boolean {
            val assetsList = context.assets.list(assetsDirName) ?: arrayOf()
            if (assetsList.isNotEmpty()) {
                Log.d(TAG, "copeAssets: 是一个文件夹")
                File(copyDirPath, assetsDirName).mkdirs()
                return assetsList.fold(true) { isCope, item ->
                    copeAssets(context, "$assetsDirName/$item", copyDirPath)
                            && isCope
                }
            } else {
                Log.d(TAG, "copeAssets: 是一个文件")
                val ins: InputStream = context.assets.open(assetsDirName)
                val fos = FileOutputStream(File(copyDirPath, assetsDirName))
                val buffer = ByteArray(1024)
                while (true) {
                    val len: Int = ins.read(buffer)
                    if (len == -1) break
                    fos.write(buffer, 0, len)
                }
                ins.close()
                fos.close()
                return true
            }
        }

        // 保存图像添加入媒体数据库
        fun insertImage(contentResolver: ContentResolver, bitmap: Bitmap, fileName: String,
                        fileDir: String, isInsertGallery: Boolean): String {
            val imageFile = File(fileDir, fileName)
            if (imageFile.exists()) imageFile.delete()
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            if (isInsertGallery) {
                MediaStore.Images.Media.insertImage(contentResolver, imageFile.absolutePath,
                        fileName, fileName)
            }
            return imageFile.absolutePath
        }

        // url转文件路径
        fun url2path(context: Context, uri: Uri): String {
            var realPath: String? = null
            when (uri.scheme) {
                ContentResolver.SCHEME_FILE -> realPath = uri.path
                ContentResolver.SCHEME_CONTENT -> {
                    val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                    cursor?.let {
                        if (it.moveToFirst()) {
                            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                            if (index > -1) {
                                realPath = cursor.getString(index)
                            }
                        }
                        cursor.close()
                    }
                }
                else -> realPath = uri.path
            }
            if (realPath.isNullOrEmpty()) {
                val uriString: String = uri.toString()
                val index = uriString.lastIndexOf("/")
                val imageName = uriString.substring(index)
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(storageDir, imageName)
                if (imageFile.exists()) {
                    realPath = imageFile.absolutePath
                } else {
                    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    realPath = File(filesDir, imageName).absolutePath
                }
            }
            return realPath ?: ""
        }

        // 灰度图片
        fun convertGray(bitmap: Bitmap): Bitmap {
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0.0F)
            val filter = ColorMatrixColorFilter(colorMatrix)
            val paint = Paint()
            paint.colorFilter = filter
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint)
            return result
        }
    }
}