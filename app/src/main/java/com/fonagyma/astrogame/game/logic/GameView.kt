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
    private var drawables = ArrayList<Drawable>()
    private var obstacles = ArrayList<GObject>()
    private var attacks = ArrayList<GObject>()
    private var weaponSystem : WeaponSystem

    init{
        paint.strokeWidth=5f
        paint.color= Color.argb(255,255,125,125)
        pseRect = RectF(buttonMargin,buttonMargin,buttonHeight+buttonMargin, buttonHeight+buttonMargin)
        weaponSystem = BomberWithJoystick(context, PointF(.3f*surfaceSize.x,.7f*surfaceSize.y),PointF(.6f*surfaceSize.x,.7f*surfaceSize.y),surfaceSize)
        drawables.add(Drawable(context, R.drawable.test_100_100,0f,1f,1f,.5f,.5f,50f,50f))
        drawables.add(Drawable(context, R.drawable.test_200_200,30f,1f,1f,.5f,.5f,50f,50f))
        drawables.add(Drawable(context, R.drawable.test_300_100,60f,1f,1f,.5f,.5f,50f,50f))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.2f,surfaceSize.y*.2f),PointF(0f,0f),PointF(0f,0f),1f), surfaceSize,30f,5))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.5f,surfaceSize.y*.2f),PointF(5f,5f),PointF(0f,0f),1f),surfaceSize,30f,5))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.2f,surfaceSize.y*.3f),PointF(10f,0f),PointF(0f,0f),1f),surfaceSize,50f,5))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.3f,surfaceSize.y*.5f),PointF(0f,10f),PointF(0f,0f),1f),surfaceSize,100f,5))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.1f,surfaceSize.y*.5f),PointF(0f,0f),PointF(0f,0f),1f),surfaceSize,70f,5))
        obstacles.add(Meteor(context, PhysicalState(PointF(surfaceSize.x*.2f,surfaceSize.y*.4f),PointF(10f,5f),PointF(0f,0f),1f),surfaceSize,20f,5))

    }


    override fun run(){
        while(drawing){
            val frameStartTime = System.currentTimeMillis()
            millis = if(lastFrameMillis>0){
                frameStartTime- lastFrameMillis
            }else{
                0
            }
            lastFrameMillis=frameStartTime
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
            for (d in drawables){
                d.draw(canvas,paint,PointF(surfaceSize.x/2,surfaceSize.y/2))
            }

            for(o in obstacles){
                o.draw(canvas,paint)
            }
            for(a in attacks){
                a.draw(canvas,paint)
            }

            weaponSystem.draw(canvas,paint)

            canvas.drawCircle(surfaceSize.x/2,surfaceSize.y/2,5f,paint)

            holder.unlockCanvasAndPost(canvas)
        }
    }
    private fun update(millis: Long){
        Log.d("millis", "$millis")
        for (d in drawables){
            d.rotation+=2f
        }
        weaponSystem.update(millis)

        if (weaponSystem.timerUntilNextAttack.get()<0){
            if (weaponSystem.actionType){
                weaponSystem.actionTypeAttack(obstacles)
            }
            if (weaponSystem.objectType){
                attacks.add(weaponSystem.objectTypeAttack())
            }
            weaponSystem.timerUntilNextAttack.increase(weaponSystem.reloadTimeMillis)
        }

        for (o in obstacles){
            o.update(millis)
        }
        for (a in attacks){
            a.update(millis)
            for (o in obstacles){

                if (a.exists && o.exists && a.collider.intersects(o.collider)){
                    a.onCollide(o)
                    o.onCollide(a)
                }
            }
        }

        ///garbage collection may be faster by reusing old ones
        var newList = ArrayList<GObject>()
        for (o in obstacles){
            if(o.exists)
            {
                newList.add(o)
            }
        }
        obstacles=newList
        newList.clear()
        for (a in attacks){
            if(a.exists)
            {
                newList.add(a)
            }
        }
        attacks=newList

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
            if (!paused){
                weaponSystem.control(arrayListOf(PointF(motionEvent.x,motionEvent.y)))
            }
        }
        // Did the user touch the screen
        if (motionEvent.action and MotionEvent.ACTION_MASK ==
            MotionEvent.ACTION_DOWN) {
            if (!paused){
                weaponSystem.control(arrayListOf(PointF(motionEvent.x,motionEvent.y)))
            }
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