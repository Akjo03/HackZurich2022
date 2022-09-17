package ch.hackzurich.trains

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.bitwise_and
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import java.lang.Math.*
import kotlin.math.max
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        val mainImageView = findViewById<ImageView>(R.id.imageView)
        setImageViewListener(mainImageView)

        val mainImage = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)

        var resizedImage = Mat()
        resize(mainImage, resizedImage, Size(mainImage.width()*2.0, mainImage.height()*2.0))

        val grayscaleImage = Mat()
        cvtColor(resizedImage, grayscaleImage, COLOR_RGBA2GRAY)

        var blurredImage = Mat()
        GaussianBlur(grayscaleImage, blurredImage, Size(5.0, 5.0), 0.0)

        calculateLine(getSlice(blurredImage), resizedImage, mainImageView, 50.0, 81.0, 300, 10.0, 80.0)
    }

    fun calculateLine(inputImage: Mat, originalImage: Mat, mainImageView: ImageView, cannyThreshold1: Double, cannyThreshold2: Double, lineThreshold: Int, minLineLength: Double, maxLineGap: Double) {
        val topLeft = Point(356.96777, 724.9519)
        val topRight = Point(529.9695, 724.9519)
        val bottomLeft = Point(261.97998, 950.8967)
        val bottomRight = Point(625.94604, 950.8967)
        val startX = bottomLeft.x + ((bottomRight.x - bottomLeft.x) / 2).roundToInt()

        val cannyImage = Mat()
        Canny(inputImage, cannyImage, cannyThreshold1, cannyThreshold2)

        var lineImage = Mat()
        HoughLinesP(cannyImage, lineImage, 1.0, PI/180, lineThreshold, minLineLength, maxLineGap)

        for (x in 0 until lineImage.rows()) {
            val vec: DoubleArray = lineImage.get(x, 0)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]
            val start = Point(x1, y1)
            val end = Point(x2, y2)
            // only draw lines that reach the top
            val min = min(y2, y1);
            println("y1:" + y1)
            if (min < 600.0 && y1 != y2) {
                line(originalImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
            }
        }

        val bitmapImage = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.ARGB_8888)

        val rectangle: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(0.0, originalImage.height().toDouble()), // bottom left
                Point(0.0, 450.0),  // top left
                Point(originalImage.width().toDouble(), 450.0),  // top right
                Point(originalImage.width().toDouble(), originalImage.height().toDouble())  // bottom right
            )
        )

        var clearance1: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(startX, bottomLeft.y),
                Point(131.0 + startX, 9.0 * -1 + bottomLeft.y),
                Point(169.0 + startX, 36.0 * -1 + bottomLeft.y),
                Point(169.0 + startX, 56.0 * -1 + bottomLeft.y),
                Point(210.0 + startX, 56.0 * -1 + bottomLeft.y),
                Point(210.0 + startX, 170.0 * -1 + bottomLeft.y),
                Point(220.0 + startX, 170.0 * -1 + bottomLeft.y),
                Point(220.0 + startX, 300.0 * -1 + bottomLeft.y),
                Point(210.0 + startX, 300.0 * -1 + bottomLeft.y),
                Point(210.0 + startX, 333.0 * -1 + bottomLeft.y),
                Point(166.0 + startX, 430.0 * -1 + bottomLeft.y),
                Point(102.2 + startX, 480.0 * -1 + bottomLeft.y),
                Point(102.0 + startX, 480.0 * -1 + bottomLeft.y),
                Point(102.0 + startX, 600.0 * -1 + bottomLeft.y),
                Point(0.0 + startX, 600.0 * -1 + bottomLeft.y)
            )
        )
        var clearance2: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(startX, bottomLeft.y),
                Point(131.0 * -1 + startX, 9.0 * -1 + bottomLeft.y),
                Point(169.0 * -1 + startX, 36.0 * -1 + bottomLeft.y),
                Point(169.0 * -1 + startX, 56.0 * -1 + bottomLeft.y),
                Point(210.0 * -1 + startX, 56.0 * -1 + bottomLeft.y),
                Point(210.0 * -1 + startX, 170.0 * -1 + bottomLeft.y),
                Point(220.0 * -1 + startX, 170.0 * -1 + bottomLeft.y),
                Point(220.0 * -1 + startX, 300.0 * -1 + bottomLeft.y),
                Point(210.0 * -1 + startX, 300.0 * -1 + bottomLeft.y),
                Point(210.0 * -1 + startX, 333.0 * -1 + bottomLeft.y),
                Point(166.0 * -1 + startX, 430.0 * -1 + bottomLeft.y),
                Point(102.2 * -1 + startX, 480.0 * -1 + bottomLeft.y),
                Point(102.0 * -1 + startX, 480.0 * -1 + bottomLeft.y),
                Point(102.0 * -1 + startX, 600.0 * -1 + bottomLeft.y),
                Point(0.0 * -1 + startX, 600.0 * -1 + bottomLeft.y)
            )
        )
        /*
        var flippedClearance = clearance.get(0).clone()
        Core.multiply(clearance.get(0), MatOfDouble(1.0, -1.0, 1.0), flippedClearance)

        // clearance = listOf(flippedClearance as MatOfPoint)
        var flippedMoment = listOf(MatOfPoint(flippedClearance))
        */

        var innerShape = MatOfPoint(
            bottomLeft, // bottom left
            topLeft,  // top left
            topRight,  // top right
            bottomRight  // bottom right
        )

        var outerShape = MatOfPoint(
            bottomLeft, // bottom left
            Point(bottomLeft.x, topLeft.y),  // top left
            Point(bottomRight.x, topRight.y),  // top right
            bottomRight  // bottom right
        )

        val inner: List<MatOfPoint> = listOf(
            innerShape
        )

        val outline: List<MatOfPoint> = listOf(
            outerShape
        )


        // var corners = arrayOf(bottomLeft, topLeft, topRight, bottomRight)
        // var transform = transformPerspective(originalImage, innerShape, outerShape, corners)
        // polylines(originalImage, rectangle, true, Scalar(0.0, 0.0, 255.0), 10)
        polylines(originalImage, inner, true, Scalar(255.0, 255.0, 0.0), 5)
        /// polylines(originalImage, outline, true, Scalar(0.0, 255.0, 0.0), 5)
        polylines(originalImage, clearance1, true, Scalar(0.0, 255.0, 0.0), 5)
        polylines(originalImage, clearance2, true, Scalar(0.0, 255.0, 0.0), 5)
        circle(originalImage, Point(startX, bottomLeft.y), 20, Scalar(0.0, 0.0, 250.0), 5)
        /*
        val srcMat = Mat(4, 1, CvType.CV_32FC2)
        val dstMat = Mat(4, 1, CvType.CV_32FC2)


        srcMat.put(0, 0, 50.0, 50.0)
        dstMat.put(0, 0, 50.0, 50.0)
        val perspectiveTransform = getPerspectiveTransform(srcMat, dstMat)

        val dst: Mat = originalImage.clone()

        warpPerspective(originalImage, dst, perspectiveTransform, Size(1600.0, 2500.0))
        */
        // Utils.matToBitmap(transform, bitmapImage)
        Utils.matToBitmap(originalImage, bitmapImage)
        mainImageView.setImageBitmap(bitmapImage)
    }

    fun getSlice(source: Mat): Mat {
        val height = source.height().toDouble()
        val width = source.width().toDouble()
        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(0.0, height), // bottom left
                Point(0.0, 450.0),  // top left
                Point(width, 450.0),  // top right
                Point(width, height)  // bottom right
            )
        )

        val mask = Mat.zeros(source.rows(), source.cols(), 0)
        fillPoly(mask, polygons, Scalar(255.0))

        val dest = Mat()
        bitwise_and(source, mask, dest)

        return dest
    }

    fun setImageViewListener(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> println(event.x.toString() + " " + event.y.toString())
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

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
        var matrix = getPerspectiveTransform(inner, outer)
        // transform the image
        var output = Mat()
        var point = Point(width, height)
        var transformed = warpPerspective(image, output, matrix, Size(200.0,300.0)) // i s outer correct here?
        // rotate? and return the result
        return output
    }

    /**
     * RECTANGLE SHAPE (BLUE)
     */
    /*
    val polygons: List<MatOfPoint> = listOf(
        MatOfPoint(
            Point(0.0, height - 200.0), // bottom left
            Point(450.0, 400.0),  // top left
            Point(450.0, 400.0),  // top right
            Point(width, height - 200.0)  // bottom right
        )
    )
    */
}