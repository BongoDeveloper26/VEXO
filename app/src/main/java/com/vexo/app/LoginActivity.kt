package com.vexo.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.vexo.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val PREFS_NAME = "LoginPrefs"
    private val KEY_REMEMBER = "remember_me"
    private val KEY_EMAIL = "saved_email"
    private val KEY_PASSWORD = "saved_password"

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("GoogleLogin", "Error code: ${e.statusCode}")
                Toast.makeText(this, "Error de Google (${e.statusCode}): ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("GoogleLogin", "Result Code no es OK: ${result.resultCode}")
            Toast.makeText(this, "No se pudo completar el inicio con Google. Revisa tu SHA-1 en Firebase.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loadSavedCredentials()
        setupButtons()
    }

    private fun loadSavedCredentials() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isRemembered = prefs.getBoolean(KEY_REMEMBER, false)
        
        if (isRemembered) {
            binding.cbRememberMe.isChecked = true
            binding.etEmail.setText(prefs.getString(KEY_EMAIL, ""))
            binding.etPassword.setText(prefs.getString(KEY_PASSWORD, ""))
        }
    }

    private fun saveCredentials(email: String, pass: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        if (binding.cbRememberMe.isChecked) {
            editor.putBoolean(KEY_REMEMBER, true)
            editor.putString(KEY_EMAIL, email)
            editor.putString(KEY_PASSWORD, pass)
        } else {
            editor.clear()
        }
        editor.apply()
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.loading.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.loading.visibility = View.GONE
                        if (task.isSuccessful) {
                            saveCredentials(email, password)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.loading.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.loading.visibility = View.GONE
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error de Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}