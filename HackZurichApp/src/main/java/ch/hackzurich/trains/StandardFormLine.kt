package ch.hackzurich.trains

import org.opencv.core.Point

data class StandardFormLine(val a: Double, val c: Double) {
    companion object {
        fun fromPoints(p1: Point, p2: Point): StandardFormLine {
            val a = (p2.y - p1.y) / (p2.x - p1.x)
            val c = p1.y - a * p1.x
            return StandardFormLine(a,c)
        }

    }
    fun getPointForX(x: Double): Point{
        val y = (a*x)+c
        return Point(x,y)
    }
}
