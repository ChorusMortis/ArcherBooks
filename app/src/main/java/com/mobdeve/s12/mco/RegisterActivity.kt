package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Visibility
import com.mobdeve.s12.mco.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerLoginHyperLink()
        addListenerCreateAccountBtn()
        addListenerSignUpWithGoogleBtn()
    }

    private fun addListenerLoginHyperLink() {
        viewBinding.registerTvLoginhyperlink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addListenerCreateAccountBtn() {
        viewBinding.registerBtnRegisterbtn.setOnClickListener(View.OnClickListener {
            if(allFieldsValid()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun addListenerSignUpWithGoogleBtn() {
        // TODO MCO3: Add Google Handling
        viewBinding.registerBtnSignupgoogle.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
    }

    private fun allFieldsValid() : Boolean {
        if(!areAllFieldsFilled()) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_incomplete_fields)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isEmailAddValid()) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_invalid_email)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isEmailUnique()){
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_email_already_exists)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isPasswordValid()) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_short_password)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!arePasswordsMatching()) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_password_mismatch)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else {
            viewBinding.registerTvWarning.visibility = View.GONE
            return true
        }

        return false
    }

    private fun areAllFieldsFilled() : Boolean {
        if(viewBinding.registerEtFullname.text.toString() == "" ||
            viewBinding.registerEtEmail.text.toString() == "" ||
            viewBinding.registerEtPassword.text.toString() == "" ||
            viewBinding.registerEtConfirmpassword.text.toString() == "") {
            return false
        } else {
            return true
        }
    }

    private fun isEmailAddValid() : Boolean {
        val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        return emailRegex.matches(viewBinding.registerEtEmail.text.toString())
    }

    private fun isEmailUnique() : Boolean {
        // TODO MCO3: Access database
        return true
    }

    private fun isPasswordValid() : Boolean {
        val strongPasswordRegex = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")
        val passwordRegex = Regex("^.{8,}$")
        return passwordRegex.matches(viewBinding.registerEtPassword.text.toString())
    }

    private fun arePasswordsMatching() : Boolean {
        return (viewBinding.registerEtPassword.text.toString() == viewBinding.registerEtConfirmpassword.text.toString())
    }
}