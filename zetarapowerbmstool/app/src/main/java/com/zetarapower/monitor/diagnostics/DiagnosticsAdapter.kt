package com.zetarapower.monitor.diagnostics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zetarapower.monitor.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for Diagnostics RecyclerView
 * Displays sections with parameters and events
 */
class DiagnosticsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DiagnosticsItem>()
    private val dateFormatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())

    companion object {
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_PARAMETER = 1
        private const val VIEW_TYPE_EVENT = 2
    }

    sealed class DiagnosticsItem {
        data class SectionHeader(val title: String) : DiagnosticsItem()
        data class Parameter(val title: String, val value: String) : DiagnosticsItem()
        data class Event(val event: DiagnosticsEvent) : DiagnosticsItem()
    }

    fun setItems(newItems: List<DiagnosticsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DiagnosticsItem.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is DiagnosticsItem.Parameter -> VIEW_TYPE_PARAMETER
            is DiagnosticsItem.Event -> VIEW_TYPE_EVENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION_HEADER -> {
                val view = inflater.inflate(R.layout.item_diagnostics_section_header, parent, false)
                SectionHeaderViewHolder(view)
            }
            VIEW_TYPE_PARAMETER -> {
                val view = inflater.inflate(R.layout.item_diagnostics_parameter, parent, false)
                ParameterViewHolder(view)
            }
            VIEW_TYPE_EVENT -> {
                val view = inflater.inflate(R.layout.item_diagnostics_event, parent, false)
                EventViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DiagnosticsItem.SectionHeader -> (holder as SectionHeaderViewHolder).bind(item)
            is DiagnosticsItem.Parameter -> (holder as ParameterViewHolder).bind(item)
            is DiagnosticsItem.Event -> (holder as EventViewHolder).bind(item, dateFormatter)
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolders

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.sectionTitle)

        fun bind(item: DiagnosticsItem.SectionHeader) {
            titleText.text = item.title
        }
    }

    class ParameterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.parameterTitle)
        private val valueText: TextView = itemView.findViewById(R.id.parameterValue)

        fun bind(item: DiagnosticsItem.Parameter) {
            titleText.text = item.title
            valueText.text = item.value
        }
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestampText: TextView = itemView.findViewById(R.id.eventTimestamp)
        private val typeText: TextView = itemView.findViewById(R.id.eventType)
        private val messageText: TextView = itemView.findViewById(R.id.eventMessage)

        fun bind(item: DiagnosticsItem.Event, dateFormatter: SimpleDateFormat) {
            val event = item.event
            timestampText.text = dateFormatter.format(event.timestamp)
            typeText.text = event.type.title
            messageText.text = event.message

            // Set color based on event type
            val colorRes = when (event.type) {
                DiagnosticsEvent.EventType.CONNECTION -> R.color.event_connection
                DiagnosticsEvent.EventType.DISCONNECTION -> R.color.event_disconnection
                DiagnosticsEvent.EventType.DATA_UPDATE -> R.color.event_data_update
                DiagnosticsEvent.EventType.ERROR -> R.color.event_error
            }
            typeText.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }
}
