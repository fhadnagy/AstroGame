package com.fonagyma.astrogame.game.logic


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.math.roundToInt
import kotlin.random.Random
import com.fonagyma.astrogame.R


@SuppressLint("ViewConstructor")
class OLDLiveDrawingView(context: Context, mScreenX : Int, mScreenY: Int): SurfaceView(context) , Runnable{
    private val debugging = true
    private lateinit var canvas: Canvas
    private val paint: Paint = Paint()
    private var fps: Long = 0
    private var gameTimeMillis :Long = 0
    private var msPassed= System.currentTimeMillis()
    private var prevFrameMillis: Long = 0
    private val millisInSecond: Long = 1000
    private val fontSize: Int = mScreenX /15
    private lateinit var thread: Thread
    private var gameOver = false
    private val walls : PointF
    private var prevHp : Int
    private var hp : Int
    private var hpmax : Int
    private var earthH : Float = .95f
    @Volatile
    private var drawing: Boolean = false
    private var paused = false
    private var counterA = 0
    private var counterB = 0
    private var clickableList = ArrayList<OLDClickable>()
    private var drawables = ArrayList<OLDGameObject>()
    private var collidables = ArrayList<OLDCollidable>()
    private var js : OLDJoystick
    private var cnn : OLDCannon
    private var pseRect = RectF(10f,10f,100f,100f)
    private var tryAgainRect = RectF(10f,10f,100f,100f)
    private var highScore = 0
    private val random = Random(System.currentTimeMillis())
    private var score :Int = 0

    private var hpToMoney = 5f
    private var asteroidInterval : Long = 1400
    private var asteroidHpBase : Int = 1
    private var rocketInterval : Long = 800
    private var reloadSpeed : Float = 1f
    private var currencyING : Int = 100

    private var rocketSize : Float = 1f
    private var rocketDamage : Float = 1f
    private var expRadius : Float = 1f
    private var expDamage : Float = 1f
    private var rocketSpeed : Float = 1f
    private var rocketInaccuracy : Float = 1f

    private var rocketSizeBase : Float = 12f
    private var rocketDamageBase : Float = 1f
    private var expRadiusBase : Float = 40f
    private var expDamgeBase :  Float = 1f
    private var rocketSpeedBase : Float = 300f
    private var rocketInaccuracyBase : Float = 2f
    private var btnHeight = 10f
    private var buttonMargin = 20f
    private var buttonStartPos= PointF()

    ///var mP= PointF(mScreenX.toFloat()/2,mScreenY.toFloat()/2)
    init {

        clickableList.add(OLDJoystick(PointF(mScreenX*.4f,mScreenY*.5f),RectF(mScreenX*.4f,mScreenY*.5f,mScreenX-5f,mScreenY-5f),context))
        drawables.add(OLDCannon(PointF(mScreenX*.3f,mScreenY*.8f),context))
        js = clickableList[0] as OLDJoystick
        cnn = drawables[0] as OLDCannon
        cnn.rocketStartS = rocketSpeedBase*rocketSpeed
        walls = PointF(mScreenX.toFloat(),mScreenY.toFloat())
        btnHeight= walls.y/18
        buttonMargin= walls.y/80
        buttonStartPos.x= walls.x-btnHeight-buttonMargin
        buttonStartPos.y= 0f+buttonMargin

        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(0*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(0*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(0*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.rocket_speed,.4f,.4f,rocketSpeed,.25f))
        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(1*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(1*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(1*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.rocket_damage,.4f,.4f,rocketDamage,1f))
        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(2*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(2*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(2*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.explosion_damage,.4f,.4f,expDamage,1f))
        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(3*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(3*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(3*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.explosion_radius,.4f,.4f,expRadius,.5f))
        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(4*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(4*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(4*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.reload_speed,.4f,.4f,reloadSpeed,.125f))
        clickableList.add(OLDCounterButton(PointF(buttonStartPos.x,buttonStartPos.y+(5*(buttonMargin+btnHeight))+buttonMargin*2),RectF(buttonStartPos.x,buttonStartPos.y+(5*(buttonMargin+btnHeight))+buttonMargin*2,buttonStartPos.x+btnHeight,buttonStartPos.y+btnHeight+(5*(buttonMargin+btnHeight))+buttonMargin*2),context,R.drawable.accuracy,.4f,.4f,rocketInaccuracy,.125f))

        hpmax = 50
        hp = hpmax
        prevHp = hpmax
        pseRect = RectF(buttonMargin,buttonMargin,btnHeight+buttonMargin, btnHeight+buttonMargin)
        tryAgainRect = RectF(buttonMargin,buttonMargin,btnHeight*2+buttonMargin, btnHeight*2+buttonMargin)

        //test asteroids
        /*collidables.add(Astroid(PointF(mScreenX*.5f,mScreenY*.5f),
            context, PointF(.01f,0f), 216f, 60f, walls))
        collidables.add(Astroid(PointF(mScreenX*.4f,mScreenY*.6f),
            context, PointF(.01f,0f), 64f, 40f, walls))
        collidables.add(Astroid(PointF(mScreenX*.3f,mScreenY*.7f),
            context, PointF(.01f,0f), 27f, 30f, walls))*/
    }
    private fun draw(){
        if (holder.surface.isValid) {
            // Lock the canvas (graphics memory) ready to draw
            canvas = holder.lockCanvas()
            if(!gameOver){
            // Fill the screen with a solid color
            canvas.drawColor(Color.argb(255, 50, 50, 150))
            // Choose a color to paint with
            paint.color = Color.argb(255, 25, 255, 25)
            // Choose the font size
            paint.textSize = fontSize.toFloat()
                paint.strokeWidth= 10f

            canvas.drawLine(0f, walls.y * earthH, walls.x, walls.y * earthH, paint)
            // Draw what needs drawing
            for (cl in clickableList) {
                cl.draw(canvas, paint)
            }

            for (co in collidables) {
                co.draw(canvas, paint)
            }

            for (go in drawables) {
                go.draw(canvas, paint)
            }

            paint.color = Color.argb(255, 255, 0, 0)

            paint.style = Paint.Style.STROKE
            canvas.drawRect(pseRect, paint)
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 6f

            if (!paused) {
                canvas.drawRect(
                    pseRect.left + pseRect.width() * .25f,
                    pseRect.top + pseRect.height() * .2f,
                    pseRect.right - pseRect.width() * .6f,
                    pseRect.bottom - pseRect.height() * .2f,
                    paint
                )
                canvas.drawRect(
                    pseRect.left + pseRect.width() * .6f,
                    pseRect.top + pseRect.height() * .2f,
                    pseRect.right - pseRect.width() * .25f,
                    pseRect.bottom - pseRect.height() * .2f,
                    paint
                )
                paint.color = Color.argb(255, 255, 0, 0)
            } else {
                canvas.drawLine(
                    pseRect.left + pseRect.width() * .3f,
                    pseRect.top + pseRect.height() * .2f,
                    pseRect.right - pseRect.width() * .2f,
                    pseRect.bottom - pseRect.height() * .5f,
                    paint
                )
                canvas.drawLine(
                    pseRect.left + pseRect.width() * .3f,
                    pseRect.top + pseRect.height() * .8f,
                    pseRect.right - pseRect.width() * .2f,
                    pseRect.bottom - pseRect.height() * .5f,
                    paint
                )
                canvas.drawLine(
                    pseRect.left + pseRect.width() * .3f,
                    pseRect.top + pseRect.height() * .2f,
                    pseRect.left + pseRect.width() * .3f,
                    pseRect.top + pseRect.height() * .8f,
                    paint
                )

            }

            paint.strokeWidth = 2f


            paint.color = Color.argb(255, 25, 255, 25)
            paint.textSize = buttonMargin*3
            canvas.drawText(
                "<$score>",
                walls.x*.45f, (earthH+1f)/2f*walls.y+buttonMargin, paint
            )
            paint.color = Color.argb(255, 255, 25, 25)
            canvas.drawText(
                "<$hp>/<$hpmax>",
                 buttonMargin, (earthH+1f)/2f*walls.y+buttonMargin, paint
            )
                paint.textSize = buttonMargin*2
                paint.color = Color.argb(255, 0, 255, 255)
            canvas.drawText(
                "$currencyING",
                walls.x-buttonMargin*3-btnHeight, buttonMargin*2, paint
            )

        }else{
                canvas.drawColor(Color.argb(255, 75, 75, 152))
                // Choose a color to paint with
                paint.color = Color.argb(255, 25, 255, 25)
                // Choose the font size
                paint.textSize = fontSize.toFloat()

                canvas.drawRect(tryAgainRect, paint)
                val matrix = Matrix()
                matrix.preScale(btnHeight/100f*.4f,btnHeight/100f*.4f)
                val bbbb= BitmapFactory.decodeResource(context.resources,R.drawable.again)
                canvas.drawBitmap( Bitmap.createBitmap(bbbb,0,0,bbbb.height,bbbb.width,matrix,true),  20f,20f,paint)

                paint.color = Color.argb(255, 255, 75, 75)
                paint.textSize = 80f
                paint.strokeWidth = 20f

                canvas.drawText(
                    "GAME OVER",
                    walls.x/2f - 220f, walls.y/2f-100f, paint
                )

                paint.textSize = 40f
                paint.strokeWidth = 6f

                paint.color = Color.argb(255, 255, 255, 25)

                canvas.drawText(
                    "Highscore(Prev): <$highScore>",
                    walls.x/2f - 220f, walls.y/2f, paint
                )

                paint.color = Color.argb(255, 25, 255, 25)

                canvas.drawText(
                    "Your Score: <$score>",
                    walls.x/2f - 220f, walls.y/2f+100f, paint
                )


                paint.strokeWidth = 8f
                paint.textSize = fontSize.toFloat()


            }

            if(hp<prevHp)
            {
                canvas.drawColor(Color.argb(80,255,0,0))
                prevHp=hp
            }

            // Display the drawing on screen
            // unlockCanvasAndPost is a
            // function of SurfaceHolder
            holder.unlockCanvasAndPost(canvas)
        }
    }
    private fun printDebuggingText() {
        val debugSize = fontSize / 2
        val debugStart = 150
        paint.color = Color.argb(255,25,255,25)
        paint.textSize = debugSize.toFloat()
        canvas.drawText("fps: $fps",
            10f, (debugStart + debugSize).toFloat(), paint)
        canvas.drawText("No ${collidables.size}", 10f, (debugStart + debugSize*2f).toFloat(), paint)
        canvas.drawText("time: ${gameTimeMillis/1000}", 10f, (debugStart + debugSize*3f).toFloat(), paint)
        canvas.drawText("A: ${counterA}", 10f, (debugStart + debugSize*4f).toFloat(), paint)
        canvas.drawText("INGC: ${currencyING}", 10f, (debugStart + debugSize*5f).toFloat(), paint)
    }
    override fun run() {
        // The drawing Boolean gives us finer control
        // rather than just relying on the calls to run
        // drawing must be true AND
        // the thread running for the main
        // loop to execute
        while (drawing) {
            // What time is it now at the
            // start of the loop?
            val frameStartTime = System.currentTimeMillis()
            if (prevFrameMillis>0){
                msPassed = frameStartTime-prevFrameMillis
            }else{
                msPassed = 0
            }
            //getting millis passed as a more accurate time state instead of relying on fps
            prevFrameMillis = frameStartTime
            gameTimeMillis+=msPassed

            if (gameOver){
                msPassed=0
                gameTimeMillis=0
            }

            // Provided the app isn't paused
            // call the update function
            if (!paused) {
                update()
            }
            // The movement has been handled
            // we can draw the scene.
            draw()
            // How long did this frame/loop take?
            // Store the answer in timeThisFrame
            val timeThisFrame = System.currentTimeMillis() - frameStartTime
            // Make sure timeThisFrame is
            // at least 1 millisecond
            // because accidentally dividing
            // by zero crashes the app
            if (timeThisFrame > 0) {
                // Store the current frame rate in fps
                // ready to pass to the update functions of
                // of our particles in the next frame/loop
                fps = millisInSecond / timeThisFrame
            }

        }

    }
    private fun update() {
        rocketSpeed=(clickableList[1] as OLDCounterButton).counter
        rocketDamage=(clickableList[2] as OLDCounterButton).counter
        expDamage=(clickableList[3] as OLDCounterButton).counter
        expRadius=(clickableList[4] as OLDCounterButton).counter
        reloadSpeed=(clickableList[5] as OLDCounterButton).counter
        cnn.rocketStartS=rocketSpeedBase*rocketSpeed
        if(!paused && !gameOver){
            if (collidables.size>0){
                for(a in 0..collidables.size-2){
                    for(b in a+1..collidables.size-1){
                        if(collidables[a].collides(collidables[b])){
                            collidables[a].onCollide(collidables[b])
                        }
                    }
                }
            }

            for (cl in clickableList){
                cl.update(msPassed)
            }
            for(go in drawables){
                go.update(msPassed,js.rotation)
            }
            for(co in collidables){
                co.update(msPassed,js.rotation)
            }
            if (gameTimeMillis/asteroidInterval > counterA){
                counterA++
                Log.d("gtms","$gameTimeMillis")

                collidables.add(OLDAsteroid(PointF(walls.x*(0.2f+random.nextFloat()*.6f), walls.y*(0.02f+random.nextFloat()*.03f)),
                        context, PointF(-1f+random.nextFloat()*2f,2f+random.nextFloat()*2f),2f, 40f, walls,asteroidHpBase*(counterA/10 + 1)))
            }
            if ((gameTimeMillis*reloadSpeed).toLong()/rocketInterval > counterB){
                counterB++
                Log.d("gtms","$gameTimeMillis")

                collidables.add(OLDRocket(PointF(cnn.position.x+cnn.rocketStartP.x,cnn.position.y+cnn.rocketStartP.y ),
                    context, cnn.rocketStartV,rocketSize*rocketSizeBase, walls,(rocketDamageBase*rocketDamage).toInt(), cnn.rotation,rocketInaccuracyBase/rocketInaccuracy*rocketSpeed))
            }

        //deletes
        val tempCollidables = ArrayList<OLDCollidable>()
        for(a in collidables){
                if (a.exists)
                {

                    if (a.type==1 && a.position.y>walls.y*earthH)
                    {
                        a.exists = false
                        val astr = a as OLDAsteroid
                        hp-= astr.hp
                    }else{
                        tempCollidables.add(a)
                    }
                }else if(a.destroyed){
                    score+= a.pointsOnDestruction
                    currencyING+= (a.pointsOnDestruction*hpToMoney).roundToInt()
                    if (a.type==2 || a.type==4)
                    {
                        tempCollidables.add(OLDExplosion(a.position,context, PointF(0f,0f),expRadius*expRadiusBase,(expDamgeBase*expDamage).toInt()))
                    }
                }
        }
        collidables= tempCollidables

            if (hp<1)
            {
                gameOver=true
            }
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
            prevFrameMillis=0
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }
        prevFrameMillis=0

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
                if (js.hitBox.contains(motionEvent.x,motionEvent.y))
                    js.onClick(PointF(motionEvent.x,motionEvent.y))
        }
        // Did the user touch the screen
        if (motionEvent.action and MotionEvent.ACTION_MASK ==
            MotionEvent.ACTION_DOWN) {
            if(!paused && !gameOver)
            {
                for (cl in clickableList) {
                    if(cl.hitBox.contains(motionEvent.x,motionEvent.y))
                    {
                        if (cl.upgradecost<=currencyING)
                        {
                            currencyING-=cl.upgradecost
                            cl.onClick(PointF(motionEvent.x,motionEvent.y))
                        }
                    }
                }
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
            if(gameOver && tryAgainRect.contains(motionEvent.x,motionEvent.y)){
                hp = hpmax
                prevHp = hpmax
                if (score> highScore)
                {
                    highScore = score
                }
                prevFrameMillis=0
                score =0
                currencyING = 0
                counterA = 0
                counterB = 0
                (clickableList[1] as OLDCounterButton).reset()
                (clickableList[2] as OLDCounterButton).reset()
                (clickableList[3] as OLDCounterButton).reset()
                (clickableList[4] as OLDCounterButton).reset()
                (clickableList[5] as OLDCounterButton).reset()
                (clickableList[6] as OLDCounterButton).reset()

                collidables.clear()
                gameOver = false

            }
        }
        return true
    }
}