package com.vexo.app

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.vexo.app.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (validateEmail(email)) {
                sendPasswordReset(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.layoutEmail.error = getString(R.string.error_empty_email)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.layoutEmail.error = getString(R.string.error_invalid_email)
                false
            }
            else -> {
                binding.layoutEmail.error = null
                true
            }
        }
    }

    private fun sendPasswordReset(email: String) {
        binding.loading.visibility = View.VISIBLE
        binding.btnResetPassword.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.loading.visibility = View.GONE
                binding.btnResetPassword.isEnabled = true
                
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.reset_password_sent), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        binding.layoutEmail.error = getString(R.string.error_user_not_found)
                    } else {
                        Toast.makeText(this, "Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}