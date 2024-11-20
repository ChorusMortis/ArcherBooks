package com.mobdeve.s12.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.mobdeve.s12.mco.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

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
        viewBinding.loginBtnLoginbtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val user = hashMapOf(
                    "emailAddress" to viewBinding.loginEtEmail.text.toString(),
                    "password" to viewBinding.loginEtPassword.text.toString()
                )

                if (!areAllFieldsValid(user)) {
                    return@launch
                }

                authHandler = AuthHandler.getInstance(this@LoginActivity)!!
                val loggingInStatus = authHandler.loginAccount(user["emailAddress"]!!, user["password"]!!)
                if(loggingInStatus == "Success") {
                    setWarningMessage(R.string.warning_user_not_found, View.GONE)

                    showLoginSuccessToast()

                    val intent : Intent = if(user["emailAddress"] == "Admin") { // TODO MCO3: Change this based on your designated admin email
                        Intent(this@LoginActivity, AdminTransactionsActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, MainActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else if(loggingInStatus == "User not found") {
                    setWarningMessage(R.string.warning_user_not_found, View.VISIBLE)
                } else if(loggingInStatus == "Invalid credentials") {
                    setWarningMessage(R.string.warning_invalid_credentials, View.VISIBLE)
                } else {
                    setWarningMessage(R.string.warning_generic_login_fail, View.VISIBLE)
                }
            }
        }
    }

    private fun addListenerSignInWithGoogleBtn() {
        viewBinding.loginBtnSigningoogle.setOnClickListener {
            // create credential option for Google specifically
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.server_client_id))
                .setAutoSelectEnabled(false)
                .setNonce(getNonce())
                .build()

            // create credential request for Credential Manager
            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(this)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity,
                    )
                    // Google credential obtained, handle it
                    Log.d("LoginActivity", "Getting credentials success")
                    handleGoogleSignin(result)
                } catch (e: GetCredentialCancellationException) {
                    Log.w("LoginActivity", "$e, doing nothing")
                    // do nothing since it's not really a bad thing to cancel
                } catch (e: GetCredentialException) {
                    Log.e("LoginActivity", e.toString())
                    setWarningMessage(R.string.warning_google_login_fail, View.VISIBLE)
                }
            }
        }
    }

    private fun handleGoogleSignin(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // get Google auth credential from Google credential
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                // sign into Google account with Google auth credential
                googleSignIn(googleCredential)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("FirebaseAuth", "Received an invalid google id token response", e)
                setWarningMessage(R.string.warning_google_login_fail, View.VISIBLE)
            }
        } else {
            Log.e("LoginActivity", "Unexpected type of credential")
            setWarningMessage(R.string.warning_google_login_fail, View.VISIBLE)
        }
    }

    private fun googleSignIn(googleCredential: AuthCredential) {
        authHandler = AuthHandler.getInstance(this@LoginActivity)!!
        // signs into existing Google account, creating Google account if it doesn't exist yet
        // NOTE: if user registered using email/pw with the same email exists, account auth
        // provider changes into Google only (only Google can be used to sign into it)
        authHandler.authSignInWithCredential(googleCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // hide any error if sign in with Google is successful
                Log.d("FirebaseAuth", "Firebase auth sign in success")
                viewBinding.loginTvWarning.visibility = View.GONE

                // get authenticated user and its email, exit early if email does not exist
                val user = task.result?.user
                val email = user?.email ?: return@addOnCompleteListener

                // create Google account in database (if it doesn't exist yet) and go to MainActivity
                createGoogleAccountInDb(user, email)
            } else {
                Log.e("FirebaseAuth", "Firebase auth sign in fail", task.exception)
                setWarningMessage(R.string.warning_google_login_fail, View.VISIBLE)
            }
        }
    }

    private fun createGoogleAccountInDb(user: FirebaseUser, email: String) {
        // split names (assume last word/name is last name, the rest is first name)
        val names = user.displayName?.let { splitName(it) }
        val firstName = names?.first
        val lastName = names?.second

        CoroutineScope(Dispatchers.Main).launch {
            firestoreHandler = FirestoreHandler.getInstance(this@LoginActivity)!!
            // check if user entry in collection with the same email already exists
            val existingUser = firestoreHandler.getUserByEmail(email)
            // create user entry in Firebase db if user entry with same email does not exist
            if (firstName != null && lastName != null && existingUser == null) {
                val newUser = UserModel(user.uid, firstName, lastName, email, UserModel.SignUpMethod.GOOGLE)
                firestoreHandler.createUser(newUser)
            }
            showLoginSuccessToast()
            // start activity regardless of whether user entry was written into db
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
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

    private fun showLoginSuccessToast() {
        Toast.makeText(this@LoginActivity, "User successfully logged in.", Toast.LENGTH_SHORT).show()
    }

    private fun splitName(fullName: String): Pair<String, String> {
        val names = fullName.split(" ")
        val firstName = names.dropLast(1).joinToString(" ")
        // assume the last word/name is only the last name
        val lastName = names.lastOrNull() ?: ""
        return Pair(firstName, lastName)
    }

    private fun getNonce(): String {
        val nonceBytes = UUID.randomUUID().toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(nonceBytes)
        val hash = digest.fold("") {str, it -> str + "%02x".format(it)}
        return hash
    }
}