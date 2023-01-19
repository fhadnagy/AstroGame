package com.fonagyma.astrogame.game.logic

import android.graphics.PointF
import android.graphics.RectF

class PhysicalState(var position: PointF,
                    var velocity: PointF,
                    private var acceleration: PointF,
                    var mass: Float){
    fun update(millis: Long){
        position.x+=velocity.x*(millis/1000).toFloat()
        position.y+=velocity.y*(millis/1000).toFloat()
        velocity.x+=acceleration.x*(millis/1000).toFloat()
        velocity.y+=acceleration.y*(millis/1000).toFloat()
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

abstract class Drawable(var resID : Int, var rotation: Float, var sizeX: Float, var sizeY: Float){
    abstract fun draw()
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
    //TODO: collision for circle and rectangle
}

interface GObject{
    var timer : Timer
    var physicalState : PhysicalState
    var drawable : Drawable
    var collider : Collider
    fun draw(millis: Long)
    fun update(millis: Long)
}