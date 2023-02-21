package com.zerodev.coachmark_view

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.text.Spannable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import com.zerodev.coachmark_view.config.DismissType
import com.zerodev.coachmark_view.config.Gravity
import com.zerodev.coachmark_view.config.PointerType

@SuppressLint("ViewConstructor")
class CoachmarkView private constructor(context: Context, view: View?) : FrameLayout(context) {
    private val selfPaint = Paint()
    private val paintLine = Paint()
    private val paintCircle = Paint()
    private val paintCircleInner = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val arrowPath = Path()
    private val target: View?
    private var targetRect: RectF? = null
    private val backgroundRect = Rect()
    private val density: Float
    private var stopY = 0f
    private var isTop = false
    var isShowing = false
        private set
    private var yMessageView = 0
    private var startYLineAndCircle = 0f
    private var circleIndicatorSize = 0f
    private var circleIndicatorSizeFinal = 0f
    private var circleInnerIndicatorSize = 0f
    private var lineIndicatorWidthSize = 0f
    private var messageViewPadding = 0
    private var marginGuide = 0f
    private var strokeCircleWidth = 0f
    private var indicatorHeight = 0f
    private var isPerformedAnimationSize = false
    private var mCoachmarkListener: CoachmarkListener? = null
    private var mGravity: Gravity? = null
    private var dismissType: DismissType? = null
    private var pointerType: PointerType? = null
    private var mMessageView: View

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        target = view
        density = context.resources.displayMetrics.density
        init()
        mMessageView = View(getContext())

        val layoutListener: ViewTreeObserver.OnGlobalLayoutListener = object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (target is Targetable) {
                    targetRect = (target as Targetable?)?.boundingRect()
                } else {
                    val locationTarget = IntArray(2)
                    target!!.getLocationOnScreen(locationTarget)
                    targetRect = RectF(
                        locationTarget[0].toFloat(),
                        locationTarget[1].toFloat(),
                        (locationTarget[0] + target.width).toFloat(),
                        (locationTarget[1] + target.height).toFloat()
                    )
                    if (isLandscape) {
                        targetRect!!.offset(-statusBarHeight.toFloat(), 0f)
                    }
                }
                backgroundRect[paddingLeft, paddingTop, width - paddingRight] =
                    height - paddingBottom
                if (isLandscape) {
                    backgroundRect.offset(-navigationBarSize, 0)
                } else {
                    backgroundRect.offset(0, -navigationBarSize)
                }
                isTop = targetRect!!.top + indicatorHeight <= height / 2f
                marginGuide = (if (isTop) marginGuide else -marginGuide).toInt().toFloat()
                setMessageLocation(resolveMessageViewLocation())
                startYLineAndCircle =
                    (if (isTop) targetRect!!.bottom else targetRect!!.top) + marginGuide
                stopY = yMessageView + indicatorHeight + if (isTop) -marginGuide else marginGuide
                startAnimationSize()
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private fun startAnimationSize() {
        if (!isPerformedAnimationSize) {
            val circleSizeAnimator = ValueAnimator.ofFloat(
                0f,
                circleIndicatorSizeFinal
            )
            circleSizeAnimator.addUpdateListener { valueAnimator: ValueAnimator? ->
                circleIndicatorSize = circleSizeAnimator.animatedValue as Float
                circleInnerIndicatorSize =
                    circleSizeAnimator.animatedValue as Float - density
                postInvalidate()
            }
            val linePositionAnimator = ValueAnimator.ofFloat(
                stopY,
                startYLineAndCircle
            )
            linePositionAnimator.addUpdateListener { valueAnimator: ValueAnimator? ->
                startYLineAndCircle = linePositionAnimator.animatedValue as Float
                postInvalidate()
            }
            linePositionAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
            linePositionAnimator.start()
            linePositionAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    circleSizeAnimator.duration = SIZE_ANIMATION_DURATION.toLong()
                    circleSizeAnimator.start()
                    isPerformedAnimationSize = true
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
        }
    }

    private fun init() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density
        marginGuide = MARGIN_INDICATOR * density
        indicatorHeight = INDICATOR_HEIGHT * density
        messageViewPadding = (MESSAGE_VIEW_PADDING * density).toInt()
        strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density
    }

    val navigationBarSize: Int
        get() {
            val resources = resources
            val id = resources.getIdentifier("navigation_bar_height_landscape", "dimen", "android")
            return if (id > 0) {
                resources.getDimensionPixelSize(id)
            } else 0
        }
    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    private val isLandscape: Boolean
        private get() {
            val display_mode = resources.configuration.orientation
            return display_mode != Configuration.ORIENTATION_PORTRAIT
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (target != null) {
            selfPaint.color = BACKGROUND_COLOR
            selfPaint.style = Paint.Style.FILL
            selfPaint.isAntiAlias = true
            canvas.drawRect(backgroundRect, selfPaint)
            paintLine.style = Paint.Style.FILL
            paintLine.color = LINE_INDICATOR_COLOR
            paintLine.strokeWidth = lineIndicatorWidthSize
            paintLine.isAntiAlias = true
            paintCircle.style = Paint.Style.FILL
            paintCircle.color = CIRCLE_INDICATOR_COLOR
            paintCircle.strokeCap = Paint.Cap.ROUND
            paintCircle.strokeWidth = strokeCircleWidth
            paintCircle.isAntiAlias = true
            paintCircleInner.style = Paint.Style.FILL
            paintCircleInner.color = CIRCLE_INNER_INDICATOR_COLOR
            paintCircleInner.isAntiAlias = true
            val x = targetRect!!.left / 2 + targetRect!!.right / 2

            //Code to draw arrow
            arrowPath.reset()
            if (isTop) {
                arrowPath.moveTo(x, startYLineAndCircle - circleIndicatorSize * 2)
            } else {
                arrowPath.moveTo(x, startYLineAndCircle + circleIndicatorSize * 2)
            }
            arrowPath.lineTo(x + circleIndicatorSize, startYLineAndCircle)
            arrowPath.lineTo(x - circleIndicatorSize, startYLineAndCircle)
            arrowPath.close()
            canvas.drawPath(arrowPath, paintCircle)


            targetPaint.xfermode = X_FER_MODE_CLEAR
            targetPaint.isAntiAlias = true
            if (target is Targetable) {
                canvas.drawPath((target as Targetable).guidePath()!!, targetPaint)
            } else {
                canvas.drawRoundRect(
                    targetRect!!,
                    RADIUS_SIZE_TARGET_RECT.toFloat(),
                    RADIUS_SIZE_TARGET_RECT.toFloat(),
                    targetPaint
                )
            }
        }
    }

    fun dismiss() {
        ((context as Activity).window.decorView as ViewGroup).removeView(this)
        isShowing = false
        if (mCoachmarkListener != null) {
            mCoachmarkListener?.onDismiss(target)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (dismissType) {
                DismissType.outside -> if (!isViewContains(mMessageView, x, y)) {
                    dismiss()
                }
                DismissType.anywhere -> dismiss()
                DismissType.targetView -> if (targetRect!!.contains(x, y)) {
                    target!!.performClick()
                    dismiss()
                }
                DismissType.selfView -> if (isViewContains(mMessageView, x, y)) {
                    dismiss()
                }
                DismissType.outsideTargetAndMessage -> if (!(targetRect!!.contains(
                        x,
                        y
                    ) || isViewContains(
                        mMessageView,
                        x,
                        y
                    ))
                ) {
                    dismiss()
                }
                else -> {}
            }
            return true
        }
        return false
    }

    private fun isViewContains(view: View, rx: Float, ry: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    private fun setMessageLocation(p: Point) {
        mMessageView.x = p.x.toFloat()
        mMessageView.y = p.y.toFloat()
        postInvalidate()
    }

    fun updateGuideViewLocation() {
        requestLayout()
    }

    private fun resolveMessageViewLocation(): Point {
        var xMessageView: Int
        xMessageView = if (mGravity === Gravity.center) {
            (targetRect!!.left - mMessageView.width / 2 + target!!.width / 2).toInt()
        } else {
            targetRect!!.right.toInt() - mMessageView.width
        }
        if (isLandscape && xMessageView + mMessageView.width > backgroundRect.right) {
            xMessageView -= navigationBarSize
        }
        if (xMessageView + mMessageView.width > width) {
            xMessageView = width - mMessageView.width
        }
        if (xMessageView < 0) {
            xMessageView = 0
        }

        //set message view bottom
        if (targetRect!!.top + indicatorHeight > height / 2f) {
            isTop = false
            yMessageView = (targetRect!!.top - mMessageView.height - indicatorHeight).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect!!.top + target!!.height + indicatorHeight).toInt()
        }
        if (yMessageView < 0) {
            yMessageView = 0
        }
        return Point(xMessageView, yMessageView)
    }

    fun show() {
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.isClickable = false
        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        startAnimation(startAnimation)
        isShowing = true
    }


    fun setMessageViewToBeShown(view: View) {
        removeView(this.mMessageView)
        this.mMessageView = view
        val viewLayoutParams =
            MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(
            mMessageView,
            viewLayoutParams
        )
    }


    class Builder(private val context: Context) {
        private var targetView: View? = null
        private var title: String? = null
        private var contentText: String? = null
        private var gravity: Gravity? = null
        private var dismissType: DismissType? = null
        private var pointerType: PointerType? = null
        private var contentSpan: Spannable? = null
        private var titleTypeFace: Typeface? = null
        private var contentTypeFace: Typeface? = null
        private var coachmarkListener: CoachmarkListener? = null
        private var titleTextSize = 0
        private var contentTextSize = 0
        private var lineIndicatorHeight = 0f
        private var lineIndicatorWidthSize = 0f
        private var circleIndicatorSize = 0f
        private var circleInnerIndicatorSize = 0f
        private var strokeCircleWidth = 0f
        private var builderMessageView: View = CoachmarkMessageView(context)
        fun setTargetView(view: View?): Builder {
            targetView = view
            return this
        }

        /**
         * gravity CoachmarkView
         *
         * @param gravity it should be one type of Gravity enum.
         */
        fun setGravity(gravity: Gravity?): Builder {
            this.gravity = gravity
            return this
        }

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        fun setDismissType(dismissType: DismissType?): Builder {
            this.dismissType = dismissType
            return this
        }

        fun setMessageView(view: View): Builder {
            builderMessageView = view
            return this
        }

        /**
         * this method defining the type of pointer
         *
         * @param pointerType should be one type of PointerType enum. for example: arrow -> To show arrow pointing to target view
         */
        fun setPointerType(pointerType: PointerType?): Builder {
            this.pointerType = pointerType
            return this
        }

        fun build(): CoachmarkView {
            val coachmarkView = CoachmarkView(context, targetView)
            coachmarkView.mGravity = if (gravity != null) gravity else Gravity.auto
            coachmarkView.dismissType = if (dismissType != null) dismissType else DismissType.targetView
            coachmarkView.pointerType = if (pointerType != null) pointerType else PointerType.circle
            coachmarkView.setMessageViewToBeShown(builderMessageView)
            val density = context.resources.displayMetrics.density
            if (coachmarkListener != null) {
                coachmarkView.mCoachmarkListener = coachmarkListener
            }
            if (lineIndicatorHeight != 0f) {
                coachmarkView.indicatorHeight = lineIndicatorHeight * density
            }
            if (lineIndicatorWidthSize != 0f) {
                coachmarkView.lineIndicatorWidthSize = lineIndicatorWidthSize * density
            }
            if (circleIndicatorSize != 0f) {
                coachmarkView.circleIndicatorSize = circleIndicatorSize * density
            }
            if (circleInnerIndicatorSize != 0f) {
                coachmarkView.circleInnerIndicatorSize = circleInnerIndicatorSize * density
            }
            if (strokeCircleWidth != 0f) {
                coachmarkView.strokeCircleWidth = strokeCircleWidth * density
            }
            return coachmarkView
        }
    }

    companion object {
        private const val INDICATOR_HEIGHT = 19
        private const val MESSAGE_VIEW_PADDING = 5
        private const val SIZE_ANIMATION_DURATION = 1
        private const val APPEARING_ANIMATION_DURATION = 1
        private const val CIRCLE_INDICATOR_SIZE = 6
        private const val LINE_INDICATOR_WIDTH_SIZE = 3
        private const val STROKE_CIRCLE_INDICATOR_SIZE = 3
        private const val RADIUS_SIZE_TARGET_RECT = 15
        private const val MARGIN_INDICATOR = 20
        private const val BACKGROUND_COLOR = -0x67000000
        private const val CIRCLE_INNER_INDICATOR_COLOR = -0x333334
        private const val CIRCLE_INDICATOR_COLOR = Color.WHITE
        private const val LINE_INDICATOR_COLOR = Color.WHITE
        private const val CHILD_VIEW_MARGIN = 20
    }
}