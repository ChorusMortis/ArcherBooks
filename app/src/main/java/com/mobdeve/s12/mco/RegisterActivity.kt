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

                if (!areAllFieldsValid(newUser, passwords)) {
                    return@launch
                }

                viewBinding.registerLoadingCover.visibility = View.VISIBLE
                viewBinding.registerProgressBar.visibility = View.VISIBLE

                authHandler = AuthHandler.getInstance(this@RegisterActivity)
                val userId = authHandler.createAccount(newUser.emailAddress, passwords["password"]!!, this@RegisterActivity)

                firestoreHandler = FirestoreHandler.getInstance(this@RegisterActivity)
                newUser.userId = userId
                firestoreHandler.createUser(newUser)

                showRegistrationSuccessToast()
                // if user already has a verified email, no email is sent
                authHandler.sendVerificationEmail()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Please check your inbox to verify your email.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error sending verification email! Please contact an admin.", Toast.LENGTH_LONG).show()
                    }
                }

                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

                viewBinding.registerLoadingCover.visibility = View.GONE
                viewBinding.registerProgressBar.visibility = View.GONE
            }
        }
    }

    private fun addListenerSignUpWithGoogleBtn() {
        viewBinding.registerBtnSignupgoogle.setOnClickListener {
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
                viewBinding.registerLoadingCover.visibility = View.VISIBLE
                viewBinding.registerProgressBar.visibility = View.VISIBLE

                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@RegisterActivity,
                    )
                    // Google credential obtained, handle it
                    Log.d("RegisterActivity", "Getting credentials success")
                    handleGoogleSignup(result)
                    // hide loading cover and progress bar in other function because it uses another thread later
                } catch (e: GetCredentialCancellationException) {
                    Log.w("RegisterActivity", "$e, doing nothing")
                    // do nothing since it's not really a bad thing to cancel
                    viewBinding.registerLoadingCover.visibility = View.GONE
                    viewBinding.registerProgressBar.visibility = View.GONE
                } catch (e: GetCredentialException) {
                    Log.e("RegisterActivity", e.toString())
                    setWarningMessage(R.string.warning_google_register_fail, View.VISIBLE)
                    viewBinding.registerLoadingCover.visibility = View.GONE
                    viewBinding.registerProgressBar.visibility = View.GONE
                }
                // loading cover and progress bar isn't all done here because handleGoogleSignup uses another thread
                // if it's done here, hiding is too early once handleGoogleSignup finishes
            }
        }
    }

    private fun handleGoogleSignup(result: GetCredentialResponse) {
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
                setWarningMessage(R.string.warning_google_register_fail, View.VISIBLE)
            }
        } else {
            Log.e("RegisterActivity", "Unexpected type of credential")
            setWarningMessage(R.string.warning_google_register_fail, View.VISIBLE)
        }
    }

    private fun googleSignIn(googleCredential: AuthCredential) {
        authHandler = AuthHandler.getInstance(this@RegisterActivity)
        // signs into existing Google account, creating Google account if it doesn't exist yet
        // NOTE: if user registered using email/pw with the same email exists, account auth
        // provider changes into Google only (only Google can be used to sign into it)
        authHandler.authSignInWithCredential(googleCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // hide any error if sign in with Google is successful
                Log.d("FirebaseAuth", "Firebase auth sign in success")
                viewBinding.registerTvWarning.visibility = View.GONE

                // get authenticated user and its email, exit early if email does not exist
                val user = task.result?.user
                val email = user?.email ?: return@addOnCompleteListener

                // block email without dlsu.edu.ph domain
                if (!email.endsWith("dlsu.edu.ph")) {
                    viewBinding.registerLoadingCover.visibility = View.GONE
                    viewBinding.registerProgressBar.visibility = View.GONE
                    authHandler.logoutAccount()
                    Toast.makeText(this@RegisterActivity, "Sorry! Only DLSU (dlsu.edu.ph) emails are allowed.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                // create Google account in database (if it doesn't exist yet) and go to MainActivity
                createGoogleAccountInDb(user, email)
            } else {
                Log.e("FirebaseAuth", "Firebase auth sign in fail", task.exception)
                setWarningMessage(R.string.warning_google_register_fail, View.VISIBLE)
            }
        }
    }

    private fun createGoogleAccountInDb(user: FirebaseUser, email: String) {
        // split names (assume last word/name is last name, the rest is first name)
        val names = user.displayName?.let { splitName(it) }
        val firstName = names?.first
        val lastName = names?.second

        CoroutineScope(Dispatchers.Main).launch {
            firestoreHandler = FirestoreHandler.getInstance(this@RegisterActivity)
            // check if user entry in collection with the same email already exists
            val existingUser = firestoreHandler.getUserByEmail(email)
            // create user entry in Firebase db if user entry with same email does not exist
            if (firstName != null && lastName != null && existingUser == null) {
                val newUser = UserModel(user.uid, firstName, lastName, email, UserModel.SignUpMethod.GOOGLE)
                firestoreHandler.createUser(newUser)
                showRegistrationSuccessToast()
            }
            // hide loading cover and progress bar here for proper timing
            viewBinding.registerLoadingCover.visibility = View.GONE
            viewBinding.registerProgressBar.visibility = View.GONE
            // start activity regardless of whether user entry was written into db
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
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
        firestoreHandler = FirestoreHandler.getInstance(this)
        val isUnique = !(firestoreHandler.doesUserExist(emailAdd))
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

    private fun showRegistrationSuccessToast() {
        Toast.makeText(this@RegisterActivity, "Account successfully created.", Toast.LENGTH_SHORT).show()
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