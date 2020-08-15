package top.wiz.wj_ocr.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        val width = screenWidth / 3
        val height = screenHeight / 3

        canvas.let {
            it.drawLine(width, 0.0F, width, screenHeight, mLinePaint)
            it.drawLine(width * 2, 0.0F, width * 2, screenHeight, mLinePaint)
            it.drawLine(0.0F, height, screenWidth, height, mLinePaint)
            it.drawLine(0.0F, height * 2, screenWidth, height * 2, mLinePaint)
        }
    }
}