package gun0912.tedimagepicker.sample


import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}
