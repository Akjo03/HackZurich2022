package ch.hackzurich.trains

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.bitwise_and
import org.opencv.imgproc.Imgproc.*
import java.lang.Math.PI
import java.lang.Math.min


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        val mainImageView = findViewById<ImageView>(R.id.imageView)

        val mainImage = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)

        val resizedImage = Mat()
        resize(mainImage, resizedImage, Size(mainImage.width()*2.0, mainImage.height()*2.0))

        val grayscaleImage = Mat()
        cvtColor(resizedImage, grayscaleImage, COLOR_RGBA2GRAY)

        val blurredImage = Mat()
        GaussianBlur(grayscaleImage, blurredImage, Size(5.0, 5.0), 0.0)

        calculateLine(getSlice(blurredImage), resizedImage, mainImageView, 50.0, 81.0, 300, 10.0, 80.0)
        /* THINGS FOR PERSPECTIVE WARPING
        val srcMat = Mat(4, 1, CvType.CV_32FC2)
        val dstMat = Mat(4, 1, CvType.CV_32FC2)


        srcMat.put(0, 0, 50.0, 50.0)
        dstMat.put(0, 0, 50.0, 50.0)
        val perspectiveTransform = getPerspectiveTransform(srcMat, dstMat)

        val dst: Mat = resizedImage.clone()

        warpPerspective(resizedImage, dst, perspectiveTransform, Size(1600.0, 2500.0)) */

    }

    fun calculateLine(inputImage: Mat, originalImage: Mat, mainImageView: ImageView, cannyThreshold1: Double, cannyThreshold2: Double, lineThreshold: Int, minLineLength: Double, maxLineGap: Double) {
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
            if (min < 600.0) {
                line(originalImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
            }
        }

        println("amount of lines drawn: " + lineImage.rows())

        val bitmapImage = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.ARGB_8888)

        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(0.0, originalImage.height().toDouble()), // bottom left
                Point(0.0, 450.0),  // top left
                Point(originalImage.width().toDouble(), 450.0),  // top right
                Point(originalImage.width().toDouble(), originalImage.height().toDouble())  // bottom right
            )
        )

        polylines(originalImage, polygons, true, Scalar(0.0, 0.0, 255.0), 10)
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