package gun0912.tedimagepicker.sample


import androidx.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)
    }

}
