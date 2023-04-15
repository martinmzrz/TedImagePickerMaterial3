package gun0912.tedimagepicker.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView


internal abstract class BaseViewHolder<D>(view: View) : RecyclerView.ViewHolder(view) {
    protected val context: Context = itemView.context

    abstract fun bind(data: D)

    open fun recycled() {

    }
}