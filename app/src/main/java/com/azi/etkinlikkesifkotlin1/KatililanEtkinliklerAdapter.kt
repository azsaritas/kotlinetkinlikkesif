package com.azi.etkinlikkesifkotlin1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KatililanEtkinliklerAdapter(
    private val joinedEventsList: List<String>
) : RecyclerView.Adapter<KatililanEtkinliklerAdapter.JoinedEventsViewHolder>() {

    class JoinedEventsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventName: TextView = view.findViewById(R.id.tvEventName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinedEventsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_katililanetkinlik, parent, false)
        return JoinedEventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: JoinedEventsViewHolder, position: Int) {
        val eventName = joinedEventsList[position]
        holder.tvEventName.text = eventName
    }

    override fun getItemCount(): Int = joinedEventsList.size
}