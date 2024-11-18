package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityLoginBinding
    private lateinit var authHandler: AuthHandler
    private lateinit var firestoreHandler: FirestoreHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        addListenerRegisterHyperLink()
        addListenerSignInBtn()
        addListenerSignInWithGoogleBtn()
    }

    private fun addListenerRegisterHyperLink() {
        viewBinding.loginTvRegisterhyperlink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addListenerSignInBtn() {
        viewBinding.loginBtnLoginbtn.setOnClickListener(View.OnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val user = hashMapOf(
                    "emailAddress" to viewBinding.loginEtEmail.text.toString(),
                    "password" to viewBinding.loginEtPassword.text.toString()
                )

                if (areAllFieldsValid(user)) {
                    authHandler = AuthHandler.getInstance(this@LoginActivity)!!
                    val loggingInStatus = authHandler.loginAccount(user["emailAddress"]!!, user["password"]!!)
                    if(loggingInStatus == "Success") {
                        setWarningMessage(R.string.warning_user_not_found, View.GONE)

                        val toast = Toast.makeText(this@LoginActivity, "User successfully logged in.", Toast.LENGTH_SHORT)
                        toast.show()

                        val intent : Intent = if(user["emailAddress"] == "Admin") { // TODO MCO3: Change this based on your designated admin email
                            Intent(this@LoginActivity, AdminTransactionsActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, MainActivity::class.java)
                        }
                        startActivity(intent)
//                        authHandler.logoutAccount()
                        finish()
                    } else if(loggingInStatus == "User not found") {
                        setWarningMessage(R.string.warning_user_not_found, View.VISIBLE)
                    } else if(loggingInStatus == "Invalid credentials") {
                        setWarningMessage(R.string.warning_invalid_credentials, View.VISIBLE)
                    }
                }
            }
        })
    }

    private fun addListenerSignInWithGoogleBtn() {
        // TODO MCO3: Add Google Handling
        viewBinding.loginBtnSigningoogle.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
//            authHandler.logoutAccount()
            finish()
        })
    }

    private fun areAllFieldsValid(user: HashMap<String, String>) : Boolean {
        if(!areAllFieldsFilled(user)) {
            setWarningMessage(R.string.warning_incomplete_fields, View.VISIBLE)
        } else if(!isEmailAddValid(user["emailAddress"]!!)) {
            setWarningMessage(R.string.warning_invalid_email, View.VISIBLE)
        } else {
            setWarningMessage(R.string.warning_invalid_email, View.GONE)
            return true
        }
        return false
    }

    private fun areAllFieldsFilled(user: HashMap<String, String>): Boolean {
        return !(user["emailAddress"] == "" ||
                user["password"] == "")
    }

    private fun isEmailAddValid(emailAdd: String) : Boolean {
        val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        return emailRegex.matches(emailAdd)
    }

    private fun setWarningMessage(message: Int, visibility: Int) {
        viewBinding.loginTvWarning.text = viewBinding.root.context.getString(message)
        viewBinding.loginTvWarning.visibility = visibility
    }
}