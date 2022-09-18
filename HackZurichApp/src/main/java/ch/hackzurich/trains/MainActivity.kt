package ch.hackzurich.trains

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.bitwise_and
import org.opencv.imgproc.Imgproc.*
import java.lang.Math.*
import kotlin.math.max
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    lateinit var mainImageView: ImageView;
    lateinit var imageProcessor: ImageProcessor;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()
        val mainImage = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)
        imageProcessor = ImageProcessor(mainImage);
        mainImageView = findViewById(R.id.imageView)
        setImageViewListener(mainImageView)
        findViewById<Button>(R.id.button).setOnClickListener { reset() }

        imageProcessor.preProcessImage()
        imageProcessor.extractTrainTrackData()
        reDrawImage()

    }
    fun reset() {
        val bitmap = imageProcessor.getImageWithoutContourAsBitmap()
        mainImageView.setImageBitmap(bitmap)
    }

    fun reDrawImage(){
        val bitmap = imageProcessor.getImageWithContourAsBitmap()
        mainImageView.setImageBitmap(bitmap)
    }

    fun recalculateTrainClearanceShape(y: Double) {
        val anchorLine = imageProcessor.anchorLine
        val lineOfUserClick = StandardFormLine(0.0, y)
        val fromPoints = StandardFormLine.fromPoints(anchorLine.first, anchorLine.second)
        val intersect = GeometryUtil.calculateIntersectionPoint(fromPoints.a, fromPoints.c, lineOfUserClick.a, lineOfUserClick.c)
        val d1 = GeometryUtil.getDistance(intersect, imageProcessor.recognizedTracks[0].first, imageProcessor.recognizedTracks[0].second)
        val d2 = GeometryUtil.getDistance(intersect, imageProcessor.recognizedTracks[1].first, imageProcessor.recognizedTracks[1].second)
        val averageDistance = listOf(d2,d1).average()
        val factor = averageDistance / 100
        val clearance1 = ClearanceShapeFactory.getLeftClearance(intersect, factor)
        val clearance2 = ClearanceShapeFactory.getRightClearance(intersect, factor)
        imageProcessor.drawContourOnImageFreshImage(clearance1, clearance2)
        reDrawImage()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setImageViewListener(imageView: ImageView) {
        imageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    recalculateTrainClearanceShape(event!!.y.toDouble())
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

}
