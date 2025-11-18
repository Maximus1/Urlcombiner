package com.maxxtools.urlcombiner // Bitte durch deinen echten Paketnamen ersetzen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Ein Adapter, um die Liste der BaseUrlItem-Objekte in einem RecyclerView anzuzeigen.
 * Verwendet ListAdapter für eine effiziente Aktualisierung der Liste.
 */
class UrlAdapter(
    private val onEditClick: (BaseUrlItem) -> Unit,
    private val onDeleteClick: (BaseUrlItem) -> Unit
) : ListAdapter<BaseUrlItem, UrlAdapter.UrlViewHolder>(UrlDiffCallback()) {

    /**
     * Erstellt eine neue ViewHolder-Instanz, indem das Layout für ein einzelnes Listenelement
     * aus der XML-Datei geladen wird.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_url, parent, false)
        return UrlViewHolder(view, onEditClick, onDeleteClick)
    }

    /**
     * Bindet die Daten eines bestimmten BaseUrlItem-Objekts an die Views im ViewHolder.
     */
    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    /**
     * Der ViewHolder hält die Referenzen auf die Views für ein einzelnes Listenelement.
     */
    class UrlViewHolder(
        itemView: View,
        private val onEditClick: (BaseUrlItem) -> Unit,
        private val onDeleteClick: (BaseUrlItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val urlTextView: TextView = itemView.findViewById(R.id.textViewUrl)
        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(item: BaseUrlItem) {
            nameTextView.text = item.name
            urlTextView.text = item.url
            editButton.setOnClickListener { onEditClick(item) }
            deleteButton.setOnClickListener { onDeleteClick(item) }
        }
    }

    /**
     * DiffUtil.ItemCallback hilft dem ListAdapter zu bestimmen, welche Elemente in der Liste
     * sich geändert haben, um nur die notwendigen UI-Updates durchzuführen.
     */
    class UrlDiffCallback : DiffUtil.ItemCallback<BaseUrlItem>() {
        override fun areItemsTheSame(oldItem: BaseUrlItem, newItem: BaseUrlItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BaseUrlItem, newItem: BaseUrlItem): Boolean = oldItem == newItem
    }
}