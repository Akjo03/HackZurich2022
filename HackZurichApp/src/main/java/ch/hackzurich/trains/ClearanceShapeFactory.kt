package ch.hackzurich.trains

import org.opencv.core.MatOfPoint
import org.opencv.core.Point

object ClearanceShapeFactory {


    fun getRightClearance(anchor: Point, factor: Double): List<MatOfPoint> {
        return mirrorableClearance(anchor, factor, true)
    }

    fun getLeftClearance(anchor: Point, factor: Double): List<MatOfPoint> {
        return mirrorableClearance(anchor, factor, false)
    }

    private fun mirrorableClearance(anchor: Point, factor: Double, isMirrored: Boolean): List<MatOfPoint> {
        val anchorY = anchor.y
        val anchorX = anchor.x
        var mirror = 1;
        if (isMirrored) {
            mirror = -1;
        }
        return listOf(
            MatOfPoint(
                Point(anchorX, anchorY),
                Point(131.0 * mirror * factor + anchorX, 9.0 * -1 * factor + anchorY),
                Point(169.0 * mirror * factor + anchorX, 36.0 * -1 * factor + anchorY),
                Point(169.0 * mirror * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * mirror * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * mirror * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * mirror * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * mirror * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * mirror * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * mirror * factor + anchorX, 333.0 * -1 * factor + anchorY),
                Point(166.0 * mirror * factor + anchorX, 430.0 * -1 * factor + anchorY),
                Point(102.2 * mirror * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * mirror * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * mirror * factor + anchorX, 600.0 * -1 * factor + anchorY),
                Point(0.0 * mirror * factor + anchorX, 600.0 * -1 * factor + anchorY)
            )
        )
    }
}
