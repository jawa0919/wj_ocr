package top.wiz.wj_ocr.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.Camera
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import top.wiz.wj_ocr.WjOcrPlugin.Companion.TAG
import kotlin.math.abs

class CameraUtil {
    companion object {

        //获取摄像头实例
        fun getCameraInstance(): Camera {
            var camera: Camera? = null
            val numberOfCameras = Camera.getNumberOfCameras()
            for (cameraId in 0..numberOfCameras) {
                Log.d(TAG, "getCameraInstance: $cameraId")
            }
            for (cameraId in 0 until numberOfCameras) {
                val cameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(cameraId, cameraInfo)
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    camera = Camera.open(cameraId)
                    break
                }
            }
            return camera ?: Camera.open(0)
        }

        //获取设备方向
        fun deviceOrientation(activity: Activity): Int {
            val rotation = activity.windowManager.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
            return degrees
        }

        // 设置预览方向
        fun displayOrientation(activity: Activity, cameraId: Int): Int {
            val degrees: Int = deviceOrientation(activity)
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            var result = 0
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360 // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }

        fun getScreenWH(context: Context): DisplayMetrics {
            var dMetrics = DisplayMetrics()
            dMetrics = context.resources.displayMetrics
            return dMetrics
        }

        // 最优化尺寸
        fun fixSize(context: Context, sizes: List<Camera.Size>, w: Double, h: Double): Camera.Size {
            val ASPECT_TOLERANCE = 0.1//阈值，用于选取最优
            var targetRatio = -1.0
            val orientation = context.resources.configuration.orientation
            //保证targetRatio始终大于1，因为size.width/size.height始终大于1
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                targetRatio = h / w
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                targetRatio = w / h
            }
            var optimalSize: Camera.Size? = null
            var minDiff = Double.MAX_VALUE
            val targetHeight = w.coerceAtMost(h)
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                //若大于了阈值，则继续筛选
                if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }
                if (abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
            //若通过比例没有获得最优，则通过最小差值获取最优，保证至少能得到值
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE
                for (size in sizes) {
                    if (abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size
                        minDiff = abs(size.height - targetHeight)
                    }
                }
            }
            return optimalSize!!;
        }

        // 图片旋转
        fun pictureRotation(activity: Activity, cameraId: Int): Int {
            val degrees = deviceOrientation(activity)
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            var pictureRotation = 0
            pictureRotation = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (info.orientation - degrees + 360) % 360
            } else {
                (info.orientation + degrees) % 360
            }
            return pictureRotation
        }

        // 计算焦点及测光区域
        fun calculateTapArea(focusWidth: Int, focusHeight: Int,
                             areaMultiple: Float,
                             x: Float, y: Float,
                             previewLeft: Int, previewRight: Int,
                             previewTop: Int, previewBottom: Int): Rect {
            val areaWidth = (focusWidth * areaMultiple).toInt()
            val areaHeight = (focusHeight * areaMultiple).toInt()

            val centerX = (previewLeft + previewRight) / 2
            val centerY = (previewTop + previewBottom) / 2

            val unitX = (previewRight.toDouble() - previewLeft.toDouble()) / 2000
            val unitY = (previewBottom.toDouble() - previewTop.toDouble()) / 2000

            val left: Int = clamp(((x - areaWidth / 2 - centerX) / unitX).toInt(), -1000, 1000)
            val top: Int = clamp(((y - areaHeight / 2 - centerY) / unitY).toInt(), -1000, 1000)
            val right: Int = clamp((left + areaWidth / unitX).toInt(), -1000, 1000)
            val bottom: Int = clamp((top + areaHeight / unitY).toInt(), -1000, 1000)
            return Rect(left, top, right, bottom)
        }

        fun clamp(x: Int, min: Int, max: Int): Int {
            if (x > max) return max
            return if (x < min) min else x
        }
    }
}