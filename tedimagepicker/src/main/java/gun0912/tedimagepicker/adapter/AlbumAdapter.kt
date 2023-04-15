package gun0912.tedimagepicker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.firefly.viewutils.gone
import com.firefly.viewutils.visible
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseRecyclerViewAdapter
import gun0912.tedimagepicker.base.BaseViewHolder
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.model.Album
import gun0912.tedimagepicker.util.TextFormatUtil

internal class AlbumAdapter(private val builder: TedImagePickerBaseBuilder<*>) : BaseRecyclerViewAdapter<Album, AlbumAdapter.AlbumViewHolder>() {

    private var selectedPosition = 0

    override fun getViewHolder(parent: ViewGroup, viewType: ViewType): AlbumViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    fun setSelectedAlbum(album: Album) {
        val index = items.indexOf(album)
        if (index >= 0 && selectedPosition != index) {
            val lastSelectedPosition = selectedPosition
            selectedPosition = index
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    inner class AlbumViewHolder(view: View) : BaseViewHolder<Album>(view) {
        private val ivImage: SimpleDraweeView = view.findViewById(R.id.iv_image)
        private val selectedForeground: View = view.findViewById(R.id.selected_foreground)
        private val tvName: TextView = view.findViewById(R.id.tv_name)
        private val tvCount: TextView = view.findViewById(R.id.tv_count)

        override fun bind(data: Album) {
            ivImage.setImageURI(data.thumbnailUri.toString())

            if(adapterPosition == selectedPosition){
                selectedForeground.visible()
            } else {
                selectedForeground.gone()
            }

            tvName.text = data.name
            tvCount.text = TextFormatUtil.getMediaCountText(builder.imageCountFormat, data.mediaCount)
        }

        override fun recycled() {
            ivImage.setImageURI("")
        }
    }
}