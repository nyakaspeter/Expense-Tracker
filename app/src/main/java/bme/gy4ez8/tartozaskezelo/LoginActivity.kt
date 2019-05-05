package bme.gy4ez8.tartozaskezelo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.auth

class LoginActivity : AppCompatActivity() {

    private var emailText: TextInputLayout? = null
    private var passwordText: TextInputLayout? = null
    private var loginButton: Button? = null
    private var registerButton: Button? = null

    private var loginProgress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AppCompat)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailText = findViewById(R.id.login_emailText)
        passwordText = findViewById(R.id.login_passwordText)
        loginButton = findViewById(R.id.login_buttonLogin)
        registerButton = findViewById(R.id.login_buttonRegister)

        loginProgress = ProgressDialog(this)

        loginButton!!.setOnClickListener {
            val email = emailText!!.editText!!.text.toString()
            val password = passwordText!!.editText!!.text.toString()

            login_user(email, password)
        }

        registerButton!!.setOnClickListener {
            val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(registerIntent)
        }
    }

    private fun login_user(email: String, password: String) {
        var isError: Boolean? = false
        if (email == "") {
            emailText!!.error = "A mező nem maradhat üresen!"
            isError = true
        } else
            emailText!!.error = null
        if (password == "") {
            passwordText!!.error = "A mező nem maradhat üresen!"
            isError = true
        } else
            passwordText!!.error = null
        if (isError!!) return

        loginProgress!!.setTitle("Bejelentkezés")
        loginProgress!!.setCanceledOnTouchOutside(false)
        loginProgress!!.show()

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        loginProgress!!.dismiss()
                        // Sign in success, update UI with the signed-in user's information
                        val mainIntent = Intent(this@LoginActivity, TabbedActivity::class.java)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        loginProgress!!.dismiss()
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginActivity, "Hiba történt!",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}
