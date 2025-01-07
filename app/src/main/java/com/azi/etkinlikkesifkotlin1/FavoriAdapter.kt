package com.azi.etkinlikkesifkotlin1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val favoriteList: List<String>, // Favori etkinlik adları
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    class FavoritesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventName: TextView = view.findViewById(R.id.tvEventName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favori, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val eventName = favoriteList[position]
        holder.tvEventName.text = eventName
    }

    override fun getItemCount(): Int = favoriteList.size
}
