package ch.hackzurich.trains

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.IOException
import java.sql.Types


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()

        var img: Mat? = null
        try {
            img = Utils.loadResource(this, R.drawable.movie_moment, CvType.CV_8UC4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
