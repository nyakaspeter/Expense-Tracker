package bme.gy4ez8.tartozaskezelo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import bme.gy4ez8.tartozaskezelo.firebase.Firebase
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.auth
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private var usernameText: TextInputLayout? = null
    private var emailText: TextInputLayout? = null
    private var passwordText: TextInputLayout? = null
    private var registerButton: Button? = null

    private var registerProgress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameText = findViewById(R.id.register_usernameText)
        emailText = findViewById(R.id.register_emailText)
        passwordText = findViewById(R.id.register_passwordText)
        registerButton = findViewById(R.id.register_registerButton)

        registerProgress = ProgressDialog(this)

        registerButton!!.setOnClickListener {
            val username = usernameText!!.editText!!.text.toString()
            val email = emailText!!.editText!!.text.toString()
            val password = passwordText!!.editText!!.text.toString()

            register(username, email, password)
        }
    }

    private fun register(username: String, email: String, password: String) {
        var error = false
        if (username == "") {
            usernameText!!.error = "A mező nem maradhat üresen!"
            error = true
        } else
            usernameText!!.error = null
        if (email == "") {
            emailText!!.error = "A mező nem maradhat üresen!"
            error = true
        } else
            emailText!!.error = null
        if (password.length < 6) {
            passwordText!!.error = "A jelszó legalább 6 karakter hosszú legyen!"
            error = true
        } else
            passwordText!!.error = null

        if (error) return

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var error = false
                for (userSnapshot in dataSnapshot.children) {
                    if (userSnapshot.child("username").getValue(String::class.java) == username) {
                        usernameText!!.error = "A felhasználónév foglalt!"
                        error = true
                    } else
                        usernameText!!.error = null
                    if (userSnapshot.child("email").getValue(String::class.java) == email) {
                        emailText!!.error = "Az e-mail cím foglalt!"
                        error = true
                    } else
                        emailText!!.error = null
                }
                if (error) return
                register_user(username, email, password)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun register_user(username: String, email: String, password: String) {
        registerProgress!!.setTitle("Regisztráció")
        registerProgress!!.setCanceledOnTouchOutside(false)
        registerProgress!!.show()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Firebase.init()

                usersRef.child(user!!.uid).child("username").setValue(username)
                usersRef.child(user!!.uid).child("email").setValue(email)
                usersRef.child(user!!.uid).child("uid").setValue(user!!.uid)

                registerProgress!!.dismiss()
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this@RegisterActivity, "Sikeres regisztráció",
                        Toast.LENGTH_SHORT).show()
                val mainIntent = Intent(this@RegisterActivity, TabbedActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()
            } else {
                registerProgress!!.dismiss()
                // If sign in fails, display a message to the user.
                Toast.makeText(this@RegisterActivity, "Hiba történt!",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
}
