package com.azi.etkinlikkesifkotlin1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    val eventList = mutableListOf<Event>()

    // Etkinlikleri Eventbrite API'den çekmek için bir işlev
    fun fetchEvents(token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val events = EventbriteApi.getEvents(token) // Eventbrite API çağrısı
                eventList.clear()
                eventList.addAll(events)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Bir hata oluştu.")
            }
        }
    }
}
