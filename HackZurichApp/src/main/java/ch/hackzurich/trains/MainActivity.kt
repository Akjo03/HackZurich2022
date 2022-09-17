package ch.hackzurich.trains

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.bitwise_and
import org.opencv.imgproc.Imgproc.*
import java.lang.Math.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private var index = 0

    private var frameScale: Double = 2.0
    private var blurSize: Int = 5
    private var sigmaX: Double = 5.0
    private var cannyThreshold1: Double = 50.0
    private var cannyThreshold2: Double = 81.0
    private var lineResolution: Double = 1.0
    private var lineThreshold: Int = 300
    private var minLineLength: Double = 10.0
    private var maxLineGap: Double = 80.0

    private var clearanceScale: Double = 1.2

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        val mainImageView = findViewById<ImageView>(R.id.imageView)
        setImageViewListener(mainImageView)

        val nextFrameView = findViewById<Button>(R.id.nextFrameButton)
        val retriever = MediaMetadataRetriever()
        val mainVideo = Uri.parse("android.resource://$packageName/${R.raw.movie2}")
        retriever.setDataSource(this, mainVideo)

        updateFrame(retriever, mainImageView)
        try {
            nextFrameView.setOnClickListener {
                index += 60
                updateFrame(retriever, mainImageView)
            }
        } catch (ignored: Exception) {}
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun updateFrame(retriever: MediaMetadataRetriever, mainImageView: ImageView) {
        val currentFrameBitmap = retriever.getFrameAtIndex(index)
        val currentFrame = Mat()
        Utils.bitmapToMat(currentFrameBitmap, currentFrame)

        val resizedImage = Mat()
        resize(currentFrame, resizedImage, Size(currentFrame.width()*frameScale, currentFrame.height()*frameScale))
        val grayscaleImage = Mat()
        cvtColor(resizedImage, grayscaleImage, COLOR_RGBA2GRAY)
        val blurredImage = Mat()
        GaussianBlur(grayscaleImage, blurredImage, Size(blurSize.toDouble(), blurSize.toDouble()), sigmaX)
        calculateLine(getSlice(blurredImage), resizedImage, mainImageView)
    }

    private fun calculateLine(inputImage: Mat, originalImage: Mat, mainImageView: ImageView) {
        val topLeft = Point(356.96777, 724.9519)
        val topRight = Point(529.9695, 724.9519)
        val bottomLeft = Point(261.97998, 950.8967)
        val bottomRight = Point(625.94604, 950.8967)
        val startX = bottomLeft.x + ((bottomRight.x - bottomLeft.x) / 2).roundToInt()

        val cannyImage = Mat()
        Canny(inputImage, cannyImage, cannyThreshold1, cannyThreshold2)

        val lineImage = Mat()
        HoughLinesP(cannyImage, lineImage, lineResolution, PI/180, lineThreshold, minLineLength, maxLineGap)

        val recognizedTracks = mutableListOf<Pair<Point, Point>>()

        for (x in 0 until lineImage.rows()) {
            val vec: DoubleArray = lineImage.get(x, 0)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]
            val start = Point(x1, y1)
            val end = Point(x2, y2)
            // only draw lines that reach the top
            val min = y2.coerceAtMost(y1)
            println("y1: $y1")
            if (min < 600.0 && y1 != y2) {
                line(originalImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
                recognizedTracks.add(Pair(start, end))
            }
        }

        val bitmapImage = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.ARGB_8888)

        val clearance1 = getRightClearance(startX, bottomLeft.y, clearanceScale)
        val clearance2 = getLeftClearance(startX, bottomLeft.y, clearanceScale)


        val innerShape = MatOfPoint(
            bottomLeft, // bottom left
            topLeft,  // top left
            topRight,  // top right
            bottomRight  // bottom right
        )

        val inner: List<MatOfPoint> = listOf(
            innerShape
        )

        polylines(originalImage, inner, true, Scalar(255.0, 255.0, 0.0), 5)
        polylines(originalImage, clearance1, true, Scalar(0.0, 255.0, 0.0), 5)
        polylines(originalImage, clearance2, true, Scalar(0.0, 255.0, 0.0), 5)
        circle(originalImage, Point(startX, bottomLeft.y), 20, Scalar(0.0, 0.0, 250.0), 5)

        val anchorLine = calculateLineZero(recognizedTracks[0].second, recognizedTracks[0].first, recognizedTracks[1].first, recognizedTracks[1].second)
        line(originalImage, anchorLine.first, anchorLine.second, Scalar(255.0, 0.0, 0.0), 3)

        Utils.matToBitmap(originalImage, bitmapImage)
        mainImageView.setImageBitmap(bitmapImage)
    }

    private fun getSlice(source: Mat): Mat {
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

    @SuppressLint("ClickableViewAccessibility")
    fun setImageViewListener(imageView: ImageView) {
        imageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> println(event.x.toString() + " " + event.y.toString())
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    private fun calculateLineZero(o1: Point, p1: Point, o2: Point, p2: Point): Pair<Point, Point> {
        val avgOfYOrigins = listOf(o1.y, o2.y).average().roundToInt().toDouble()
        val avgOfYPoint = listOf(p1.y, p2.y).average().roundToInt().toDouble()
        val avgOfXOrigins = listOf(o1.x, o2.x).average().roundToInt().toDouble()
        val avgOfXPoint = listOf(p1.x, p2.x).average().roundToInt().toDouble()
        return Pair(Point(avgOfXOrigins, avgOfYOrigins), Point(avgOfXPoint, avgOfYPoint))
    }

    private fun getRightClearance(anchorX: Double, anchorY: Double, factor: Double): List<MatOfPoint> {
        return listOf(
            MatOfPoint(
                Point(anchorX, anchorY),
                Point(131.0 + anchorX, 9.0 * -1 + anchorY),
                Point(169.0 * factor + anchorX, 36.0 * -1 + anchorY),
                Point(169.0 * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * factor + anchorX, 333.0 * -1 * factor + anchorY),
                Point(166.0 * factor + anchorX, 430.0 * -1 * factor + anchorY),
                Point(102.2 * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * factor + anchorX, 600.0 * -1 * factor + anchorY),
                Point(0.0 * factor + anchorX, 600.0 * -1 * factor + anchorY)
            )
        )
    }

    private fun getLeftClearance(anchorX: Double, anchorY: Double, factor: Double): List<MatOfPoint> {
        return listOf(
            MatOfPoint(
                Point(anchorX, anchorY),
                Point(131.0 * -1 * factor + anchorX, 9.0 * -1 * factor + anchorY),
                Point(169.0 * -1 * factor + anchorX, 36.0 * -1 * factor + anchorY),
                Point(169.0 * -1 * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * -1 * factor + anchorX, 56.0 * -1 * factor + anchorY),
                Point(210.0 * -1 * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * -1 * factor + anchorX, 170.0 * -1 * factor + anchorY),
                Point(220.0 * -1 * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * -1 * factor + anchorX, 300.0 * -1 * factor + anchorY),
                Point(210.0 * -1 * factor + anchorX, 333.0 * -1 * factor + anchorY),
                Point(166.0 * -1 * factor + anchorX, 430.0 * -1 * factor + anchorY),
                Point(102.2 * -1 * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * -1 * factor + anchorX, 480.0 * -1 * factor + anchorY),
                Point(102.0 * -1 * factor + anchorX, 600.0 * -1 * factor + anchorY),
                Point(0.0 * -1 * factor + anchorX, 600.0 * -1 * factor + anchorY)
            )
        )
    }
}