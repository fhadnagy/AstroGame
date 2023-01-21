package com.fonagyma.astrogame.game.logic

import android.content.Context
import android.graphics.*
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
    private var centerPointF = PointF(imageBitmap.width*(centerXRatio)-imageBitmap.width/2f,imageBitmap.height*(centerYRatio)-imageBitmap.height/2f)
    fun draw(canvas: Canvas,paint: Paint,position: PointF){
        val matrix = Matrix()
        matrix.preRotate(rotation)
        matrix.preScale(sizeX,sizeY)
        val adjustedBitmap = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)
        val c = rotateVector(PointF(centerPointF.x*sizeX,centerPointF.y*sizeY),-rotation/180f* Math.PI)
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
