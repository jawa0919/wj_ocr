package top.wiz.wj_ocr.util

import android.content.Context
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
                return true;
            }
        }
    }
}