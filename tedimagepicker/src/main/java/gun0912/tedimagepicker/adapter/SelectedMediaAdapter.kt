package gun0912.tedimagepicker.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseRecyclerViewAdapter
import gun0912.tedimagepicker.base.BaseViewHolder

internal class SelectedMediaAdapter :
    BaseRecyclerViewAdapter<Uri, SelectedMediaAdapter.MediaViewHolder>() {

    var onClearClickListener: ((Uri) -> Unit)? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun getViewHolder(parent: ViewGroup, viewType: ViewType) : MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager = recyclerView.layoutManager
    }


    inner class MediaViewHolder(view:View) : BaseViewHolder<Uri>(view) {
        private val ivImage = view.findViewById<AppCompatImageView>(R.id.iv_image)
        private val ivClear = view.findViewById<AppCompatImageView>(R.id.iv_clear)

        init {
            ivClear.setOnClickListener {
                val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                    ?: return@setOnClickListener)
                onClearClickListener?.invoke(item)
            }
        }

        override fun bind(data: Uri) {
            Glide.with(itemView.context)
                .load(data)
                .into(ivImage)
        }

        override fun recycled() {
            if ((itemView.context as? Activity)?.isDestroyed == true) {
                return
            }
            ivImage.setImageDrawable(null)
        }
    }

}