package top.wiz.wj_ocr.camera

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class FocusView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val mBorderWidth = 4.0F
    private val mLinePaint: Paint = Paint().also {
        it.isAntiAlias = true
        it.style = Paint.Style.STROKE
        it.color = Color.parseColor("#45ffffff")
        it.strokeWidth = mBorderWidth
    }

    init {
        this.alpha = 0f //初始化设置透明
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width.toFloat() / 2
        val cy = height.toFloat() / 2
        val radius = cy - mBorderWidth / 2
        canvas.drawCircle(cx, cy, radius, mLinePaint)
    }

    private fun setMainColor() {
        mLinePaint.color = Color.parseColor("#52ce90")
        postInvalidate()
    }

    private fun reSet() {
        mLinePaint.color = Color.parseColor("#45e0e0e0")
        postInvalidate()
    }


    private var isFocusing = false

    fun beginFocus() {
        isFocusing = true
        if (animSet.isRunning) {
            animSet.cancel()
        }
        if (fadeInOut.isRunning) {
            fadeInOut.cancel()
        }
        animSet.start()
    }

    fun isFocusing(): Boolean {
        return isFocusing
    }

    private var animSet: AnimatorSet = AnimatorSet().also {
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.3f, 1f)
        it.play(scaleX).with(scaleY)
        it.interpolator = LinearInterpolator()
        it.duration = 1000
        it.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {
                this@FocusView.alpha = 1f
            }

            override fun onAnimationEnd(p0: Animator?) {
                setMainColor()
                fadeInOut.start()
            }
        })
    }

    private var fadeInOut: ObjectAnimator = ObjectAnimator.ofFloat(
            this, "alpha", 1f, 0f).also {
        it.duration = 500
        it.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                reSet()
                isFocusing = false
            }
        })
    }
}
