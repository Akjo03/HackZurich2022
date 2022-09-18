package ch.hackzurich.trains

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.polylines
import kotlin.math.roundToInt

class ImageProcessor(val imageToProcess: Mat) {
    lateinit var preProcessedImage: Mat
    lateinit var blurredImage: Mat
    lateinit var imageWithContours: Mat
    val recognizedTracks = mutableListOf<Pair<Point, Point>>()
    lateinit var anchorLine: Pair<Point, Point>
    fun preProcessImage() {
        var resizedImage = Mat()
        Imgproc.resize(imageToProcess, resizedImage, Size(imageToProcess.width() * 2.0, imageToProcess.height() * 2.0))

        val grayscaleImage = Mat()
        Imgproc.cvtColor(resizedImage, grayscaleImage, Imgproc.COLOR_RGBA2GRAY)

        blurredImage = Mat()
        Imgproc.GaussianBlur(grayscaleImage, blurredImage, Size(5.0, 5.0), 0.0)
        this.preProcessedImage = resizedImage
        this.imageWithContours = preProcessedImage.clone()
    }

    fun extractTrainTrackData() {
        var processableSlice = getSlice(blurredImage)
        calculateLine(processableSlice)
    }


    fun calculateLine(
        inputImage: Mat,
        cannyThreshold1: Double = 50.0,
        cannyThreshold2: Double = 81.0,
        lineThreshold: Int = 300,
        minLineLength: Double = 10.0,
        maxLineGap: Double = 80.0
    ) {
        val cannyImage = Mat()
        Imgproc.Canny(inputImage, cannyImage, cannyThreshold1, cannyThreshold2)

        var lineImage = Mat()
        Imgproc.HoughLinesP(cannyImage, lineImage, 1.0, Math.PI / 180, lineThreshold, minLineLength, maxLineGap)


        for (x in 0 until lineImage.rows()) {
            val vec: DoubleArray = lineImage.get(x, 0)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]
            val start = Point(x1, y1)
            val end = Point(x2, y2)
            // only draw lines that reach the top
            val min = Math.min(y2, y1);
            println("y1:" + y1)
            if (min < 600.0 && y1 != y2) {
                Imgproc.line(preProcessedImage, start, end, Scalar(255.0, 0.0, 0.0), 3)
                recognizedTracks.add(GeometryUtil.ensurePairOfCorrectOrientation(start, end))

            }
        }

        println("RecognizedTracksSize: ${recognizedTracks.size}")
        anchorLine = calculateClearanceAnchorLine(
            recognizedTracks[0].first,
            recognizedTracks[0].second,
            recognizedTracks[1].first,
            recognizedTracks[1].second
        )
        Imgproc.line(preProcessedImage, anchorLine.first, anchorLine.second, Scalar(255.0, 0.0, 0.0), 3)


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
        Imgproc.fillPoly(mask, polygons, Scalar(255.0))

        val dest = Mat()
        Core.bitwise_and(source, mask, dest)

        return dest
    }

    private fun calculateClearanceAnchorLine(o1: Point, p1: Point, o2: Point, p2: Point): Pair<Point, Point> {
        val avgOfYOrigins = listOf(o1.y, o2.y).average().roundToInt().toDouble()
        val avgOfYPoint = listOf(p1.y, p2.y).average().roundToInt().toDouble()
        val avgOfXOrigins = listOf(o1.x, o2.x).average().roundToInt().toDouble()
        val avgOfXPoint = listOf(p1.x, p2.x).average().roundToInt().toDouble()
        return Pair(Point(avgOfXOrigins, avgOfYOrigins), Point(avgOfXPoint, avgOfYPoint))
    }

    fun drawContourOnImageFreshImage(vararg contours: List<MatOfPoint>){
        imageWithContours = preProcessedImage.clone()
        for(contour in contours) {
            polylines(imageWithContours, contour, true, Scalar(0.0, 255.0, 0.0), 5)
        }
    }

    fun getImageWithContourAsBitmap(): Bitmap?{
        return convertToBitmap(imageWithContours)
    }

    fun getImageWithoutContourAsBitmap(): Bitmap? {
        return convertToBitmap(preProcessedImage)
    }


    private fun convertToBitmap(image: Mat): Bitmap? {
        val bitmapImage = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bitmapImage)
        return bitmapImage;
    }

}
