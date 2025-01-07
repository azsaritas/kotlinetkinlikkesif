package com.azi.etkinlikkesifkotlin1

import android.graphics.Path.Direction
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.azi.etkinlikkesifkotlin1.databinding.FragmentGirisBinding
import com.azi.etkinlikkesifkotlin1.databinding.FragmentKayitBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class KayitFragment : Fragment() {

    private var _binding: FragmentKayitBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitOlButonu.setOnClickListener{kayitOl(it)}
    }

    fun kayitOl(view:View){
        println("kayit butonu tiklandi")

        val email = binding.emailKayitText.text.toString()
        val password= binding.sifreKayitText.text.toString()
        val password2 = binding.sifreKayit2Text.text.toString()


        if(email.isNotEmpty() && password.isNotEmpty() && password==password2) {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                    task -> if (task.isSuccessful){
                //kullanıcı oluşturuldu
                val action= KayitFragmentDirections.actionKayitFragmentToGirisFragment()
                Navigation.findNavController(view).navigate(action)
            }
            }.addOnFailureListener { exception -> Toast.makeText(requireContext(),exception.localizedMessage,
                Toast.LENGTH_LONG).show()
            }

        }
        else {
            Toast.makeText(requireContext(), "Hatalı Kayıt", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}