package top.wiz.wj_ocr.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ReferenceLine : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val mLinePaint: Paint = Paint().also {
        it.isAntiAlias = true
        it.color = Color.parseColor("#ffffffff")
        it.strokeWidth = 1.0F
    }

    private val mBlurPaint: Paint = Paint().also {
        it.flags = Paint.ANTI_ALIAS_FLAG;
        it.color = Color.TRANSPARENT
        it.alpha = 200;
        it.maskFilter = BlurMaskFilter(1F, BlurMaskFilter.Blur.INNER)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        val width = screenWidth / 3
        val height = screenHeight / 3

        canvas.let {
            canvas.drawRect(0f, 0f, screenWidth, height, mBlurPaint)
//            canvas.drawRect(0f, height, width, height * 2, mBlurPaint)
//            canvas.drawRect(width * 2, height, screenWidth, height * 2, mBlurPaint)
            canvas.drawRect(0f, height * 2, screenWidth, screenHeight, mBlurPaint)
            it.drawLine(width, 0.0F, width, screenHeight, mLinePaint)
            it.drawLine(width * 2, 0.0F, width * 2, screenHeight, mLinePaint)
            it.drawLine(0.0F, height, screenWidth, height, mLinePaint)
            it.drawLine(0.0F, height * 2, screenWidth, height * 2, mLinePaint)
        }
    }
}