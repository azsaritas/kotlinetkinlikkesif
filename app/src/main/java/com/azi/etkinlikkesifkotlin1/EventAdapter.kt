package com.azi.etkinlikkesifkotlin1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class EventAdapter(
    private var eventList: List<Event>, // Bu artık 'var' olarak tanımlandı
    private val onItemClick: (Event) -> Unit, // Tıklama olayını yakalamak için lambda
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val eventCapacity: TextView = itemView.findViewById(R.id.eventCapacity)
        val eventLogo: ImageView = itemView.findViewById(R.id.eventLogo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventCapacity.text = event.capacity?.let {
            "Kapasite: $it"
        } ?: "Kapasite belirtilmedi"

        Glide.with(holder.itemView.context)
            .load(event.logoUrl)
            .into(holder.eventLogo)

        // Tıklama olayını ayarla
        holder.itemView.setOnClickListener {
            onItemClick(event) // Lambda'yı çağır
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    // Listeyi güncelleyen metot
    fun setData(newEventList: List<Event>) {
        eventList = newEventList
        notifyDataSetChanged() // RecyclerView'a değişiklikleri bildir
    }


}
