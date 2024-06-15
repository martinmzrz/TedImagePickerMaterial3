package gun0912.tedimagepicker.zoom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar
import gun0912.tedimagepicker.R

internal class TedImageZoomActivity : AppCompatActivity() {
    private lateinit var uri: Uri

    private var ivMedia: PhotoView? = null
    private var toolbar: MaterialToolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSavedInstanceState(savedInstanceState)
        setContentView(R.layout.activity_zoom_out)
        ivMedia = findViewById(R.id.iv_media)
        toolbar = findViewById(R.id.materialToolbar)

        toolbar?.setNavigationOnClickListener { finish() }
        toolbar?.title = getFileNameFromUri(uri)
        Glide.with(this)
            .load(uri)
            .into(ivMedia!!)
    }

    private fun setSavedInstanceState(savedInstanceState: Bundle?) {

        val bundle: Bundle? = when {
            savedInstanceState != null -> savedInstanceState
            else -> intent.extras
        }

        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             bundle?.getParcelable(EXTRA_URI, Uri::class.java) ?: return finish()
        } else {
            @Suppress("DEPRECATION")
            bundle?.getParcelable(EXTRA_URI) ?: return finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_URI, uri)
        super.onSaveInstanceState(outState)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val fileName: String?
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1

        return if (columnIndex > -1) {
            fileName = cursor?.getString(columnIndex)
            cursor?.close()
            fileName
        } else {
            null
        }
    }

    companion object {
        private const val EXTRA_URI = "EXTRA_URI"
        fun getIntent(context: Context, uri: Uri) =
            Intent(context, TedImageZoomActivity::class.java)
                .apply {
                    putExtra(EXTRA_URI, uri)
                }
    }
}