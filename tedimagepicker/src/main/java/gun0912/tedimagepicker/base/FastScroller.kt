package gun0912.tedimagepicker.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firefly.fire_rx.FireObservable.Companion.subscribeOnIOAndObserveOnMain
import gun0912.tedimagepicker.R
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    private lateinit var mViewScroller: FrameLayout
    private lateinit var mViewBubble: FrameLayout
    private lateinit var mTextViewBubble: TextView

    var recyclerView: RecyclerView? = null
        set(value) {
            field = value
            field?.run {
                addOnScrollListener(scrollListener)
            }
        }

    private val scrollListener = ScrollListener()
    private var viewHeight: Int = 0
    private val hideScrollerSubject = PublishSubject.create<Boolean>()

    private var currentAnimator: AnimatorSet? = null
    private var hideDisposable: Disposable? = null

    init {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL
        clipChildren = false
        val view = LayoutInflater.from(context).inflate(R.layout.layout_scroller, this, true)
        mViewScroller = view.findViewById(R.id.view_scroller)
        mViewBubble = view.findViewById(R.id.view_bubble)
        mTextViewBubble = view.findViewById(R.id.tv_bubble)
        setupHideScrollerSubscribe()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchScroller(event)) {
                    mViewScroller.isSelected = true
                    showBubble()
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mViewScroller.isSelected) {
                    showScroller(event)
                    hideScrollerSubject.onNext(true)
                    true
                } else {
                    false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mViewScroller.isSelected = false
                hideBubble()
                return false
            }
            else -> super.onTouchEvent(event)

        }
    }

    private fun isTouchScroller(event: MotionEvent): Boolean {
        val scrollerRect = Rect().apply {
            mViewScroller.getHitRect(this)
        }
        return scrollerRect.contains(event.x.toInt(), event.y.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewHeight = h
    }


    private fun setupHideScrollerSubscribe() {
        hideDisposable = hideScrollerSubject.debounce(HIDE_DELAY_SECOND, TimeUnit.SECONDS)
            .subscribeOnIOAndObserveOnMain()
            .filter { !mViewScroller.isSelected }
            .subscribe({
                hideAnimateHandle()
            }, { throwable -> throwable.printStackTrace() })
    }


    private fun showScroller(event: MotionEvent) {
        currentAnimator?.cancel()

        setScrollerPosition(event.y)
        setRecyclerViewPosition(event.y)
    }


    private fun setScrollerPosition(positionY: Float) {
        mViewScroller.y = getValueInRange(
            positionY - (mViewScroller.height / 2),
            viewHeight - mViewScroller.height
        )

        mViewBubble.y = getValueInRange(
            positionY - (mViewBubble.height / 2),
            viewHeight - mViewBubble.height
        )
    }

    private fun setRecyclerViewPosition(positionY: Float) {
        recyclerView?.adapter?.run {
            val proportion: Float = when {
                mViewScroller.y == 0f -> 0f
                mViewScroller.y + mViewScroller.height >= viewHeight - SCROLLER_MAX_POSITION_GAP -> 1f
                else -> positionY / viewHeight
            }
            val targetPos: Float = getValueInRange(proportion * itemCount, itemCount - 1)

            (recyclerView?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                targetPos.roundToInt(),
                0
            )
        }

    }

    private fun getValueInRange(value: Float, max: Int): Float = value.coerceIn(0f, max.toFloat())

    private fun showAnimateHandle() {
        if (mViewScroller.visibility == View.VISIBLE) {
            return
        }
        mViewScroller.visibility = View.VISIBLE

        ObjectAnimator.ofFloat(
            mViewScroller,
            TRANSLATION_X,
            mViewScroller.width.toFloat(),
            0f
        ).apply {
            duration = ANIMATION_TIME_HANDLE
        }.start()
    }


    private fun hideAnimateHandle() {
        if (mViewScroller.visibility == View.INVISIBLE) {
            return
        }

        ObjectAnimator.ofFloat(
            mViewScroller,
            TRANSLATION_X,
            0f,
            mViewScroller.width.toFloat()
        ).apply {

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mViewScroller.visibility = View.INVISIBLE
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    mViewScroller.visibility = View.INVISIBLE
                    currentAnimator = null
                }
            })

            duration = ANIMATION_TIME_HANDLE
        }.start()

    }


    private fun showBubble() {
        if (mViewBubble.visibility == View.VISIBLE) {
            return
        }
        mViewBubble.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(
            mViewBubble,
            TRANSLATION_X,
            mViewBubble.width.toFloat(),
            0f
        ).apply {
            duration = ANIMATION_TIME_BUBBLE
        }.start()
    }

    private fun hideBubble() {
        if (mViewBubble.visibility == View.INVISIBLE) {
            return
        }
        ObjectAnimator.ofFloat(
            mViewBubble,
            TRANSLATION_X,
            0f,
            mViewBubble.width.toFloat()
        ).apply {

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mViewBubble.visibility = View.INVISIBLE

                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    mViewBubble.visibility = View.INVISIBLE
                }
            })
            duration = ANIMATION_TIME_BUBBLE
        }.start()
    }

    override fun onDetachedFromWindow() {
        recyclerView?.removeOnScrollListener(scrollListener)
        hideDisposable?.dispose()
        super.onDetachedFromWindow()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (dy == 0) {
                return
            }

            (rv.layoutManager as? LinearLayoutManager)?.run {
                if (mViewScroller.visibility == View.INVISIBLE) {
                    showAnimateHandle()
                }

                updateBubbleAndHandlePosition()
                hideScrollerSubject.onNext(true)
            }
        }

    }


    private fun updateBubbleAndHandlePosition() {
        if (mViewScroller.isSelected) {
            return
        }
        recyclerView?.let {
            val verticalScrollOffset = it.computeVerticalScrollOffset()
            val verticalScrollRange = it.computeVerticalScrollRange()
            val proportion =
                verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - viewHeight)
            setScrollerPosition(viewHeight * proportion)
        }

    }

    fun setBubbleText(text: String) {
        mTextViewBubble.text = text
    }

    companion object {
        private const val HIDE_DELAY_SECOND: Long = 1
        private const val SCROLLER_MAX_POSITION_GAP: Long = 5
        private const val TRANSLATION_X = "translationX"
        private const val ANIMATION_TIME_HANDLE = 400L
        private const val ANIMATION_TIME_BUBBLE = 150L
    }
}