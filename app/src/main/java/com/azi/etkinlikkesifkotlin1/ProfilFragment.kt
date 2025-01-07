package com.azi.etkinlikkesifkotlin1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesAdapter: FavoritesAdapter


    private lateinit var rvFavorites: RecyclerView

    private lateinit var etUserName: EditText
    private lateinit var etEmail: EditText
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var btnSaveProfile: Button
    private lateinit var btnLogout: Button

    private var favoriteList: MutableList<String> = mutableListOf()
    private lateinit var katililanEtkinliklerAdapter: KatililanEtkinliklerAdapter
    private val joinedEventsList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Firebase initializations
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        rvFavorites = view.findViewById(R.id.favoriteEventsRecyclerView)

        etUserName = view.findViewById(R.id.usernameEditText)
        etEmail = view.findViewById(R.id.emailEditText)
        switchNotifications = view.findViewById(R.id.pushNotificationsSwitch)
        btnSaveProfile = view.findViewById(R.id.saveProfileButton)
        btnLogout = view.findViewById(R.id.logoutButton)

        // RecyclerView setup
        rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        favoritesAdapter = FavoritesAdapter(favoriteList)
        rvFavorites.adapter = favoritesAdapter

        // RecyclerView ayarları
        val joinedEventsRecyclerView = view.findViewById<RecyclerView>(R.id.joinedEventsRecyclerView)
        joinedEventsRecyclerView.layoutManager = LinearLayoutManager(context)
        katililanEtkinliklerAdapter = KatililanEtkinliklerAdapter(joinedEventsList)
        joinedEventsRecyclerView.adapter = katililanEtkinliklerAdapter

        // Katılınan etkinlikleri yükle
        loadJoinedEvents()
        // Set user email
        val currentUser = auth.currentUser
        etEmail.setText(currentUser?.email)

        // Load profile data
        loadProfileData()

        // Load favorites
        loadFavorites()

        // Save profile data
        btnSaveProfile.setOnClickListener {
            saveProfileData()
        }

        // Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Çıkış yapıldı.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_girisFragment)
        }
    }
    private fun loadJoinedEvents() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .collection("joinedEvents")
            .get()
            .addOnSuccessListener { documents ->
                joinedEventsList.clear()
                for (document in documents) {
                    val eventName = document.getString("eventName")
                    eventName?.let {
                        joinedEventsList.add(it)
                    }
                }
                katililanEtkinliklerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Etkinlikler yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    etUserName.setText(document.getString("userName"))
                    switchNotifications.isChecked = document.getBoolean("notifications") == true
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileData() {
        val userId = auth.currentUser?.uid ?: return
        val userName = etUserName.text.toString().trim()
        val notificationsEnabled = switchNotifications.isChecked

        if (userName.isEmpty()) {
            Toast.makeText(requireContext(), "Kullanıcı adı boş bırakılamaz.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "userName" to userName,
            "notifications" to notificationsEnabled
        )

        firestore.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profil güncellendi!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("favorites").get()
            .addOnSuccessListener { querySnapshot ->
                favoriteList.clear()
                for (document in querySnapshot.documents) {
                    favoriteList.add(document.id)
                }
                favoritesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Favoriler yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
