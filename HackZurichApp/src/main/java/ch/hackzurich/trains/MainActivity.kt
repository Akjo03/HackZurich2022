package ch.hackzurich.trains

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.addWeighted
import org.opencv.core.Core.bitwise_and
import org.opencv.imgproc.Imgproc.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        val mainImageView = findViewById<ImageView>(R.id.imageView)

        val mainImage = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)

        val resizedImage = Mat()
        resize(mainImage, resizedImage, Size(mainImage.width()*2.0, mainImage.height()*2.0))

        val cannyImage = getEdges(resizedImage, 100.0, 200.0, 5.0, 5.0)
        var lineImage = getLines(cannyImage, 2, 50, 100.0, 20.0)
        val visualized = visualize(resizedImage, lineImage, Scalar(0.0, 255.0, 0.0), 0.9, 1.0, 1.0)

        val bitmapImage = Bitmap.createBitmap(visualized.cols(), visualized.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(visualized, bitmapImage)

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

    fun getSlice(source: Mat): Mat {
        val height = source.height().toDouble()
        val width = source.width().toDouble()

        val polygons: List<MatOfPoint> = listOf(
            MatOfPoint(
                Point(100.0, height), // bottom left
                Point(450.0, 400.0),  // top left
                Point(900.0, 400.0),  // top right
                Point(width, height)  // bottom right
            )
        )

        val mask = Mat.zeros(source.rows(), source.cols(), 0)
        fillPoly(mask, polygons, Scalar(255.0))

        val dest = Mat()
        bitwise_and(source, mask, dest)

        return dest
    }


    fun getLines(source: Mat, resolutionFactor: Int, lineThreshold: Int, minLineLength: Double, maxLineGap: Double): Pair<HoughLine, HoughLine> {
        val lines = Mat()
        HoughLinesP(source, lines,resolutionFactor.toDouble(), Math.PI/180, lineThreshold, minLineLength, maxLineGap)

        val left = HoughLine(source)
        val right = HoughLine(source)

        for (row in 0 until lines.rows()) {
            val points: DoubleArray = lines.get(row, 0)
            val weighted = WeightedObservedPoints()
            val fitter = PolynomialCurveFitter.create(1)

            weighted.add(points[0], points[1])
            weighted.add(points[2], points[3])

            val fitted = fitter.fit(weighted.toList())
            val slope = fitted[1]

            if (slope < 0) {
                left.add(fitted)
            } else {
                right.add(fitted)
            }
        }

        return Pair(left, right)
    }

    fun visualize(source: Mat, lines: Pair<HoughLine, HoughLine>, lineColor: Scalar, alpha: Double, beta: Double, gamma: Double): Mat {
        val grey = Mat.zeros(source.rows(), source.cols(), 0)
        val dest = Mat()
        cvtColor(grey, dest, COLOR_GRAY2RGB)

        line(dest, lines.first.coordinates.first, lines.first.coordinates.second, lineColor, LINE_8)
        line(dest, lines.second.coordinates.first, lines.second.coordinates.second, lineColor, LINE_8)

        val done = Mat()
        addWeighted(source, alpha, dest, beta, gamma, done)

        return done
    }
}