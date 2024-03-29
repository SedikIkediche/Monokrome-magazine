package com.ssquare.myapplication.monokrome.ui.main.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssquare.myapplication.monokrome.data.DomainHeader
import com.ssquare.myapplication.monokrome.data.DomainMagazine
import com.ssquare.myapplication.monokrome.databinding.HeaderLayoutBinding
import com.ssquare.myapplication.monokrome.databinding.ListItemBinding
import com.ssquare.myapplication.monokrome.util.ClickAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class MagazineAdapter(
    private val magazineListener: MagazineListener,
    private val headerListener: HeaderListener
) :
    ListAdapter<MagazineAdapter.DataItem, RecyclerView.ViewHolder>(MagazineDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var items : MutableList<DataItem>?  = null
    fun addHeaderAndSubmitList(list: List<DomainMagazine>?, header: DomainHeader?) {
        Timber.d("Raw list: $list")
        adapterScope.launch {
            items = when {
                (header != null && list != null) ->
                    listOf(DataItem.HeaderItem(header)) + list.map { DataItem.MagazineItem(it) }
                (header == null && list != null) -> list.map { DataItem.MagazineItem(it) }
                (header != null && list == null) -> listOf(DataItem.HeaderItem(header))
                else -> emptyList()
            }.toMutableList()
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

        fun bind(clickListener: HeaderListener, header: DomainHeader) {
            binding.clickListener = clickListener
            binding.header = header
        }
    }

    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }


        fun bind(magazine: DomainMagazine, clickListener: MagazineListener) {


            binding.magazine = magazine
            binding.clickListener = clickListener
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

    class MagazineListener(val clickListener: (magazine: DomainMagazine, action: ClickAction) -> Unit) {
        fun onClick(
            magazine: DomainMagazine,
            action: ClickAction
        ) = clickListener(magazine, action)
    }

    class HeaderListener(val clickListener: () -> Unit) {
        fun onClick() = clickListener()
    }

    sealed class DataItem {
        data class MagazineItem(val magazine: DomainMagazine) : DataItem() {
            override val id = magazine.id
        }

        data class HeaderItem(val header: DomainHeader) : DataItem() {
            override val id = Long.MIN_VALUE
        }

        abstract val id: Long
    }
}