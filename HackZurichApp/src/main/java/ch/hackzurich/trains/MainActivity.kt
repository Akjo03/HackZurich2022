package ch.hackzurich.trains

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.opencv.android.*
import org.opencv.android.OpenCVLoader
import org.opencv.core.*

import org.opencv.core.Core.*
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI


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

        val cannyImage = Mat()
        Canny(blurredImage, cannyImage, 50.0, 150.0)

        // var lineImage = Mat()
        // HoughLinesP(cannyImage, lineImage, 1.0, PI/180, 50, 20.0, 20.0)

        val bitmapImage = Bitmap.createBitmap(cannyImage.cols(), cannyImage.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(cannyImage, bitmapImage)

        mainImageView.setImageBitmap(bitmapImage)
    }
}