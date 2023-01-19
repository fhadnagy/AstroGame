package com.fonagyma.astrogame.game.logic

import android.content.Context
import android.graphics.*
import android.util.Log
import com.fonagyma.astrogame.R
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.random.Random

abstract class OLDCollidable (pos: PointF, context: Context, _velocity : PointF, _hR : Float): OLDGameObject(pos,context) {
    var velocity = _velocity
    var exists = true
    var type : Int = 0
    var hR = _hR
    var pointsOnDestruction : Int = 0
    var destroyed = false
    abstract fun collides(oC : OLDCollidable): Boolean
    abstract fun onCollide(oC: OLDCollidable)
}

class OLDAsteroid(pos: PointF, context: Context, _velocity : PointF, _mass: Float, _hR : Float, walle : PointF, _mxhp: Int) : OLDCollidable(pos, context, _velocity, _hR){
    var omega = 50f
    var mxhp : Int = 5
    var hp : Int
    var mass :Float
    val random = Random(System.currentTimeMillis())
    private var wall : PointF
    init {
        type = 1
        omega = random.nextFloat()*30f-60f
        sizeX=.01f*hR
        sizeY=.01f*hR
        mass= _mass
        wall = walle
        mxhp=_mxhp
        hp=mxhp
        pointsOnDestruction = mxhp

        imageR= R.drawable.astroid1
        imageBitmap = BitmapFactory.decodeResource(context.resources,imageR)
        Log.d("inf","${imageBitmap.height} ${imageBitmap.width} ")

        cP= PointF(imageBitmap.width*(.49f)-imageBitmap.width/2f,imageBitmap.height*(.53f)-imageBitmap.height/2f)
    }
    override fun log() {
        TODO("Not yet implemented")
    }
    override fun draw(canvas: Canvas, paint: Paint) {
        if(!exists) return
        //paint.color=Color.argb(255,255,0,0)
        //canvas.drawCircle(position.x,position.y,hR,paint)


        val matrix = Matrix()
        matrix.preRotate(turn)
        matrix.preScale(sizeX,sizeY)
        val myB = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)

        val c = rotateVector(PointF(cP.x*sizeX,cP.y*sizeY),-turn/180f* Math.PI)
        canvas.drawBitmap(myB,position.x-myB.width/2-c.x,position.y-myB.height/2-c.y,paint)

        paint.color=Color.argb(255,255,0,0)

        /*paint.style= Paint.Style.STROKE
        canvas.drawCircle(position.x,position.y,hR,paint)
        */
        paint.style= Paint.Style.FILL
        paint.color=Color.argb(255,255,0,0)

        canvas.drawRect(position.x-hR,position.y-hR*1.4f,position.x+hR,position.y-hR*1.1f,paint)
        paint.color=Color.argb(255,0,255,0)

        canvas.drawRect(position.x-hR,position.y-hR*1.4f,position.x+2*hR*(-.5f+hp.toFloat()/mxhp.toFloat()),position.y-hR*1.1f,paint)

    }
    override fun update(millisPassed: Long, vararg plus: Float) {
        if (hp<=0) {
            exists= false
            destroyed = true
        }
        if(!exists) return
        //omega *= 1f-.05f *(millisPassed/1000f)
        turn += omega *(millisPassed/1000f)
        if (turn >360f) turn-=360f
        position.x += velocity.x
        position.y += velocity.y

        if(position.x>wall.x || position.y>wall.y|| position.x<0 || position.y<0){
            exists = false
        }

        /**
        if(position.x>wall.x-hR){
            velocity.x *= -.9f
            position.x = wall.x-hR
        }
        if(position.y>wall.y-hR){
            velocity.y *= -.9f
            position.y = wall.y-hR
        }
        if(position.x<hR){
            velocity.x *= -.9f
            position.x = hR
        }
        if(position.y<hR){
            velocity.y *= -.9f
            position.y = hR
        }*/
    }
    override fun collides(oC: OLDCollidable): Boolean {
        if (!exists or !oC.exists){
            return false
        }
        val d = kotlin.math.sqrt(
            kotlin.math.abs(position.x - oC.position.x).pow(2) + kotlin.math.abs(
                position.y - oC.position.y
            ).pow(2)
        )
        if (hR+ oC.hR - 1f > d) return true

        return false
    }
    override fun onCollide(oC: OLDCollidable) {
        if (oC.type == 1)
        {
            val other= oC as OLDAsteroid
            val d = kotlin.math.sqrt(
                kotlin.math.abs(position.x - other.position.x).pow(2) + kotlin.math.abs(
                    position.y - other.position.y
                ).pow(2)
            )

            /*val mid = PointF(
                    (position.x * (other.hitBoxR + mass.pow(2)) + other.position.x * (hitBoxR + other.mass.pow(
                            2
                    ))) / (other.hitBoxR + hitBoxR + mass.pow(2) + other.mass.pow(2)),
                    (position.y * (other.hitBoxR + mass.pow(2)) + other.position.y * (hitBoxR + other.mass.pow(
                            2
                    ))) / (other.hitBoxR + hitBoxR + mass.pow(2) + other.mass.pow(2))
            )*/
            val mid = PointF(
                (position.x * (other.hR) + other.position.x * (hR)) / (other.hR + hR),
                (position.y * (other.hR)+ other.position.y * (hR)) / (other.hR + hR))

            val vx = position.x - mid.x
            val vy = position.y - mid.y
            val dv = kotlin.math.sqrt(kotlin.math.abs(vx).pow(2) + kotlin.math.abs(vy).pow(2))

            val e = PointF(position.y - other.position.y, other.position.x - position.x)
            val angle = atan(e.y.toDouble() / e.x.toDouble())

            val va = rotateVector(velocity, angle)
            val vb = rotateVector(other.velocity, angle)
            val k = 1f
            val v1 = va.y
            val v2 = vb.y
            val C = (1 + k) * (mass * v1 + other.mass * v2) / (mass + other.mass)
            va.y = C - k * v1
            vb.y = C - k * v2

            velocity = rotateVector(va, -angle)
            other.velocity = rotateVector(vb, -angle)

            position.x = mid.x + vx / dv * hR * 1.001f
            position.y = mid.y + vy / dv * hR * 1.001f
            other.position.x = mid.x - vx / dv * other.hR * 1.001f
            other.position.y = mid.y - vy / dv * other.hR * 1.001f

        }else if(oC.type==2){
            val bll = oC as OLDBall
            hp-=bll.damage
            oC.exists= false
            oC.destroyed = true
        }else if(oC.type == 3)
        {
            val exp = oC as OLDExplosion
            if (exp.active)
            {
                hp-=exp.damage

            }
        }else if(oC.type==4)
        {
            val rc = oC as OLDRocket
            hp-=rc.damage
            rc.exists=false
            rc.destroyed= true

        }

    }
}

class OLDBall(pos: PointF, context: Context, _velocity :PointF, mss :Float, _hR : Float, walle : PointF) : OLDCollidable(pos,context,_velocity,_hR){

    var mass :Float
    var lifetime: Long =5000
    private var wall : PointF
    var damage = 2
    private val rnd = Random(System.currentTimeMillis())
    private val colorM = Color.argb(255,100+rnd.nextInt(155),100+rnd.nextInt(155),100+rnd.nextInt(155))
    init {
        type = 2
        mass = mss
        wall = walle
    }
    override fun draw(canvas: Canvas, paint: Paint) {
        if(!exists) return
        paint.color=colorM
        canvas.drawCircle(position.x,position.y,hR,paint)
    }
    override fun log() {
        TODO("Not yet implemented")
    }
    override fun update(millisPassed: Long, vararg plus: Float) {
        if (lifetime<1){
            exists= false
        }
        if(!exists) return
        position.x+=velocity.x*millisPassed/1000
        position.y+=velocity.y*millisPassed/1000

        if(position.x>wall.x-hR){
            velocity.x *= -.9f
            position.x = wall.x-hR
        }
        if(position.y>wall.y-hR){
            velocity.y *= -.9f
            position.y = wall.y-hR
        }
        if(position.x<hR){
            velocity.x *= -.9f
            position.x = hR
        }
        if(position.y<hR){
            velocity.y *= -.9f
            position.y = hR
        }
        lifetime-=millisPassed
    }
    override fun collides(oC: OLDCollidable): Boolean {
        if (!exists or !oC.exists){
            return false
        }
        val d = kotlin.math.sqrt(
            kotlin.math.abs(position.x - oC.position.x).pow(2) + kotlin.math.abs(
                position.y - oC.position.y
            ).pow(2)
        )


        if (hR + oC.hR > d) return true

        return false

    }
    override fun onCollide(oC: OLDCollidable){
        if (oC.type==2) {
            val other = oC as OLDBall

            val mid = PointF(
                (position.x * (other.hR) + other.position.x * (hR)) / (other.hR + hR),
                (position.y * (other.hR) + other.position.y * (hR)) / (other.hR + hR)
            )

            val vx = position.x - mid.x
            val vy = position.y - mid.y
            val dv = kotlin.math.sqrt(
                kotlin.math.abs(vx).pow(2) + kotlin.math.abs(vy).pow(2)
            )

            val e = PointF(position.y - other.position.y, other.position.x - position.x)
            val angle = atan(e.y.toDouble() / e.x.toDouble())

            val va = rotateVector(velocity, angle)
            val vb = rotateVector(other.velocity, angle)
            val k = 1f
            val v1 = va.y
            val v2 = vb.y
            val C = (1 + k) * (mass * v1 + other.mass * v2) / (mass + other.mass)
            va.y = C - k * v1
            vb.y = C - k * v2

            velocity = rotateVector(va, -angle)
            other.velocity = rotateVector(vb, -angle)

            position.x = mid.x + vx / dv * hR * 1.001f
            position.y = mid.y + vy / dv * hR * 1.001f
            other.position.x = mid.x - vx / dv * other.hR * 1.001f
            other.position.y = mid.y - vy / dv * other.hR * 1.001f
        }else if(oC.type==1){
            exists= false
            destroyed = true
            val other = oC as OLDAsteroid
            other.hp-=damage
        }else if(oC.type == 3)
        {
            val exp = oC as OLDExplosion
            if (exp.active){
                exists= false
                destroyed = true
            }

        }else if(oC.type==4)
        {
            val rc = oC as OLDRocket
            exists=false
            destroyed= true
            rc.exists=false
            rc.destroyed= true

        }
    }
}

class OLDExplosion(pos: PointF, context: Context, _velocity :PointF, _hR : Float, dmg: Int) : OLDCollidable(pos,context,_velocity,_hR){
    var lifetime: Long =50
    var damage = 10
    var active = true
    private val rnd = Random(System.currentTimeMillis())
    private val colorM = Color.argb(255,100+rnd.nextInt(155),100+rnd.nextInt(155),100+rnd.nextInt(155))
    init {
        type = 3
        sizeX=.013f*hR
        sizeY=.013f*hR
        turn = rnd.nextFloat()*360f
        imageR= R.drawable.exp
        imageBitmap = BitmapFactory.decodeResource(context.resources,imageR)

        cP= PointF(imageBitmap.width*(.40f)-imageBitmap.width/2f,imageBitmap.height*(.43f)-imageBitmap.height/2f)

        damage= dmg
    }
    override fun draw(canvas: Canvas, paint: Paint) {
        if(!exists) return


        val matrix = Matrix()
        matrix.preRotate(turn)
        matrix.preScale(sizeX,sizeY)
        val myB = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)

        val c = rotateVector(PointF(cP.x*sizeX,cP.y*sizeY),-turn/180f* Math.PI)
        canvas.drawBitmap(myB,position.x-myB.width/2-c.x,position.y-myB.height/2-c.y,paint)

        paint.color=Color.argb(255,255,0,0)

        //paint.style= Paint.Style.STROKE
        //canvas.drawCircle(position.x,position.y,hR,paint)

        paint.style= Paint.Style.FILL

    }
    override fun log() {
        TODO("Not yet implemented")
    }
    override fun update(millisPassed: Long, vararg plus: Float) {
        if (lifetime<1){
            exists= false
        }
        active = false
        if(!exists) return

        lifetime-=millisPassed
    }
    override fun collides(oC: OLDCollidable): Boolean {
        if (!exists or !oC.exists){
            return false
        }

        val d = kotlin.math.sqrt(
            kotlin.math.abs(position.x - oC.position.x).pow(2) + kotlin.math.abs(
                position.y - oC.position.y
            ).pow(2)
        )

        if (hR + oC.hR > d) return true

        return false

    }
    override fun onCollide(oC: OLDCollidable){
        if(!active) return
        if (oC.type==2) {
            oC.exists = false
            oC.destroyed = true
        }else if(oC.type==1){
            val other = oC as OLDAsteroid
            other.hp-=damage
        }else if(oC.type==4){
            oC.exists = false
            oC.destroyed = true
        }
    }
}

class OLDRocket(pos: PointF, context: Context, _velocity : PointF, _hR : Float, walle : PointF, dmg: Int, _turn: Float, _iAc:Float) : OLDCollidable(pos, context, _velocity, _hR){
    val random = Random(System.currentTimeMillis())
    var lifetime : Long = 0
    var inaccuracy : Float
    private var wall : PointF
    var damage = 10
    init {
        type = 4
        sizeX=.032f*hR
        sizeY=.032f*hR
        wall = walle
        inaccuracy = _iAc
        damage=dmg
        turn=_turn
        imageR= R.drawable.rcketwflame
        imageBitmap = BitmapFactory.decodeResource(context.resources,imageR)
        Log.d("inf","${imageBitmap.height} ${imageBitmap.width} ")

        cP= PointF(imageBitmap.width*(.50f)-imageBitmap.width/2f,imageBitmap.height*(.10f)-imageBitmap.height/2f)
    }
    override fun log() {
        TODO("Not yet implemented")
    }
    override fun draw(canvas: Canvas, paint: Paint) {
        if(!exists) return
        //paint.color=Color.argb(255,255,0,0)
        //canvas.drawCircle(position.x,position.y,hR,paint)

        val matrix = Matrix()
        matrix.preRotate(turn)
        matrix.preScale(sizeX,sizeY)
        val myB = Bitmap.createBitmap(imageBitmap,0, 0, imageBitmap.width, imageBitmap.height, matrix, true)

        val c = rotateVector(PointF(cP.x*sizeX,cP.y*sizeY),-turn/180f* Math.PI)
        canvas.drawBitmap(myB,position.x-myB.width/2-c.x,position.y-myB.height/2-c.y,paint)

        paint.color=Color.argb(255,255,0,0)

        //paint.style= Paint.Style.STROKE
        //canvas.drawCircle(position.x,position.y,hR,paint)

        paint.style= Paint.Style.FILL
        paint.color=Color.argb(255,255,0,0)


        /*paint.color=Color.argb(255,255,0,0)
        canvas.drawLine(position.x,position.y,position.x+myB.width/2,
        position.y+myB.height/2,paint)
        paint.color=Color.argb(255,255,255,0)
        canvas.drawLine(position.x+myB.width/2, position.y+myB.height/2,
        position.x+myB.width/2+c.x, position.y+myB.height/2+c.y, paint)*/
    }
    override fun update(millisPassed: Long, vararg plus: Float) {
        if(!exists) return
        if(lifetime>50){
            lifetime-=50
            val temp : Float =(-.5f +  random.nextFloat())*inaccuracy
            turn += temp
            velocity = rotateVector(velocity,-temp/180f*PI)
        }
        //omega *= 1f-.05f *(millisPassed/1000f)
        //turn += omega *(millisPassed/1000f)
        //if (turn >360f) turn-=360f
        position.x+=velocity.x*millisPassed/1000
        position.y+=velocity.y*millisPassed/1000

        if(position.x>wall.x || position.y>wall.y|| position.x<0 || position.y<0){
            exists = false
            destroyed = false
        }

        /**
        if(position.x>wall.x-hR){
        velocity.x *= -.9f
        position.x = wall.x-hR
        }
        if(position.y>wall.y-hR){
        velocity.y *= -.9f
        position.y = wall.y-hR
        }
        if(position.x<hR){
        velocity.x *= -.9f
        position.x = hR
        }
        if(position.y<hR){
        velocity.y *= -.9f
        position.y = hR
        }*/
        lifetime+=millisPassed
    }
    override fun collides(oC: OLDCollidable): Boolean {
        if (!exists or !oC.exists){
            return false
        }
        val d = kotlin.math.sqrt(
            kotlin.math.abs(position.x - oC.position.x).pow(2) + kotlin.math.abs(
                position.y - oC.position.y
            ).pow(2)
        )
        if (hR+ oC.hR - 1f > d) return true

        return false
    }
    override fun onCollide(oC: OLDCollidable) {
        if (oC.type == 1)
        {
            val other= oC as OLDAsteroid
            other.hp-=damage
            exists=false
            destroyed=true

        }else if(oC.type==2){
            oC.exists= false
            oC.destroyed = true
            exists=false
            destroyed=true
        }else if(oC.type == 3)
        {
            val exp = oC as OLDExplosion
            if (exp.active)
            {
                exists=false
                destroyed=true
            }
        }else if(oC.type==4)
        {
            exists= false
            destroyed=true
            oC.exists= false
            oC.destroyed=true
        }

    }
}