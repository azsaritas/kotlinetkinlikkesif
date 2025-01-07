package com.azi.etkinlikkesifkotlin1

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject

// MapFragment.kt
class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private val eventViewModel: EventViewModel by viewModels()
    private var mapReady = false
    private var eventsLoaded = false
    private var pendingEvents = mutableListOf<Event>()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: Location? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FusedLocationProviderClient'ı başlat
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // API'den verileri çek
        val token = "SXO4K7J3E4JU427WY7ZW"
        eventViewModel.fetchEvents(token, {
            pendingEvents.clear()
            pendingEvents.addAll(eventViewModel.eventList)
            eventsLoaded = true
            if (mapReady) {
                addMarkersToMap()
            }
        }, { error ->
            Toast.makeText(requireContext(), "Hata: $error", Toast.LENGTH_LONG).show()
        })
    }


        @SuppressLint("MissingPermission")
        private fun getCurrentLocationAndAddMarker() {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                } ?: run {
                    Log.e("MapFragment", "Current location is null")
                }
            }
        }

        private fun drawRouteToEvent(eventLocation: LatLng) {
            currentLocation?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                val url = getDirectionUrl(currentLatLng, eventLocation)
                val request = Request.Builder().url(url).build()

                OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        Log.e("MapFragment", "Direction API call failed: ${e.message}")
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            responseData?.let {
                                val routes = parseDirectionsResponse(it)
                                activity?.runOnUiThread {
                                    googleMap.addPolyline(PolylineOptions().addAll(routes).color(Color.BLUE))
                                }
                            }
                        } else {
                            Log.e("MapFragment", "Direction API call unsuccessful")
                        }
                    }
                })
            }
        }

        private fun getDirectionUrl(origin: LatLng, dest: LatLng): String {
            val originParam = "origin=${origin.latitude},${origin.longitude}"
            val destParam = "destination=${dest.latitude},${dest.longitude}"
            val keyParam = "key=AIzaSyDouC17OoLPotp2rAIvF-ejKPmkMr085nY" // Replace with your actual Google Directions API key
            return "https://maps.googleapis.com/maps/api/directions/json?$originParam&$destParam&$keyParam"
        }

        private fun parseDirectionsResponse(response: String): List<LatLng> {
            val jsonResponse = JSONObject(response)
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            val path = mutableListOf<LatLng>()
            for (i in 0 until steps.length()) {
                val step = steps.getJSONObject(i)
                val polyline = step.getJSONObject("polyline").getString("points")
                path.addAll(decodePolyline(polyline))
            }
            return path
        }

        private fun decodePolyline(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].code - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val p = LatLng((lat / 1E5), (lng / 1E5))
                poly.add(p)
            }
            return poly
        }



    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapReady = true

        // Kullanıcının mevcut konumunu al ve marker ekle
        getCurrentLocationAndAddMarker()

        if (eventsLoaded) {
            addMarkersToMap()
        }

        setupMarkerClickListener()
    }

    private fun addMarkersToMap() {
        if (!::googleMap.isInitialized) return

        // Debug için log
        Log.d("MapFragment", "Adding markers. Event count: ${pendingEvents.size}")

        // Default konum (Erzurum)
        var defaultLocation = LatLng(41.2588, 39.8843)
        var hasValidMarker = false

        pendingEvents.forEach { event ->
            event.address?.let { address ->
                if (address.latitude.isNotEmpty() && address.longitude.isNotEmpty()) {
                    try {
                        val lat = address.latitude.toDouble()
                        val lng = address.longitude.toDouble()
                        val position = LatLng(lat, lng)

                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(event.name)
                                .snippet(address.localized_address_display ?: "")
                        )
                        marker?.tag = event

                        if (!hasValidMarker) {
                            defaultLocation = position
                            hasValidMarker = true
                        }
                    } catch (e: Exception) {
                        Log.e("MapFragment", "Error adding marker for ${event.name}: ${e.message}")
                    }
                }
            }
        }

        // Haritayı uygun konuma zoom yap
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    private fun setupMarkerClickListener() {
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()

            // Eğer bu marker current location değilse ve event marker'ı ise
            if (marker.title != "Current Location") {
                // Mevcut konumdan seçilen etkinliğe rota çiz
                marker.position?.let { eventLocation ->
                    drawRouteToEvent(eventLocation)
                }
            }
            true
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            val event = marker.tag as? Event
            event?.let {
                val bundle = Bundle().apply {
                    putString("eventName", it.name)
                    putString("eventDate", it.date)
                    putString("eventCapacity", it.capacity?.toString())
                    putString("eventLogoUrl", it.logoUrl)
                    putString("eventDescriptionDetail", it.description)
                    val latitude = it.address?.latitude?.toDoubleOrNull() ?: 41.41
                    val longitude = it.address?.longitude?.toDoubleOrNull() ?: 39.39
                    putDouble("eventLatitude", latitude)
                    putDouble("eventLongitude", longitude)
          }
                try {
                    findNavController().navigate(R.id.action_mapFragment_to_eventDetailFragment, bundle)
                } catch (e: Exception) {
                    Log.e("MapFragment", "Navigation error: ${e.message}")
                    Toast.makeText(requireContext(), "Detay sayfasına geçilemedi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}