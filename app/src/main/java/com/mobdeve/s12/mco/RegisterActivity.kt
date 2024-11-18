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
import com.mobdeve.s12.mco.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                                        viewBinding.registerEtEmail.text.toString(),
                                        UserModel.SignUpMethod.EMAIL)
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

                    val toast = Toast.makeText(this@RegisterActivity, "Account successfully created.", Toast.LENGTH_SHORT)
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
                } catch (e: GetCredentialCancellationException) {
                    Log.w("RegisterActivity", "$e, doing nothing")
                    // do nothing since it's not really a bad thing to cancel
                } catch (e: GetCredentialException) {
                    Log.e("RegisterActivity", e.toString())
                    showGoogleSignupWarning()
                }
            }
        }
    }

    private fun handleGoogleSignup(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                googleSignIn(googleCredential)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("FirebaseAuth", "Received an invalid google id token response", e)
                showGoogleSignupWarning()
            }
        } else {
            Log.e("RegisterActivity", "Unexpected type of credential")
            showGoogleSignupWarning()
        }
    }

    private fun googleSignIn(googleCredential: AuthCredential) {
        authHandler = AuthHandler.getInstance(this@RegisterActivity)!!
        authHandler.googleSignIn(googleCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseAuth", "Firebase auth sign in success")
                viewBinding.registerTvWarning.visibility = View.GONE

                val user = task.result?.user
                val email = user?.email ?: return@addOnCompleteListener

                CoroutineScope(Dispatchers.Main).launch {
                    if (isEmailUnique(email)) {
                        createGoogleAccount(user, email)
                    } else {
                        signIntoExistingAccountWithGoogle(user, email, googleCredential)
                    }
                }
            } else {
                Log.e("FirebaseAuth", "Firebase auth sign in fail", task.exception)
                showGoogleSignupWarning()
            }
        }
    }

    private fun createGoogleAccount(user: FirebaseUser, email: String) {
        val names = user.displayName?.let { splitName(it) }
        val firstName = names?.first
        val lastName = names?.second

        if (firstName != null && lastName != null) {
            val newUser = UserModel(user.uid, firstName, lastName, email, UserModel.SignUpMethod.GOOGLE)
            firestoreHandler = FirestoreHandler.getInstance(this@RegisterActivity)!!
            firestoreHandler.createUser(newUser)
            Log.d("FirebaseAuth", "Create new user in Firestore with Google success")
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun signIntoExistingAccountWithGoogle(user: FirebaseUser, email: String, googleCredential: AuthCredential) {
        firestoreHandler = FirestoreHandler.getInstance(this@RegisterActivity)!!
        val foundUser = firestoreHandler.getUserFromEmail(email)
        if (foundUser?.get("signUpMethod") == UserModel.SignUpMethod.EMAIL.name) {
            authHandler.googleLinkAccount(user, googleCredential).addOnCompleteListener { linkTask ->
                if (linkTask.isSuccessful) {
                    Log.d("FirebaseAuth", "Google sign in to registered account with email success")
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.e("FirebaseAuth", "Google sign in to registered account with email fail")
                    showGoogleSignupWarning()
                }
            }
        } else {
            Log.d("FirebaseAuth", "Google sign in to registered account with Google success")
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showGoogleSignupWarning() {
        viewBinding.registerTvWarning.text = getString(R.string.warning_google_register_fail)
        viewBinding.registerTvWarning.visibility = View.VISIBLE
    }

    private suspend fun areAllFieldsValid(newUser: UserModel, passwords: HashMap<String, String>) : Boolean {
        if(!areAllFieldsFilled(newUser, passwords)) {
            setWarningMessage(R.string.warning_incomplete_fields, View.VISIBLE)
        } else if(!isEmailAddValid(newUser.emailAddress)) {
            setWarningMessage(R.string.warning_invalid_email, View.VISIBLE)
        } else if(!isEmailUnique(newUser.emailAddress)) {
            setWarningMessage(R.string.warning_email_already_exists, View.VISIBLE)
        } else if(!isPasswordValid(passwords["password"]!!)) {
            setWarningMessage(R.string.warning_short_password, View.VISIBLE)
        } else if(!arePasswordsMatching(passwords)) {
            setWarningMessage(R.string.warning_password_mismatch, View.VISIBLE)
        } else {
            setWarningMessage(R.string.warning_invalid_credentials, View.GONE)
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

    private fun setWarningMessage(message: Int, visibility: Int) {
        viewBinding.registerTvWarning.text = viewBinding.root.context.getString(message)
        viewBinding.registerTvWarning.visibility = visibility
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