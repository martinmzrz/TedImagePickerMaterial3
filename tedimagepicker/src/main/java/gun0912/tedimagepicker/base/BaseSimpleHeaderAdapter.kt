package gun0912.tedimagepicker.base

import android.view.View
import android.view.ViewGroup

internal abstract class BaseSimpleHeaderAdapter<D>(protected val headerCount: Int = HEADER_COUNT) :
    BaseRecyclerViewAdapter<D, BaseViewHolder<D>>(headerCount) {

    abstract fun getItemViewHolder(parent: ViewGroup): BaseViewHolder<D>
    abstract fun getHeaderViewHolder(parent: ViewGroup): HeaderViewHolder


    override fun getViewHolder(
        parent: ViewGroup,
        viewType: ViewType
    ): BaseViewHolder<D> {
        return when (viewType) {
            ViewType.HEADER -> getHeaderViewHolder(parent)
            ViewType.ITEM -> getItemViewHolder(parent)
        }
    }

    open inner class HeaderViewHolder(view: View) : BaseViewHolder<D>(view) {

        override fun bind(data: D) {
            // no-op
        }
    }

    companion object {
        private const val HEADER_COUNT = 1
    }


}