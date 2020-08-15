package top.wiz.wj_ocr.camera

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.RelativeLayout
import top.wiz.wj_ocr.TessActivity
import top.wiz.wj_ocr.util.CameraUtil
import java.util.*
import kotlin.collections.ArrayList

class CameraPreview : SurfaceView, SurfaceHolder.Callback, Camera.AutoFocusCallback,
        SensorEventListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mFocusView?.let {
                    it.x = event.x - it.width / 2
                    it.y = event.y - it.height / 2
                    it.beginFocus()
                }
            } else if (event.action == MotionEvent.ACTION_UP) {
                focusOnTouch(event)
                v.performClick()
            }
            true
        }
    }

    private var viewWidth = 0
    private var viewHeight = 0

    private lateinit var camera: Camera

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY))
    }

    override fun onAutoFocus(success: Boolean, camera: Camera?) {

    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera = CameraUtil.getCameraInstance()
        camera.setPreviewDisplay(holder)
        updateCameraParameters()
    }

    private fun updateCameraParameters() {
        val degrees: Int = CameraUtil.displayOrientation(context as Activity, Camera.CameraInfo.CAMERA_FACING_BACK)
        camera.setDisplayOrientation(degrees)
        val p = camera.parameters

        p.setGpsTimestamp(Date().time)
        p.pictureFormat = PixelFormat.JPEG
        p.focusMode = Camera.Parameters.FOCUS_MODE_AUTO

        val displayMetrics = context.resources.displayMetrics
        val w = displayMetrics.widthPixels.toDouble()
        val h = displayMetrics.heightPixels.toDouble()

        val previewSize: Camera.Size = CameraUtil.fixSize(context, p.supportedPreviewSizes, w, h)
        p.setPreviewSize(previewSize.width, previewSize.height)

        val pictureSize: Camera.Size = CameraUtil.fixSize(context, p.supportedPictureSizes, w, h)
        p.setPictureSize(pictureSize.width, pictureSize.height)

//        val rotation = CameraUtil.pictureRotation(context as Activity, Camera.CameraInfo.CAMERA_FACING_BACK)
//        p.setRotation(rotation)

        camera.parameters = p
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        camera.stopPreview()
        updateCameraParameters()
        camera.setPreviewDisplay(holder)
        camera.startPreview()
        setFocus()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera.release()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    private var mFocusView: FocusView? = null
    private var pictureCallback: Camera.PictureCallback? = null

    fun setFocusView(focusView: FocusView) {
        mFocusView = focusView
    }

    fun setPictureCallback(callback: TessActivity) {
        pictureCallback = callback
    }

    fun focusOnTouch(event: MotionEvent) {
        val location = IntArray(2)
        val relativeLayout: RelativeLayout = parent as RelativeLayout
        relativeLayout.getLocationOnScreen(location)
        val focusRect: Rect = CameraUtil.calculateTapArea(mFocusView!!.width,
                mFocusView!!.height, 1f, event.rawX, event.rawY,
                location[0], location[0] + relativeLayout.width, location[1],
                location[1] + relativeLayout.height)
        val meteringRect: Rect = CameraUtil.calculateTapArea(mFocusView!!.width,
                mFocusView!!.height, 1.5f, event.rawX, event.rawY,
                location[0], location[0] + relativeLayout.width, location[1],
                location[1] + relativeLayout.height)
        val parameters = camera.parameters
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        if (parameters.maxNumFocusAreas > 0) {
            val focusAreas: MutableList<Camera.Area> = ArrayList()
            focusAreas.add(Camera.Area(focusRect, 1000))
            parameters.focusAreas = focusAreas
        }
        if (parameters.maxNumMeteringAreas > 0) {
            val meteringAreas: MutableList<Camera.Area> = ArrayList()
            meteringAreas.add(Camera.Area(meteringRect, 1000))
            parameters.meteringAreas = meteringAreas
        }
        try {
            camera.parameters = parameters
        } catch (e: Exception) {
        }
        camera.autoFocus(this)
    }

    fun start() {
        camera.startPreview()
    }

    fun stop() {
        camera.stopPreview()
    }

    fun takePicture() {
        camera.takePicture(null, null, pictureCallback)
    }

    fun setFocus() {
        mFocusView?.let {
            if (it.isFocusing()) {
                camera.autoFocus(this)
                val displayMetrics = context.resources.displayMetrics
                val w = displayMetrics.widthPixels.toFloat()
                val h = displayMetrics.heightPixels.toFloat()
                it.x = (w - it.width) / 2
                it.y = (h - it.height) / 2
                it.beginFocus()
            }
        }
    }
}