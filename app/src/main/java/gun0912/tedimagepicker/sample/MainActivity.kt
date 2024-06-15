package gun0912.tedimagepicker.sample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.firefly.fire_rx.FireDisposable.Companion.defaultSubscribe
import com.firefly.fire_rx.FireRx
import com.firefly.fire_rx.FireSingle.Companion.onSuccess
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.TedRxImagePicker

class MainActivity : AppCompatActivity() {

    private var selectedUriList: List<Uri>? = null
    private var mFireRx = FireRx(){
        Log.e("App", "Error", it)
    }

    private lateinit var btnNormalSingle: Button
    private lateinit var btnNormalMulti: Button
    private lateinit var btnRxSingle: Button
    private lateinit var btnRxMulti: Button
    private lateinit var btnRxMultiDropDown: Button
    private lateinit var ivImage: AppCompatImageView
    private lateinit var containerSelectedPhotos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNormalSingle = findViewById(R.id.btn_normal_single)
        btnNormalMulti = findViewById(R.id.btn_normal_multi)
        btnRxSingle = findViewById(R.id.btn_rx_single)
        btnRxMulti = findViewById(R.id.btn_rx_multi)
        btnRxMultiDropDown = findViewById(R.id.btn_rx_multi_drop_down)
        ivImage = findViewById(R.id.iv_image)
        containerSelectedPhotos = findViewById(R.id.container_selected_photos)

        setNormalSingleButton()
        setNormalMultiButton()
        setRxSingleButton()
        setRxMultiButton()
        setRxMultiDropDown()
    }


    private fun setNormalSingleButton() {
        btnNormalSingle.setOnClickListener {
            TedImagePicker.with(this)
                .start { uri -> showSingleImage(uri) }
        }
    }

    private fun setNormalMultiButton() {
        btnNormalMulti.setOnClickListener {
            TedImagePicker.with(this)
                //.mediaType(MediaType.IMAGE)
                //.scrollIndicatorDateFormat("YYYYMMDD")
                //.buttonGravity(ButtonGravity.BOTTOM)
                //.buttonBackground(R.drawable.btn_sample_done_button)
                //.buttonTextColor(R.color.sample_yellow)
                .errorListener { message -> Log.d("ted", "message: $message") }
                .cancelListener { Log.d("ted", "image select cancel") }
                .selectedUri(selectedUriList)
                .startMultiImage { list: List<Uri> -> showMultiImage(list) }
        }
    }

    private fun setRxSingleButton() {
        btnRxSingle.setOnClickListener {
            TedRxImagePicker.with(this)
                .start()
                .onSuccess(::showSingleImage)
                .defaultSubscribe(mFireRx)
        }
    }

    private fun setRxMultiButton() {
        btnRxMulti.setOnClickListener {
            TedRxImagePicker.with(this)
                .startMultiImage()
                .onSuccess(::showMultiImage)
                .defaultSubscribe(mFireRx)
        }
    }

    private fun setRxMultiDropDown() {
        btnRxMultiDropDown.setOnClickListener {
            TedRxImagePicker.with(this)
                .dropDownAlbum()
                .imageCountTextFormat("%s장")
                .startMultiImage()
                .onSuccess(::showMultiImage)
                .defaultSubscribe(mFireRx)
        }
    }

    private fun showSingleImage(uri: Uri) {
        ivImage.visibility = View.VISIBLE
        containerSelectedPhotos.visibility = View.GONE

        Glide.with(this)
            .load(uri)
            .into(ivImage)
    }


    private fun showMultiImage(uriList: List<Uri>) {
        this.selectedUriList = uriList
        Log.d("ted", "uriList: $uriList")
        ivImage.visibility = View.GONE
        containerSelectedPhotos.visibility = View.VISIBLE

        containerSelectedPhotos.removeAllViews()

        val viewSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics)
                .toInt()
        uriList.forEach {
            val itemImage = layoutInflater.inflate(R.layout.item_image, null, false) as AppCompatImageView

            Glide.with(this)
                .load(it)
                .into(itemImage)

            itemImage.layoutParams = FrameLayout.LayoutParams(viewSize, viewSize)
            containerSelectedPhotos.addView(itemImage)
        }

    }
}
