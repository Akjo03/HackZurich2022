package ch.hackzurich.trains

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.*
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

        val cannyImage = Mat()
        Canny(blurredImage, cannyImage, 50.0, 150.0)

        var lineImage = Mat()
        HoughLinesP(cannyImage, lineImage, 1.0, PI/180, 50, 20.0, 20.0)

        for (x in 0 until lineImage.cols()) {
            val vec: DoubleArray = lineImage.get(0, x)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]
            val start = Point(x1, y1)
            val end = Point(x2, y2)
            line(resizedImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
        }

        val bitmapImage = Bitmap.createBitmap(resizedImage.cols(), resizedImage.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(resizedImage, bitmapImage)

        mainImageView.setImageBitmap(bitmapImage)
    }
}