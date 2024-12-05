package rguzman.parcial04

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var rememberMeCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)
        val googleSignInButton = findViewById<Button>(R.id.google_sign_in_button)
        val showPasswordButton = findViewById<Button>(R.id.show_password_button)
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox)

        loadCredentials()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(email, password)
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        showPasswordButton.setOnClickListener {
            if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                showPasswordButton.text = "Mostrar"
            } else {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                showPasswordButton.text = "Ocultar"
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese todos los datos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (rememberMeCheckbox.isChecked) {
                        saveCredentials(email, password)
                    } else {
                        clearCredentials()
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Datos incorrectos. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese todos los datos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "El correo ya está registrado.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Datos inválidos. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        if (authTask.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCredentials(email: String, password: String) {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    private fun loadCredentials() {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""
        val password = sharedPreferences.getString("password", "") ?: ""
        emailEditText.setText(email)
        passwordEditText.setText(password)
        rememberMeCheckbox.isChecked = email.isNotEmpty() && password.isNotEmpty()
    }

    private fun clearCredentials() {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("email")
            remove("password")
            apply()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}