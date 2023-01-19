package com.fonagyma.astrogame.game.logic

import android.content.Context
import android.graphics.*
import android.util.Log
import java.lang.Math.*
import com.fonagyma.astrogame.R
import kotlin.math.atan

abstract class OLDGameObject(pos: PointF, context: Context){

        var sizeX : Float = 1f
        var sizeY : Float = 1f
        var turn : Float = 0f
        var position = pos
        lateinit var cP : PointF
        lateinit var imageBitmap : Bitmap
        var imageR : Int = -1
        abstract fun log()
        abstract fun draw(canvas : Canvas, paint: Paint)
        abstract fun update(millisPassed: Long, vararg plus: Float)
        /*fun force(a: PointF){
            velocity.x+=a.x
            velocity.y+=a.y
        }

        fun update(fps: Long) {
            // Move the particle
            val dt = 1f/fps
            position.x += velocity.x
            position.y += velocity.y
        }

        fun collides(other: GameObject): Boolean{
            val d= sqrt(abs(position.x-other.position.x).pow(2f) + abs(position.y-other.position.y).pow(2f))
            if (d<2*hitBoxR)
            {
                return true
            }
            return false
        }*/

}

class OLDCannon(pos: PointF, context: Context) : OLDGameObject(pos,context){

        var rotation : Float = 0f
        var millisSinceStart: Long= 0
        var aimSpeed : Float = .7f
        var rocketStartV = PointF()
        var rocketStartP = PointF()
        var rocketStartS = 800f
        var aimDotN : Int = 15
        var aimLengthToCannonLength : Float = 3f
        init {
                sizeX=1f
                sizeY=1f

            imageR= R.drawable.cannon
            imageBitmap = BitmapFactory.decodeResource(context.resources,imageR)
                Log.d("inf","${imageBitmap.height} ${imageBitmap.width} ")
                //20 10 so
                cP= PointF(imageBitmap.width*(25f/50f)-imageBitmap.width/2f,imageBitmap.height*(75f/100f)-imageBitmap.height/2f)
        }

        override fun update(millisPassed: Long, vararg plus: Float) {
                millisSinceStart+=millisPassed
                Log.d("ms", "$millisSinceStart")
                rotation = if (plus.isNotEmpty()){
                        plus[0]
                }else{
                        0f
                }
                rocketStartP= rotateVector(PointF(0f,-imageBitmap.height.toFloat()*.8f),-rotation/180f* PI)
                rocketStartV= rotateVector(PointF(0f,-1f), -rotation/180f* PI)
                rocketStartV.x*=rocketStartS
                rocketStartV.y*=rocketStartS
                Log.d("cannon  ","${rocketStartV.x} ${rocketStartV.y}")
                //sizeY= 2f+sin(millisSinceStart.toDouble()/1000f*2* PI).toFloat()
                //sizeX= 2f+sin(millisSinceStart.toDouble()/1000f*2* PI).toFloat()
        }


        override fun draw(canvas: Canvas, paint: Paint) {
                //paint.color=Color.argb(255,255,0,0)
                //canvas.drawCircle(position.x,position.y,10f,paint)
                //direction
                val v = rotateVector(PointF(0f,-imageBitmap.height.toFloat()*aimLengthToCannonLength),-rotation/180f* PI)
                //canvas.drawLine(position.x,position.y,position.x+v.x,position.y+v.y,paint)

                //draw dots at timeRatio of all aimDotN segments
                val mm : Long = (1000/aimSpeed).toLong()
                val timeRatio: Float = (millisSinceStart%mm).toFloat()/mm

                paint.color=Color.argb(255,0,255,0)
                //canvas.drawCircle(position.x+v.x*timeRatio,position.y+v.y*timeRatio,20f,paint)
                if (aimDotN>0){
                        for(a in 1..aimDotN){
                                val ratio : Float = (a-1+timeRatio)/aimDotN.toFloat()
                                val k= PointF(position.x+v.x*ratio,position.y+v.y*ratio)
                                canvas.drawCircle(k.x,k.y,8f*(1-ratio)+3f,paint)

                        }
                }else{
                        //line on full length
                        canvas.drawLine(position.x,position.y,position.x+v.x,position.y+v.y,paint)
                }

                val matrix = Matrix()
                matrix.preRotate(rotation)
                matrix.preScale(sizeX,sizeY)
                val myB = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)

                val c = rotateVector(PointF(cP.x*sizeX,cP.y*sizeY),-rotation/180f* PI)
                canvas.drawBitmap(myB,position.x-myB.width/2-c.x,position.y-myB.height/2-c.y,paint)

                /*paint.color=Color.argb(255,255,0,0)
                canvas.drawLine(position.x,position.y,position.x+myB.width/2,
                position.y+myB.height/2,paint)
                paint.color=Color.argb(255,255,255,0)
                canvas.drawLine(position.x+myB.width/2, position.y+myB.height/2,
                position.x+myB.width/2+c.x, position.y+myB.height/2+c.y, paint)*/

        }

        override fun log() {
                TODO("Not yet implemented")
        }
}

/**
 * rotates clockwise
 */
fun rotateVector(v : PointF, rad: Double): PointF{
        return PointF((kotlin.math.cos(rad) *v.x+ kotlin.math.sin(rad) *v.y).toFloat(),
                (kotlin.math.cos(rad) *v.y- kotlin.math.sin(rad) *v.x).toFloat())
}

fun mirrorVectorToVector(v:PointF,e:PointF):PointF{
        val angle = atan(e.y.toDouble()/e.x.toDouble())
        val va= rotateVector(v,angle)
        va.y*=-1f
        return rotateVector(va,-angle)
}


