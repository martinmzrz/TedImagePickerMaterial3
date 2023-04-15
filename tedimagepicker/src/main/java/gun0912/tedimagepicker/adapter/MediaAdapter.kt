package gun0912.tedimagepicker.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.facebook.drawee.view.SimpleDraweeView
import com.firefly.viewutils.gone
import com.firefly.viewutils.visible
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseSimpleHeaderAdapter
import gun0912.tedimagepicker.base.BaseViewHolder
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.builder.type.SelectType
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.util.ToastUtil
import gun0912.tedimagepicker.zoom.TedImageZoomActivity

internal class MediaAdapter(
    private val activity: Activity,
    private val builder: TedImagePickerBaseBuilder<*>,
) : BaseSimpleHeaderAdapter<Media>(if (builder.showCameraTile) 1 else 0) {

    internal val selectedUriList: MutableList<Uri> = mutableListOf()
    var onMediaAddListener: (() -> Unit)? = null

    override fun getHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_camera, parent, false)
        return CameraViewHolder(view)
    }
    override fun getItemViewHolder(parent: ViewGroup) : ImageViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_media, parent, false)
        return ImageViewHolder(view)
    }

    fun toggleMediaSelect(uri: Uri) {
        if (selectedUriList.contains(uri)) {
            removeMedia(uri)
        } else {
            addMedia(uri)
        }
    }

    private fun addMedia(uri: Uri) {
        if (selectedUriList.size == builder.maxCount) {
            val message =
                builder.maxCountMessage ?: activity.getString(builder.maxCountMessageResId)
            ToastUtil.showToast(message)
        } else {
            selectedUriList.add(uri)
            onMediaAddListener?.invoke()
            refreshSelectedView()
        }
    }

    private fun getViewPosition(it: Uri): Int =
        items.indexOfFirst { media -> media.uri == it } + headerCount


    private fun removeMedia(uri: Uri) {
        val position = getViewPosition(uri)
        selectedUriList.remove(uri)
        notifyItemChanged(position)
        refreshSelectedView()
    }

    private fun refreshSelectedView() {
        selectedUriList.forEach {
            val position: Int = getViewPosition(it)
            notifyItemChanged(position)
        }
    }

    inner class ImageViewHolder(view: View) : BaseViewHolder<Media>(view) {
        private val viewZoomOut = view.findViewById<Button>(R.id.view_zoom_out)
        private val multiSelectionFrame = view.findViewById<FrameLayout>(R.id.multi_selection_frame)
        private val selectedNumber = view.findViewById<TextView>(R.id.selected_number)
        private val duration = view.findViewById<TextView>(R.id.tv_duration)
        private val ivImage = view.findViewById<SimpleDraweeView>(R.id.iv_image)


        init {
            viewZoomOut.setOnClickListener {
                val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                    ?: return@setOnClickListener)
                startZoomActivity(item)
            }

            if(builder.selectType == SelectType.MULTI){
                multiSelectionFrame.visible()
            } else {
                multiSelectionFrame.gone()
            }

            viewZoomOut.gone()
        }

        override fun bind(data: Media) {

            ivImage.setImageURI(data.uri.toString())

            if(selectedUriList.contains(data.uri)){
                viewZoomOut.isClickable = false
                selectedNumber.text = (selectedUriList.indexOf(data.uri) + 1).toString()
                multiSelectionFrame.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.ted_image_picker_selected_foreground))
                selectedNumber.setBackgroundResource(R.drawable.bg_multi_image_selected)
            } else {
                viewZoomOut.isClickable = true
                selectedNumber.text = ""
                multiSelectionFrame.background = null
                selectedNumber.setBackgroundResource(R.drawable.bg_multi_image_unselected)
            }

            if(builder.showZoomIndicator && data is Media.Image){
                viewZoomOut.visible()
            } else {
                viewZoomOut.gone()
            }

            if(builder.showVideoDuration && data is Media.Video){
                duration.visible()
            } else {
                duration.gone()
            }

            if(data is Media.Video){
                duration.text = data.durationText
            }
        }

        override fun recycled() {
            if (activity.isDestroyed) {
                return
            }

            ivImage.setImageURI("")
        }

        private fun startZoomActivity(media: Media) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                ivImage,
                media.uri.toString()
            ).toBundle()

            activity.startActivity(TedImageZoomActivity.getIntent(activity, media.uri), options)

        }
    }

    inner class CameraViewHolder(view: View) : HeaderViewHolder(view) {
        private val ivImage = view.findViewById<AppCompatImageView>(R.id.iv_image)

        init {
            ivImage.setImageResource(builder.cameraTileImageResId)
            itemView.setBackgroundResource(builder.cameraTileBackgroundResId)
        }

    }

}
