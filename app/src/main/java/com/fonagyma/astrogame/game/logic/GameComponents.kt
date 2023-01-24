package com.fonagyma.astrogame.game.logic

import android.content.Context
import android.graphics.*
import android.util.Log
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.pow

class PhysicalState(var position: PointF,
                    var velocity: PointF,
                    private var acceleration: PointF,
                    var mass: Float){
    fun update(millis: Long){
        position.x+=velocity.x*(millis.toFloat()/1000).toFloat()
        position.y+=velocity.y*(millis.toFloat()/1000).toFloat()
        velocity.x+=acceleration.x*(millis.toFloat()/1000).toFloat()
        velocity.y+=acceleration.y*(millis.toFloat()/1000).toFloat()
    }
    fun applyForce(force: PointF){
        acceleration.x+=force.x/mass
        acceleration.y+=force.y/mass
    }
}

//TODO: add status effects somehow
abstract class StatusEffect(var duration: Long){

}

//TODO: figure something out for helpers/abilities/summonables
abstract class actionTypeAttackDrawable(){
    abstract fun draw()
    abstract fun update()
}

class Timer{
   private var millis : Long = 0
    constructor(){
        millis = 3000
    }
    constructor(_millis: Long)
    {
        millis = _millis
    }
    fun set(_millis: Long){
        millis = _millis
    }
    fun get():Long{
        return millis
    }
    fun decrease(_millis: Long) : Boolean
    {
        millis-=_millis
        return millis<1
    }
    fun increase(_millis: Long){
        millis+=_millis
    }
}

class Drawable(context: Context, resID: Int, var rotation: Float, var sizeX: Float, var sizeY: Float,
               centerXRatio: Float,centerYRatio:Float,var width : Float, var height : Float){
    private var imageBitmap: Bitmap = BitmapFactory.decodeResource(context.resources,resID)
    private var centerPointF = PointF(width*(centerXRatio)-width/2f,height*(centerYRatio)-height/2f)
    fun draw(canvas: Canvas,paint: Paint,position: PointF){
        val matrix = Matrix()
        matrix.preRotate(rotation)
        matrix.preScale(sizeX,sizeY)
        val adjustedBitmap = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)
        val c = rotateVector(PointF(centerPointF.x*sizeX,centerPointF.y*sizeY),-rotation/180f* Math.PI)
        //canvas.drawBitmap(adjustedBitmap,position.x-adjustedBitmap.width/2-c.x,position.y-adjustedBitmap.height/2-c.y,paint)
        canvas.drawBitmap(adjustedBitmap,null,RectF(position.x-width*(adjustedBitmap.width.toFloat()/imageBitmap.width.toFloat())/2f-c.x,
            position.y-height*(adjustedBitmap.height.toFloat()/imageBitmap.height.toFloat())/2f-c.y,position.x+width*(adjustedBitmap.width.toFloat()/imageBitmap.width.toFloat())/2f-c.x,
            position.y+height*(adjustedBitmap.height.toFloat()/imageBitmap.height.toFloat())/2f-c.y),paint)
    }
}

class Collider{
    var hitBoxCenter: PointF = PointF()
    var hitBoxRadius: Float = 0f
    var hitBoxRectF : RectF = RectF()
    var isBox = false
    constructor(_hitBoxRectF : RectF)
    {
        hitBoxRectF=_hitBoxRectF
        isBox=true
    }
    constructor(_hitBoxCenter: PointF, _hitBoxRadius: Float){
        hitBoxCenter=_hitBoxCenter
        hitBoxRadius=_hitBoxRadius
        isBox=false
    }
    fun intersects(otherCollider: Collider): Boolean{
        if(isBox){
            if (otherCollider.isBox){
                //for now lets stay with circle-circle will do math later
            }else{
                //for now lets stay with circle-circle will do math later
            }
        }else{
            if (otherCollider.isBox){
                //for now lets stay with circle-circle will do math later
            }else{
                val distance = kotlin.math.sqrt(
                    kotlin.math.abs(hitBoxCenter.x - otherCollider.hitBoxCenter.x).pow(2) +
                            kotlin.math.abs(hitBoxCenter.y - otherCollider.hitBoxCenter.y).pow(2)
                )
                return hitBoxRadius + otherCollider.hitBoxRadius - 1f > distance
            }
        }
        return false
    }
}

abstract class GObject(var physicalState : PhysicalState){
    lateinit var drawable : Drawable
    lateinit var collider : Collider
    var exists : Boolean= true
    var wasDestroyed : Boolean= false
    var pointsOnDestruction : Int = 0
    var gearsOnDestruction : Int = 0
    var crystalsOnDestruction : Int = 0
    var typeID : Int = 0
    abstract fun draw(canvas: Canvas,paint: Paint)
    abstract fun update(millis: Long)
    abstract fun onCollide(otherGObject: GObject)
}


fun rotateVector(v : PointF, rad: Double): PointF{
    return PointF((kotlin.math.cos(rad) *v.x+ kotlin.math.sin(rad) *v.y).toFloat(),
        (kotlin.math.cos(rad) *v.y- kotlin.math.sin(rad) *v.x).toFloat())
}

//mirrors v to e
fun mirrorVectorToVector(v:PointF,e:PointF):PointF{
    val angle = atan(e.y.toDouble()/e.x.toDouble())
    val va= rotateVector(v,angle)
    va.y*=-1f
    return rotateVector(va,-angle)
}

fun directionToRotation(direction: PointF): Float{
    val rt = atan(direction.x/ abs(direction.y)) *180f/ PI

    if(rt==0.toDouble() && direction.y<0){
        return 180f
    }else if(rt<0 && direction.y>0){
         return rt.toFloat()
    }else if(rt>0 && direction.y>0){
        return rt.toFloat()
    }else if(rt<0 && direction.y<0){
        return -180f-rt.toFloat()
    }else if(rt>0 && direction.y<0){
        return 180f-rt.toFloat()
    }
    return 0f
}

/*
class GButton( box : RectF, context: Context, ImageR: Int) {

    var strokeWidth = 5f
    var upgradeCount : Int = 0

    override fun log() {
        TODO("Not yet implemented")
    }
    fun reset(){
        counter = ctstart
        upgradeCount = 0
        upgradecost = 20
    }

    override fun onClick(p: PointF) {
        counter+=incrAmonunt
        upgradeCount++
        upgradecost+=10
    }

    override fun update(millisPassed: Long, vararg plus: Float) {
        return
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        val matrix = Matrix()
        matrix.preScale(sizeX,sizeY)
        val myB = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)

        val c = rotateVector(PointF(cP.x*sizeX,cP.y*sizeY),-turn/180f* Math.PI)
        canvas.drawBitmap(myB,midP.x-myB.width/2-c.x,midP.y-myB.height/2-c.y,paint)

        paint.color=Color.argb(255,255,255,0)
        paint.textSize= hitBox.height()/3f
        canvas.drawText("$upgradeCount",hitBox.left+margin,hitBox.bottom-margin,paint)
        paint.color=Color.argb(255,0,255,255)
        canvas.drawText("$upgradecost",hitBox.left+margin,hitBox.top+paint.textSize,paint)
        if (showHitbox){
            paint.color= Color.argb(255,255,255,0)
            paint.strokeWidth=strokeWidth
            paint.style=Paint.Style.STROKE
            canvas.drawRect(hitBox,paint)
            paint.style=Paint.Style.FILL

        }
    }

}

class UpgradeButton(box : RectF, context: Context, ImageR: Int,  var counterStart:Float, var icrementAmount: Float): GButton(box,context,imageR){
    var counter = 1f
    var ctstart = 0f
    var margin = 0f
    init {
        upgradecost= 20
        sizeX = _x
        sizeY = _y
        margin = hitBox.height()/20f
        sizeY *= hitBox.height()/120f
        sizeX *= hitBox.height()/120f
        counter = _ctrStart
        ctstart = _ctrStart
        incrAmonunt = _icrA
        imageR= _ImageR
        imageBitmap = BitmapFactory.decodeResource(context.resources,imageR)
        Log.d("inf","${imageBitmap.height} ${imageBitmap.width} ")

        cP= PointF(imageBitmap.width*(.50f)-imageBitmap.width/2f,imageBitmap.height*(.5f)-imageBitmap.height/2f)

    }
}
*/