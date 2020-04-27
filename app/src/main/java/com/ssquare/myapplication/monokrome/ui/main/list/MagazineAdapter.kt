package com.ssquare.myapplication.monokrome.ui.main.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssquare.myapplication.monokrome.data.Header
import com.ssquare.myapplication.monokrome.data.Magazine
import com.ssquare.myapplication.monokrome.databinding.HeaderLayoutBinding
import com.ssquare.myapplication.monokrome.databinding.ListItemBinding
import com.ssquare.myapplication.monokrome.util.ClickAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class MagazineAdapter(
    private val magazineListener: MagazineListener,
    private val headerListener: HeaderListener
) :
    ListAdapter<MagazineAdapter.DataItem, RecyclerView.ViewHolder>(MagazineDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<Magazine>?, header: Header?) {
        adapterScope.launch {
            val items = when {
                (header != null && list != null) ->
                    listOf(DataItem.HeaderItem(header)) + list.map { DataItem.MagazineItem(it) }
                (header == null && list != null) -> list.map { DataItem.MagazineItem(it) }
                (header != null && list == null) -> listOf(DataItem.HeaderItem(header))
                else -> emptyList()
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val magazineItem = getItem(position) as DataItem.MagazineItem
                holder.bind(magazineItem.magazine, magazineListener)
            }
            is HeaderViewHolder -> {
                val headerItem = getItem(position) as DataItem.HeaderItem
                holder.bind(headerListener, headerItem.header)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is DataItem.MagazineItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class HeaderViewHolder(val binding: HeaderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = HeaderLayoutBinding.inflate(inflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }

        fun bind(clickListener: HeaderListener, header: Header) {
            binding.clickListener = clickListener
            binding.header = header
        }
    }

    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(magazineItem: Magazine, clickListener: MagazineListener) {
            binding.clickListener = clickListener
            binding.magazine = magazineItem

            binding.executePendingBindings()
        }

    }


    class MagazineDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    class MagazineListener(val clickListener: (magazine: Magazine, action: ClickAction) -> Unit) {
        fun onClick(magazine: Magazine, action: ClickAction) = clickListener(magazine, action)
    }

    class HeaderListener(val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    sealed class DataItem {
        data class MagazineItem(val magazine: Magazine) : DataItem() {
            override val id = magazine.id
        }

        data class HeaderItem(val header: Header) : DataItem() {
            override val id = Long.MIN_VALUE
        }

        abstract val id: Long
    }
}