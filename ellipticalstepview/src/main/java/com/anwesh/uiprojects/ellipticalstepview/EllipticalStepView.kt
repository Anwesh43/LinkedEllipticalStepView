package com.anwesh.uiprojects.ellipticalstepview

/**
 * Created by anweshmishra on 08/10/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.content.Context

val nodes : Int = 5

fun Canvas.drawESNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val wSize : Float = 3 * gap / 4
    paint.color = Color.parseColor("#43A047")
    paint.strokeWidth = Math.min(w, h) / 60
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap + i * gap, h/2)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * j
        val sc : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f * j)) * 2
        save()
        scale(1f, sf)
        drawArc(RectF(-wSize/2, -wSize/3, wSize/2, wSize/3), 180f, 180f * sc, false, paint)
        restore()
    }
    restore()
}

class EllipticalStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            this.scale += 0.05f * this.dir
            if (Math.abs(this.scale - this.prevScale) > 1) {
                this.scale = this.prevScale + this.dir
                this.dir = 0f
                this.prevScale = this.scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ESNode(var i : Int, val state : State = State()) {

        private var prev : ESNode? = null
        private var next : ESNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = ESNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawESNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ESNode {
            var curr : ESNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class EllipticalStep(var i : Int) {

        private var curr : ESNode = ESNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : EllipticalStepView) {

        private val animator : Animator = Animator(view)
        private val es : EllipticalStep = EllipticalStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            es.draw(canvas, paint)
            animator.animate {
                es.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            es.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : EllipticalStepView {
            val view : EllipticalStepView = EllipticalStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}