package ch.hackzurich.trains

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import java.lang.Math.PI


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        val mainImageView = findViewById<ImageView>(R.id.imageView)

        val mainImage = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)

        val resizedImage = Mat()
        resize(mainImage, resizedImage, Size(mainImage.width()*2.4, mainImage.height()*2.4))

        val cannyImage = getEdges(resizedImage, 100.0, 150.0, 5.0, 5.0)

        calculateLine(cannyImage, resizedImage, mainImageView, 300, 20.0, 500.0)
    }

    fun calculateLine(cannyImage: Mat, originalImage: Mat, mainImageView: ImageView, lineThreshold: Int, minLineLength: Double, maxLineGap: Double) {
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
            line(originalImage, start, end, Scalar(255.0, 0.0, 0.0), 5)
        }

        val bitmapImage = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.ARGB_8888)

        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(0.0, originalImage.height().toDouble()), // bottom left
                Point(450.0, 400.0),  // top left
                Point(450.0, 400.0),  // top right
                Point( originalImage.width().toDouble(), originalImage.height().toDouble())  // bottom right
            )
        )

        polylines(originalImage, polygons, true, Scalar(0.0, 0.0, 255.0), 5)
        Utils.matToBitmap(originalImage, bitmapImage)

        mainImageView.setImageBitmap(bitmapImage)
    }

    fun getEdges(source: Mat, threshold1: Double, threshold2: Double, blurSize: Double, sigmaX: Double): Mat {
        val gray = Mat()
        cvtColor(source, gray, COLOR_RGB2GRAY)

        val blur = Mat()
        GaussianBlur(gray, blur, Size(blurSize, blurSize), sigmaX)

        val dest = Mat()
        Canny(blur, dest, threshold1, threshold2)

        return dest
    }
}