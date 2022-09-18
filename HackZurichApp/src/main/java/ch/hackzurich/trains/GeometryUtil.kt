package ch.hackzurich.trains

import org.opencv.core.Point
import kotlin.math.pow

object GeometryUtil {
    fun ensurePairOfCorrectOrientation(first: Point, second: Point): Pair<Point, Point>{
        if(first.y<second.y){
            return Pair(second, first);
        }
        return Pair(first, second)
    }
    fun calculateIntersectionPoint(
        a1: Double,
        c1: Double,
        a2: Double,
        c2: Double
    ): Point {
        val x = (c2 - c1) / (a1 - a2)
        val y = a1 * x + c1
        return Point(x,y)
    }
    fun getDistance(a: Point, r1: Point, r2: Point): Double {
        return (kotlin.math.abs(
            (r2.x - r1.x) * (r1.y - a.y)
                    - (r1.x - a.x) * (r2.y - r1.y)
        )
                / kotlin.math.sqrt((r2.x - r1.x).pow(2.0) + (r2.y - r1.y).pow(2.0)))
    }

}
