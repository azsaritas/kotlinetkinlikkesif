package com.azi.etkinlikkesifkotlin1

import com.google.android.gms.maps.model.LatLng

data class Event(
    val name: String,
    val date: String,
    val endDate: String,
    val capacity: Int?,
    val logoUrl: String?,
    val description: String?,
    val address: Address?,
    val categoryId: String?,
    val isFavorite: Boolean = false // Favori olup olmadığını belirten alan

// Logo URL
)

data class Address(
    val address_1: String? = null,
    val city: String? = null,
    val region: String? = null,
    val postal_code: String? = null,
    val country: String? = null,
    val latitude: String = "41.2588",  // Default değer ekledik
    val longitude: String = "39.8843", // Default değer ekledik
    val localized_address_display: String? = null
)