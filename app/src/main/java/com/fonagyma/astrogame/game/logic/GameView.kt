package com.fonagyma.astrogame.game.logic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import com.fonagyma.astrogame.R

class GameView(context: Context,width: Int, height: Int): SurfaceView(context), Runnable {
    private lateinit var thread: Thread
    private lateinit var canvas: Canvas
    private var surfaceSize = PointF(width.toFloat(),height.toFloat())
    private var paint = Paint()
    @Volatile
    private var drawing = false
    private var paused = false
    private var lastFrameMillis : Long = 0
    private var millis: Long= 0
    private var fps: Long = 0
    private var gameTimer = Timer(0)
    private var pseRect: RectF
    private var buttonMargin = 100f
    private var buttonHeight = 100f
    private var drawable : Drawable
    init{
        paint.strokeWidth=5f
        paint.color= Color.argb(255,255,125,125)
        pseRect = RectF(buttonMargin,buttonMargin,buttonHeight+buttonMargin, buttonHeight+buttonMargin)
        drawable= Drawable(context, R.drawable.astroid1,120f,1f,1f,.2f,.2f)
    }


    override fun run(){
        while(drawing){
            val frameStartTime = System.currentTimeMillis()
            millis = if(lastFrameMillis>0){
                frameStartTime- lastFrameMillis
            }else{
                0
            }
            if(!paused){
                update(millis)
            }
            draw()
            if(millis>0){
                fps=1000/millis
            }
        }
    }
    private fun draw(){
        if(holder.surface.isValid){
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.argb(255,255,255,255))

            paint.style=Paint.Style.STROKE
            canvas.drawRect(pseRect,paint)

            paint.style=Paint.Style.FILL
            drawable.draw(canvas,paint,PointF(surfaceSize.x/2,surfaceSize.y/2))
            canvas.drawCircle(surfaceSize.x/2,surfaceSize.y/2,5f,paint)

            holder.unlockCanvasAndPost(canvas)
        }
    }
    private fun update(millis: Long){
        drawable.rotation+=2f
    }
    fun pause() {
        // Set drawing to false
        // Stopping the thread isn't
        // always instant
        drawing = false
        try {
            // Stop the thread
            thread.join()
            lastFrameMillis=0
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }
        lastFrameMillis=0

    }

    fun resume() {
        drawing = true
        // Initialize the instance of Thread
        thread = Thread(this)
        // Start the thread
        thread.start()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(
        motionEvent: MotionEvent
    ): Boolean {
        if (motionEvent.action and MotionEvent.
            ACTION_MASK ==
            MotionEvent.ACTION_MOVE) {
            //for sliding
        }
        // Did the user touch the screen
        if (motionEvent.action and MotionEvent.ACTION_MASK ==
            MotionEvent.ACTION_DOWN) {
            if(pseRect.contains(motionEvent.x,motionEvent.y)){
                if (paused)
                {
                    paused=false
                    resume()
                }else{
                    paused=true
                    pause()
                }
            }
        }
        return true
    }


}