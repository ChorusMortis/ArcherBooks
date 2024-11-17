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
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.mobdeve.s12.mco.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewBinding : ActivityRegisterBinding
    private lateinit var authHandler: AuthHandler
    private lateinit var firestoreHandler: FirestoreHandler

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
        viewBinding.registerBtnRegisterbtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val newUser = UserModel(viewBinding.registerEtFirstname.text.toString(),
                                        viewBinding.registerEtLastname.text.toString(),
                                        viewBinding.registerEtEmail.text.toString())
                val passwords = hashMapOf(
                    "password" to viewBinding.registerEtPassword.text.toString(),
                    "confirm_password" to viewBinding.registerEtConfirmpassword.text.toString()
                )

                if (areAllFieldsValid(newUser, passwords)) {
                    authHandler = AuthHandler.getInstance(this@RegisterActivity)!!
                    val userId = authHandler.createAccount(newUser.emailAddress, passwords["password"]!!, this@RegisterActivity)

                    firestoreHandler = FirestoreHandler.getInstance(this@RegisterActivity)!!
                    newUser.userId = userId
                    firestoreHandler.createUser(newUser)

                    val toast = Toast.makeText(this@RegisterActivity, "Account successfully created!", Toast.LENGTH_SHORT)
                    toast.show()

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun addListenerSignUpWithGoogleBtn() {
        viewBinding.registerBtnSignupgoogle.setOnClickListener {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.server_client_id))
                .setAutoSelectEnabled(true)
                .setNonce(getNonce())
            .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(this)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@RegisterActivity,
                    )
                    Log.d("RegisterActivity", "Getting credentials success")
                    handleGoogleSignup(result)
                } catch (e: GetCredentialException) {
                    Log.e("RegisterActivity", e.toString())
                    showGoogleSignupWarning()
                }
            }
        }
    }

    private suspend fun handleGoogleSignup(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(googleCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseAuth", "Firebase auth sign in success")
                        viewBinding.registerTvWarning.visibility = View.GONE
                        // TODO: create account in database

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("FirebaseAuth", "Firebase auth sign in fail", task.exception)
                        showGoogleSignupWarning()
                    }
                }.await()

            } catch (e: GoogleIdTokenParsingException) {
                Log.e("FirebaseAuth", "Received an invalid google id token response", e)
                showGoogleSignupWarning()
            }
        } else {
            Log.e("RegisterActivity", "Unexpected type of credential")
            showGoogleSignupWarning()
        }
    }

    private fun showGoogleSignupWarning() {
        viewBinding.registerTvWarning.text = getString(R.string.warning_google_register_fail)
        viewBinding.registerTvWarning.visibility = View.VISIBLE
    }

    private suspend fun areAllFieldsValid(newUser: UserModel, passwords: HashMap<String, String>) : Boolean {
        if(!areAllFieldsFilled(newUser, passwords)) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_incomplete_fields)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isEmailAddValid(newUser.emailAddress)) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_invalid_email)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isEmailUnique(newUser.emailAddress)) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_email_already_exists)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!isPasswordValid(passwords["password"]!!)) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_short_password)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else if(!arePasswordsMatching(passwords)) {
            viewBinding.registerTvWarning.text = viewBinding.root.context.getString(R.string.warning_password_mismatch)
            viewBinding.registerTvWarning.visibility = View.VISIBLE
        } else {
            viewBinding.registerTvWarning.visibility = View.GONE
            return true
        }
        return false
    }

    private fun areAllFieldsFilled(newUser: UserModel, passwords: HashMap<String, String>) : Boolean {
        return !(newUser.firstName == "" ||
                newUser.lastName == "" ||
                newUser.emailAddress == "" ||
                passwords["password"] == "" ||
                passwords["confirm_password"] == "")
    }

    private fun isEmailAddValid(emailAdd: String) : Boolean {
        val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        return emailRegex.matches(emailAdd)
    }

    private suspend fun isEmailUnique(emailAdd: String) : Boolean {
        firestoreHandler = FirestoreHandler.getInstance(this)!!
        val isUnique = !(firestoreHandler?.doesUserExist(emailAdd))!!
        Log.d("RegisterActivity", "Returned isEmailUnique() = $isUnique")
        return isUnique
    }

    private fun isPasswordValid(password: String) : Boolean {
        val strongPasswordRegex = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")
        val passwordRegex = Regex("^.{8,}$")
        return passwordRegex.matches(password)
    }

    private fun arePasswordsMatching(passwords: HashMap<String, String>) : Boolean {
        return (passwords["password"] == passwords["confirm_password"])
    }

    private fun getNonce(): String {
        val nonceBytes = UUID.randomUUID().toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(nonceBytes)
        val hash = digest.fold("") {str, it -> str + "%02x".format(it)}
        return hash
    }
}