package com.azi.etkinlikkesifkotlin1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object EventbriteApi {
    suspend fun getEvents(token: String): List<Event> = withContext(Dispatchers.IO) {
        //val url = URL("https://www.eventbriteapi.com/v3/events/search/")
        val url = URL("https://www.eventbriteapi.com/v3/organizations/2567098408431/events/")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $token")

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val events = mutableListOf<Event>()
            val jsonArray = JSONObject(response).getJSONArray("events")
            for (i in 0 until jsonArray.length()) {
                val eventJson = jsonArray.getJSONObject(i)
                val name = eventJson.getJSONObject("name").getString("text")
                val date = eventJson.getJSONObject("start").getString("local")
                val endDate = eventJson.getJSONObject("end").getString("local")
                val capacity = eventJson.optInt("capacity", -1) // capacity yoksa -1 döner.
                val logoUrl = eventJson.optJSONObject("logo")?.optJSONObject("original")?.optString("url")
                val description = eventJson.optJSONObject("description")?.getString("text")
                val venueId = eventJson.getString("venue_id")
                val categoryId =eventJson.getString("category_id")
                val venue = fetchVenueDetails(venueId)

                events.add(Event(name, date, endDate, capacity, logoUrl, description, address = venue,categoryId))

            }
            events
        } else {
            throw Exception("API çağrısı başarısız oldu. Kod: ${connection.responseCode}")
        }
    }
    fun fetchVenueDetails(venueId: String): Address? {
        val url = URL("https://www.eventbriteapi.com/v3/venues/$venueId/?token=SXO4K7J3E4JU427WY7ZW")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val venueJson = JSONObject(response)
            val addressJson = venueJson.optJSONObject("address")

            return addressJson?.let {
                Address(
                    address_1 = it.optString("address_1"),
                    city = it.optString("city"),
                    region = it.optString("region"),
                    postal_code = it.optString("postal_code"),
                    country = it.optString("country"),
                    latitude = it.optString("latitude"),
                    longitude = it.optString("longitude"),
                    localized_address_display = it.optString("localized_address_display")
                )
            }
        } else {
            throw Exception("Mekan bilgileri API çağrısı başarısız oldu. Kod: ${connection.responseCode}")
        }
    }
}
