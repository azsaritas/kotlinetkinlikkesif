package com.azi.etkinlikkesifkotlin1

import android.content.Intent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class GirisFragment : Fragment() {

    private var _binding: FragmentGirisBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("420486296602-cqupenucv6g8qs13bd8shn83r5iefnau.apps.googleusercontent.com")  // Bu satır otomatik çalışacak
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButon.setOnClickListener{
            val action= GirisFragmentDirections.actionGirisFragmentToKayitFragment()
            Navigation.findNavController(view).navigate(action)
        }

        binding.girisButon.setOnClickListener{girisYap(it)}

        binding.googleGirisButon.setOnClickListener {
            // Önce Firebase'den çıkış yap
            auth.signOut()
            // Sonra Google girişini başlat
            googleIleGirisYap()
        }
        binding.sifremiUnuttumText.setOnClickListener {
            val email = binding.emailText.text.toString()

            if (email.isNotEmpty()) {
                // Email boş değilse şifre sıfırlama maili gönder
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        when (exception) {
                            is FirebaseAuthInvalidUserException -> {
                                // Kullanıcı bulunamadı
                                Toast.makeText(
                                    requireContext(),
                                    "Bu e-posta adresi ile kayıtlı bir kullanıcı bulunamadı.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                // Geçersiz email formatı
                                Toast.makeText(
                                    requireContext(),
                                    "Geçerli bir e-posta adresi giriniz.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                // Diğer hatalar
                                Toast.makeText(
                                    requireContext(),
                                    "Bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            } else {
                // Email boşsa uyarı göster
                Toast.makeText(
                    requireContext(),
                    "Lütfen e-posta adresinizi giriniz.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun googleIleGirisYap() {
        // Mevcut oturumu kapat
        googleSignInClient.signOut().addOnCompleteListener {
            // Yeni giriş intent'ini başlat
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    fun girisYap(view: View){
        println("giris butonu tiklandi")

        val email = binding.emailText.text.toString()
        val password= binding.sifreText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    // Burada MainActivity'deki onLoginSuccess() metodunu doğrudan çağırın
                    (requireActivity() as? MainActivity)?.onLoginSuccess()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Giriş Başarısız!", Toast.LENGTH_SHORT).show()
                }
        }
        else {
            Toast.makeText(requireContext(),"Geçersiz Email veya Şifre biçimi", Toast.LENGTH_LONG).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google giriş başarısız: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                (requireActivity() as? MainActivity)?.onLoginSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Kimlik doğrulama başarısız: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}