package ch.hackzurich.trains

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.max

object PerspectiveTransformationUtil {
    fun transformPerspective(image: Mat, inner: Mat, outer: Mat, corners: Array<Point>): Mat {
        // determine width of the new image
        // (top right and top left)
        val widthA = Math.sqrt((corners[0].x - corners[1].x))
        val widthB = 0.0;
        val width = max(widthA, widthB)
        // determine height of the new image
        // (distance between top right and bottom right)
        val heightA = 0.0;
        val heightB = 0.0;
        val height = max(heightA, heightB)
        // find perspective transform matrix
        var matrix = Imgproc.getPerspectiveTransform(inner, outer)
        // transform the image
        var output = Mat()
        var point = Point(width, height)
        var transformed = Imgproc.warpPerspective(image, output, matrix, Size(200.0, 300.0)) // i s outer correct here?
        // rotate? and return the result
        return output
    }

}
