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
        resize(mainImage, resizedImage, Size(mainImage.width()*2.0, mainImage.height()*2.0))

        val grayscaleImage = Mat()
        cvtColor(resizedImage, grayscaleImage, COLOR_RGBA2GRAY)

        val blurredImage = Mat()
        GaussianBlur(grayscaleImage, blurredImage, Size(5.0, 5.0), 0.0)

        calculateLine(blurredImage, resizedImage, mainImageView, 50.0, 15.0, 50, 20.0, 20.0)
    }

    fun calculateLine(inputImage: Mat, originalImage: Mat, mainImageView: ImageView, cannyThreshold1: Double, cannyThreshold2: Double, lineThreshold: Int, minLineLength: Double, maxLineGap: Double) {
        val cannyImage = Mat()
        Canny(inputImage, cannyImage, cannyThreshold1, cannyThreshold2)

        var lineImage = Mat()
        HoughLinesP(cannyImage, lineImage, 1.0, PI/180, lineThreshold, minLineLength, maxLineGap)

        for (x in 0 until lineImage.cols()) {
            val vec: DoubleArray = lineImage.get(0, x)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]
            val start = Point(x1, y1)
            val end = Point(x2, y2)
            line(originalImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
        }

        val bitmapImage = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(originalImage, bitmapImage)

        mainImageView.setImageBitmap(bitmapImage)
    }
}