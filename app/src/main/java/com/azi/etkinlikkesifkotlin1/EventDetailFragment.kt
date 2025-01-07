package com.azi.etkinlikkesifkotlin1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EventDetailFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private var isFavorite = false
    private val eventName: String? by lazy { arguments?.getString("eventName") }
    private var isJoined = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val eventName = arguments?.getString("eventName")
        val eventDate = arguments?.getString("eventDate")
        val eventEndDateDetail= arguments?.getString("eventEndDateDetail")
        val eventCapacity = arguments?.getString("eventCapacity")
        val eventLogoUrl = arguments?.getString("eventLogoUrl")
        val eventDescriptionDetail = arguments?.getString("eventDescriptionDetail") // Yeni eklenen description
        val eventCategoryIdDetail = arguments?.getString("eventCategoryIdDetail")

        view.findViewById<TextView>(R.id.eventNameDetail).text = eventName
        view.findViewById<TextView>(R.id.eventDateDetail).text = "Başlangıç Tarihi: $eventDate"
        view.findViewById<TextView>(R.id.eventEndDateDetail).text = "Bitiş Tarihi: $eventEndDateDetail"
        view.findViewById<TextView>(R.id.eventCapacityDetail).text = "Kapasite: $eventCapacity"
        view.findViewById<TextView>(R.id.eventDescriptionDetail).text = eventDescriptionDetail // description ekleniyor
        view.findViewById<TextView>(R.id.eventCategoryIdDetail).text= "Kategori No: $eventCategoryIdDetail"

        Log.d("EventDetail", "Category ID: ${arguments?.getString("eventCategoryId")}")

        val eventLogo = view.findViewById<ImageView>(R.id.eventLogoDetail)
        Glide.with(requireContext())
            .load(eventLogoUrl)
            .into(eventLogo)


        val navigateButton = view.findViewById<Button>(R.id.navigateButton)
        val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButtonDetail)
        val joinEventButton = view.findViewById<MaterialButton>(R.id.joinEventButton)

        // Etkinliğe katılım durumunu kontrol et
        checkIfJoined { isJoined ->
            this.isJoined = isJoined
            updateJoinButton(joinEventButton)
        }

        joinEventButton.setOnClickListener {
            if (isJoined) {
                leaveEvent()
            } else {
                joinEvent()
            }
            isJoined = !isJoined
            updateJoinButton(joinEventButton)
        }


        checkIfFavorite { isFavorite ->
            this.isFavorite = isFavorite
            updateFavoriteButton(favoriteButton)
        }

        favoriteButton.setOnClickListener {
            if (isFavorite) {
                removeFromFavorites()
                favoriteButton.setImageResource(R.drawable.favorirenksiz)
            } else {
                addToFavorites()
                favoriteButton.setImageResource(R.drawable.favorirenkli)
            }
            isFavorite = !isFavorite
        }


        navigateButton.setOnClickListener {
            val lat = arguments?.getDouble("eventLatitude")
            val lng = arguments?.getDouble("eventLongitude")

            if (lat != null && lng != null) {
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } else {
                Toast.makeText(requireContext(), "Konum bilgisi alınamadı", Toast.LENGTH_SHORT).show()
            }
            Log.d("EventDetail", "Lat: $lat, Lng: $lng")
        }


        super.onViewCreated(view, savedInstanceState)


    }
    private fun updateJoinButton(button: MaterialButton) {
        if (isJoined) {
            button.text = "Katılımdan Çık"
            button.setIconTintResource(R.color.cikisrengi)
            button.setTextColor(resources.getColor(R.color.cikisrengi))
        } else {
            button.text = "Etkinliğe Katıl"
            button.setIconTintResource(R.color.white)
            button.setTextColor(resources.getColor(R.color.white))
        }
    }
    private fun joinEvent() {
        val event = eventName ?: return
        val user = currentUser ?: return

        val eventData = hashMapOf(
            "eventName" to event,
            "joinedAt" to System.currentTimeMillis(),
            "userId" to user.uid,
            "userEmail" to user.email
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("joinedEvents")
            .document(event)
            .set(eventData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Etkinliğe başarıyla katıldınız!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun leaveEvent() {
        val event = eventName ?: return
        val user = currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("joinedEvents")
            .document(event)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Etkinlikten ayrıldınız.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfJoined(callback: (Boolean) -> Unit) {
        val user = currentUser
        val event = eventName

        if (user != null && event != null) {
            firestore.collection("users")
                .document(user.uid)
                .collection("joinedEvents")
                .document(event)
                .get()
                .addOnSuccessListener { document ->
                    callback(document.exists())
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
        } else {
            callback(false)
        }
    }
    private fun updateFavoriteButton(favoriteButton: ImageButton) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.favorirenkli)
        } else {
            favoriteButton.setImageResource(R.drawable.favorirenksiz)
        }
    }
    private fun addToFavorites() {
        // Ensure that currentUser and eventName are not null before proceeding
        val event = eventName ?: return // If eventName is null, exit the method
        val user = currentUser ?: return // If currentUser is null, exit the method

        val userFavoritesRef = firestore.collection("users")
            .document(user.uid)
            .collection("favorites")

        val favoriteEvent = hashMapOf("eventName" to event)

        userFavoritesRef.document(event).set(favoriteEvent)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "$event favorilere eklendi!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun removeFromFavorites() {
        // Ensure that currentUser and eventName are not null before proceeding
        val event = eventName ?: return // If eventName is null, exit the method
        val user = currentUser ?: return // If currentUser is null, exit the method

        val userFavoritesRef = firestore.collection("users")
            .document(user.uid)
            .collection("favorites")

        // Use eventName safely now that we are sure it is not null
        userFavoritesRef.document(event).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "$event favorilerden çıkartıldı!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfFavorite(callback: (Boolean) -> Unit) {
        if (currentUser != null && eventName != null) {
            val userFavoritesRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("favorites")
                .document(eventName!!)

            userFavoritesRef.get()
                .addOnSuccessListener { document ->
                    callback(document.exists()) // Eğer belge varsa, bu etkinlik favoridir.
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            callback(false)
        }
    }


}
