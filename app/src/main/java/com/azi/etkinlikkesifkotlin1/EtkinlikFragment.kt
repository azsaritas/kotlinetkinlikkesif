package com.azi.etkinlikkesifkotlin1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class EtkinlikFragment : Fragment() {

    private val eventViewModel: EventViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter
    private lateinit var events: List<Event> // Tüm etkinlikleri saklayacağız

    private val originalList = mutableListOf<Event>() // Tüm etkinlikler
    private val filteredList = mutableListOf<Event>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_etkinlik, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val searchEditText: EditText = view.findViewById(R.id.searchEditText)
        val filterButton: MaterialButton = view.findViewById(R.id.filterButton)

        // Adapter'i ayarla ve tıklama olayını yönet
        eventAdapter = EventAdapter(eventViewModel.eventList) { event ->
            val bundle = Bundle().apply {
                putString("eventName", event.name)
                putString("eventDate", event.date)
                putString("eventEndDateDetail", event.endDate)
                putString("eventCapacity", event.capacity?.toString())
                putString("eventLogoUrl", event.logoUrl)
                putString("eventDescriptionDetail",event.description)
                putString("eventCategoryIdDetail",event.categoryId)
                val latitude = event.address?.latitude?.toDoubleOrNull() ?: 41.41
                val longitude = event.address?.longitude?.toDoubleOrNull() ?: 39.39
                putDouble("eventLatitude", latitude)
                putDouble("eventLongitude", longitude)
            }

            // EtkinlikBilgiFragment'e geçiş
            val fragment = EventDetailFragment()
            fragment.arguments = bundle

            findNavController().navigate(
                R.id.action_etkinlikFragment_to_eventDetailFragment,
                bundle
            )
        }
        recyclerView.adapter = eventAdapter


        val token = "SXO4K7J3E4JU427WY7ZW"
        eventViewModel.fetchEvents(token, {
            originalList.clear()
            originalList.addAll(eventViewModel.eventList)
            filteredList.clear()
            filteredList.addAll(originalList)
            eventAdapter.notifyDataSetChanged()
        }, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterBySearchTerm(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        filterButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), filterButton)
            popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.filter_music -> filterByCategory("103") // Müzik
                    R.id.filter_theater -> filterByCategory("105") // Tiyatro
                    R.id.seminer_filtresi -> filterByCategory("102")
                    R.id.egitim_filtresi -> filterByCategory("115")
                    R.id.gezi_filtresi -> filterByCategory("109")
                    R.id.is_filtresi -> filterByCategory("101")
                    R.id.filter_all -> filterByCategory(null) // Tüm kategoriler
                }
                true
            }
            popupMenu.show()
        }

    }
    private fun filterBySearchTerm(term: String) {
        val searchResults = if (term.isEmpty()) {
            originalList
        } else {
            originalList.filter { event ->
                event.name.contains(term, ignoreCase = true) ||
                        event.description?.contains(term, ignoreCase = true) == true
            }
        }
        eventAdapter.setData(searchResults) // Adaptörü güncelle
    }

    private fun filterByCategory(categoryId: String?) {
        val filteredResults = if (categoryId == null) {
            originalList
        } else {
            originalList.filter { event -> event.categoryId == categoryId }
        }
        eventAdapter.setData(filteredResults) // Adaptörü güncelle
    }

}


