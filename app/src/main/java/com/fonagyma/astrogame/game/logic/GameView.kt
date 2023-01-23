package com.fonagyma.astrogame.game.logic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class GameView(context: Context,width: Int, height: Int): SurfaceView(context), Runnable {
    private lateinit var thread: Thread
    private lateinit var canvas: Canvas
    private var ratioHeight = height.toFloat()/70f
    private var random = Random(System.currentTimeMillis())
    private var surfaceSize = PointF(width.toFloat(),height.toFloat())
    private var paint = Paint()
    @Volatile
    private var drawing = false
    private var paused = false

    private var lastFrameMillis : Long = 0
    private var millis: Long= 0
    private var fps: Long = 0
    private var gameTimer = Timer(0)
    private var obstacleLimit = 10
    private var newObstacleInterval : Long = 3200
    private var timerUntilNextObstacle = Timer(newObstacleInterval)

    private var score: Long = 0
    private var maxHealth: Int = 20
    private var health: Int = maxHealth
    private var crystals: Long = 0
    private var gears: Long = 0

    private var pseRect: RectF
    private var buttonMargin = ratioHeight
    private var buttonHeight = ratioHeight*3f
    private var drawables = ArrayList<Drawable>()
    private var obstacles = ArrayList<GObject>()
    private var attacks = ArrayList<GObject>()
    private var weaponSystem : WeaponSystem
    private var earthRadius= ratioHeight*25f
    private var earthCenterPointF = PointF(surfaceSize.x/2, surfaceSize.y+earthRadius*.82f)
    private var earthCollider = Collider(earthCenterPointF,earthRadius)

    //TODO: Buttons for a new screen, resource info, ability status | maybe link
    // or implement the upgrades into the weapon systems


    init{
        paint.textSize=ratioHeight
        paint.strokeWidth=5f
        paint.color= Color.argb(255,255,125,125)
        pseRect = RectF(buttonMargin,buttonMargin,buttonHeight+buttonMargin, buttonHeight+buttonMargin)
        weaponSystem = BomberWithJoystick(context, PointF(.3f*surfaceSize.x,.7f*surfaceSize.y),PointF(.6f*surfaceSize.x,.7f*surfaceSize.y),surfaceSize)
        //drawables.add(Drawable(context, R.drawable.test_100_100,0f,1f,1f,.5f,.5f,50f,50f))
        //drawables.add(Drawable(context, R.drawable.test_200_200,30f,1f,1f,.5f,.5f,50f,50f))
        //drawables.add(Drawable(context, R.drawable.test_300_100,60f,1f,1f,.5f,.5f,50f,50f))

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
            draw()
            if(!paused){
                update(millis)
            }
            if(millis>0){
                fps=1000/millis
            }
        }
    }
    private fun draw(){
        if(holder.surface.isValid){
            if(!paused) {
                //TODO: needs a timer up top, a wave counter and a score counter
                // a health bar up top
                canvas = holder.lockCanvas()

                canvas.drawColor(Color.argb(255, 255, 255, 255))
                paint.color= Color.argb(255,255,15,125)
                canvas.drawCircle(earthCenterPointF.x,earthCenterPointF.y,earthRadius,paint)

                paint.style = Paint.Style.STROKE
                canvas.drawRect(pseRect, paint)

                paint.style = Paint.Style.FILL
                for (d in drawables) {
                    d.draw(canvas, paint, PointF(surfaceSize.x / 2, surfaceSize.y / 2))
                }

                for (o in obstacles) {
                    o.draw(canvas, paint)
                }
                for (a in attacks) {
                    a.draw(canvas, paint)
                }

                //TODO: action type attacks, maybe helpers, a sidebar for the current status effects/boosts

                weaponSystem.draw(canvas, paint)

                canvas.drawCircle(surfaceSize.x / 2, surfaceSize.y / 2, 5f, paint)

                //info
                canvas.drawText("fps: $fps\n score: $score", 200f, 50f, paint)
            }else{
                //TODO: this is for more utility (upgrades, ability select,
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }
    private fun update(millis: Long){

        Log.d("millis", "$millis")

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


        for (a in attacks){
            a.update(millis)
            for (o in obstacles){

                if (a.exists && o.exists && a.collider.intersects(o.collider)){
                    a.onCollide(o)
                    o.onCollide(a)
                }
            }
        }

        for (o in obstacles){
            o.update(millis)
            if (o.exists && o.collider.intersects(earthCollider))
            {
                if (o.typeID==-1)
                {
                    health-=(o as Meteor).health
                }
                o.exists= false
                o.wasDestroyed=true
            }
        }
        //TODO: garbage collection may be faster by reusing old ones
        val newList = ArrayList<GObject>()
        for (o in obstacles){
            if(o.exists)
            {
                newList.add(o)
            }else if(o.wasDestroyed){
                score+=o.pointsOnDestruction
            }
        }
        obstacles=newList
        val newListB = ArrayList<GObject>()
        for (a in attacks){
            if(a.exists)
            {
                newListB.add(a)
            }else{
                when(a.typeID){
                    1 -> {
                        newListB.add(Explosion(context,PhysicalState(a.physicalState.position,
                            PointF(0f,0f),PointF(0f,0f),1f),2,130,100f))
                    }
                    else -> {

                    }
                }
            }
        }
        attacks=newListB

        //TODO: maybe add was destroyed to bomb so doesn't explode on wall
        //add new meteors
        timerUntilNextObstacle.decrease(millis)
        if (timerUntilNextObstacle.get()<0 && obstacleLimit>=obstacles.size){
            obstacles.add(Meteor(context, newPhysicalForEarth(1f),30f,5))
            timerUntilNextObstacle.increase(newObstacleInterval)
        }


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
            //TODO: add "UI" for utility screen on pause
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
            if (!paused){
                weaponSystem.control(arrayListOf(PointF(motionEvent.x,motionEvent.y)))
            }

        }
        return true
    }
    fun newPhysicalForEarth(speedModifier: Float):PhysicalState{
        val obstacleStartSpeedBase = 120f
        val startPointF = PointF(surfaceSize.x*(-.3f+1.6f*random.nextFloat()),surfaceSize.y*(-.15f))
        val d = sqrt((earthCenterPointF.x-startPointF.x).pow(2)+(earthCenterPointF.y-startPointF.y).pow(2))
        return PhysicalState(startPointF, PointF((earthCenterPointF.x-startPointF.x)*obstacleStartSpeedBase*speedModifier/d,(earthCenterPointF.y-startPointF.y)*obstacleStartSpeedBase*speedModifier/d), PointF(0f,0f),1f)
    }

}