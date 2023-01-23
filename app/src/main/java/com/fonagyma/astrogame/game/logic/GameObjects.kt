package com.fonagyma.astrogame.game.logic

import android.content.Context
import android.graphics.*
import android.util.Log
import com.fonagyma.astrogame.R
import kotlin.math.*
import kotlin.random.Random


class Bomb(context: Context,physicalState: PhysicalState, var border: PointF, size: Float, _damage: Int): GObject(physicalState){
    var damage : Int
    init {
        damage=_damage
        drawable= Drawable(context, R.drawable.bomb,0f,1f,1f, .49f,.51f,size,size)
        collider= Collider(physicalState.position,size)
        typeID=1
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        drawable.draw(canvas,paint,physicalState.position)
        //paint.style= Paint.Style.STROKE
        //paint.color= Color.argb(255,255,15,125)
        //canvas.drawCircle(collider.hitBoxCenter.x,collider.hitBoxCenter.y,collider.hitBoxRadius,paint)
    }

    override fun update(millis: Long) {
        physicalState.update(millis)
        if(physicalState.position.x>border.x || physicalState.position.y>border.y|| physicalState.position.x<0 || physicalState.position.y<0){
            exists = false
        }
    }

    override fun onCollide(otherGObject: GObject) {
        when(otherGObject.typeID){
            -1 -> {
                (otherGObject as Meteor).health -= damage
                exists=false
            }
            else -> return
        }
    }
}

class Explosion(context: Context, physicalState: PhysicalState, var damage : Int, var lifetime: Long, var size: Float) : GObject(physicalState){
    var active = true
    private val rnd = Random(System.currentTimeMillis())
    init {
        typeID = 2
        var turn = rnd.nextFloat()*360f
        collider = Collider(physicalState.position, size)
        drawable = Drawable(context,R.drawable.exp,turn,3f,3f,.4f,.43f,size,size)
    }
    override fun draw(canvas: Canvas, paint: Paint) {
        if(!exists) return
        drawable.draw(canvas,paint,physicalState.position)

        paint.color=Color.argb(255,255,0,0)

        paint.style= Paint.Style.STROKE
        canvas.drawCircle(physicalState.position.x,physicalState.position.y,size,paint)

        paint.style= Paint.Style.FILL

    }

    override fun update(millis: Long) {
        if (lifetime<1){
            exists= false
        }
        active = false
        if(!exists) return

        lifetime-=millis
    }


    override fun onCollide(otherGObject: GObject) {

        if(!active) return
       when(otherGObject.typeID){
           -1 -> {
               (otherGObject as Meteor).health-=damage
           }
           else -> {
           }
       }
    }
}

class Meteor(context: Context,physicalState: PhysicalState,var size: Float,_maxHealth: Int) : GObject(physicalState) {
    var maxHealth : Int
    var health : Int
    init {
        maxHealth= _maxHealth
        pointsOnDestruction= maxHealth
        health= maxHealth
        drawable= Drawable(context, R.drawable.astroid1,0f,2f,2f, .49f,.51f,size,size)
        collider= Collider(physicalState.position,size)
        typeID=-1
    }

    override fun draw(canvas: Canvas, paint: Paint) {

        drawable.draw(canvas,paint,physicalState.position)

       // paint.style= Paint.Style.STROKE
       // paint.color= Color.argb(255,255,15,125)
        //canvas.drawCircle(collider.hitBoxCenter.x,collider.hitBoxCenter.y,collider.hitBoxRadius,paint)

        paint.style= Paint.Style.FILL
        paint.color=Color.argb(255,255,0,0)
        canvas.drawRect(physicalState.position.x-size,physicalState.position.y-size*1.4f,physicalState.position.x+size,physicalState.position.y-size*1.1f,paint)

        paint.color=Color.argb(255,0,255,0)
        canvas.drawRect(physicalState.position.x-size,physicalState.position.y-size*1.4f,physicalState.position.x+2*size*(-.5f+health.toFloat()/maxHealth.toFloat()),physicalState.position.y-size*1.1f,paint)

    }

    override fun update(millis: Long) {
        if (health<=0) {
            exists= false
            wasDestroyed = true
        }
        physicalState.update(millis)
        /*if(physicalState.position.x>border.x || physicalState.position.y>border.y|| physicalState.position.x<0 || physicalState.position.y<0){
            exists = false
        }*/
    }

    override fun onCollide(otherGObject: GObject) {
        when(otherGObject.typeID){
            1 -> {
                health -= (otherGObject as Bomb).damage
                otherGObject.exists=false
            }
            2 -> {
                health -= (otherGObject as Explosion).damage
                otherGObject.exists=false
            }
            else -> return
        }
    }
}

interface WeaponSystem{
    var objectType : Boolean
    var actionType : Boolean
    var reloadTimeMillis : Long
    var timerUntilNextAttack : Timer
    fun objectTypeAttack(): GObject
    fun actionTypeAttack(obstacles: ArrayList<GObject>) : Int
    fun control(points:ArrayList<PointF>)
    fun draw(canvas: Canvas,paint: Paint)
    fun update(millis: Long)
}

class BomberWithJoystick(var context: Context, var weaponHingePointF: PointF,var controlCenterPointF: PointF, var border: PointF) : WeaponSystem{
    override var objectType = true
    override var actionType = false
    override var reloadTimeMillis : Long = 800
    override var timerUntilNextAttack = Timer(reloadTimeMillis)
    var cursorPointF : PointF = PointF(controlCenterPointF.x,controlCenterPointF.y)
    var colorBackground = Color.argb(255,255,155,155)
    var colorForeground = Color.argb(255,0,0,52)
    var strokeWidth = 5f
    var padRadius : Float = 140f
    var margin = 5f
    var cursorRadius = 40f
    var rotation = 1f
    var aimSpeed : Float = .7f
    var rocketStartV = PointF()
    var rocketStartP = PointF()
    var rocketStartS = 400f
    var aimDotN : Int = 15
    var aimLengthToCannonLength : Float = 6f
    var weaponDrawable = Drawable(context,R.drawable.test_100_100,0f,2f,2f,.5f,.5f,100f,100f)
    var millisSinceStart : Long = 0

    init {
        rocketStartP= rotateVector(PointF(0f,-weaponDrawable.height*.8f),-rotation/180f* Math.PI)
        rocketStartV= rotateVector(PointF(0f,-1f), -rotation/180f* Math.PI)
        rocketStartV.x*=rocketStartS
        rocketStartV.y*=rocketStartS
    }

    override fun draw(canvas: Canvas,paint: Paint) {


        //with aiming
        //direction
        paint.style= Paint.Style.FILL
        val v = rotateVector(PointF(0f,-weaponDrawable.height.toFloat()*aimLengthToCannonLength),-rotation/180f* Math.PI)
        //canvas.drawLine(position.x,position.y,position.x+v.x,position.y+v.y,paint)

        //draw dots at timeRatio of all aimDotN segments
        val mm : Long = (1000/aimSpeed).toLong()
        val timeRatio: Float = (millisSinceStart%mm).toFloat()/mm

        paint.color=Color.argb(255,0,255,0)
        //canvas.drawCircle(position.x+v.x*timeRatio,position.y+v.y*timeRatio,20f,paint)
        if (aimDotN>0){
            for(a in 1..aimDotN){
                val ratio : Float = (a-1+timeRatio)/aimDotN.toFloat()
                val k= PointF(weaponHingePointF.x+v.x*ratio,weaponHingePointF.y+v.y*ratio)
                canvas.drawCircle(k.x,k.y,8f*(1-ratio)+3f,paint)

            }
        }else{
            //line on full length
            canvas.drawLine(weaponHingePointF.x,weaponHingePointF.y,weaponHingePointF.x+v.x,weaponHingePointF.y+v.y,paint)
        }

        //the cannon
        weaponDrawable.rotation=rotation
        weaponDrawable.draw(canvas,paint,weaponHingePointF)

        //console here
        paint.strokeWidth=strokeWidth
        paint.color= colorBackground
        canvas.drawCircle(controlCenterPointF.x,controlCenterPointF.y,padRadius,paint)
        paint.color = colorForeground
        canvas.drawCircle(cursorPointF.x,cursorPointF.y,cursorRadius,paint)
        paint.color = colorBackground
        canvas.drawCircle(cursorPointF.x,cursorPointF.y,cursorRadius-strokeWidth,paint)
        paint.color = colorForeground
        canvas.drawCircle(controlCenterPointF.x,controlCenterPointF.y,strokeWidth*2,paint)
    }

    override fun update(millis: Long) {
        timerUntilNextAttack.decrease(millis)
        millisSinceStart+=millis
    }

    override fun objectTypeAttack(): GObject {
        return Bomb(context, PhysicalState(PointF(weaponHingePointF.x+rocketStartP.x,weaponHingePointF.y+ rocketStartP.y),rocketStartV, PointF(0f,0f),1f),border, 20f, 2)
    }

    override fun actionTypeAttack(obstacles: ArrayList<GObject>): Int {
        TODO("Not yet implemented")
    }

    override fun control(points: ArrayList<PointF>) {
        var p= points[0]
        val d= sqrt(abs(controlCenterPointF.x-p.x).pow(2)+ abs(controlCenterPointF.y-p.y).pow(2))
        if(d > padRadius - margin - cursorRadius)
        {
            cursorPointF.x = (p.x-controlCenterPointF.x)/d* (padRadius-margin-cursorRadius)+ controlCenterPointF.x
            cursorPointF.y = (p.y-controlCenterPointF.y)/d* (padRadius-margin-cursorRadius)+ controlCenterPointF.y
        }else{
            cursorPointF.x = p.x
            cursorPointF.y = p.y
        }
        val dy= controlCenterPointF.y-cursorPointF.y
        val dx= cursorPointF.x-controlCenterPointF.x

        if (cursorPointF.x==controlCenterPointF.x && cursorPointF.y==controlCenterPointF.y)
        {
            rotation=0f
        }else{

            val rt = atan(dx/ abs(dy)) *180f/ PI
            Log.d("onCLickJs","rt: $rt")
            if(rt==0.toDouble() && dy<0){
                rotation=180f
            }else if(rt<0 && dy>0){
                rotation=rt.toFloat()
            }else if(rt>0 && dy>0){
                rotation=rt.toFloat()
            }else if(rt<0 && dy<0){
                rotation=-180f-rt.toFloat()
            }else if(rt>0 && dy<0){
                rotation=180f-rt.toFloat()
            }

        }
        rocketStartP= rotateVector(PointF(0f,-weaponDrawable.height*.8f),-rotation/180f* Math.PI)
        rocketStartV= rotateVector(PointF(0f,-1f), -rotation/180f* Math.PI)
        rocketStartV.x*=rocketStartS
        rocketStartV.y*=rocketStartS
        Log.d("onCLickJs","dy:$dy dx:$dx  rotation: $rotation")
    }

}