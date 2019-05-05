package bme.gy4ez8.tartozaskezelo.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatDialogFragment
import android.view.View
import android.widget.Button
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.sendNotificationToUser
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.username
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef


class AddFriendDialog : AppCompatDialogFragment() {

    internal lateinit var email: TextInputLayout

    internal lateinit var alone: ImageView

    internal var userExists: Boolean? = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_addfriend, null)

        email = view.findViewById(R.id.addfriend_email)

        alone = view.findViewById(R.id.gif_alone)

        builder.setView(view).setTitle("Ismerős felvétele").setNegativeButton("Mégse") { dialog, which -> }.setPositiveButton("Hozzáadás") { dialog, which -> }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog

            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener(View.OnClickListener {
                if (email.editText!!.text.toString() == "") {
                    email.error = "A mező nem maradhat üresen!"
                    return@OnClickListener
                }

                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (friend in dataSnapshot.children) {
                            if (friend.child("email").getValue(String::class.java) == email.editText!!.text.toString() || friend.child("username").getValue(String::class.java) == email.editText!!.text.toString()) {
                                if (friend.child("friends").child(user!!.uid).exists()) {
                                    email.error = "Az ismerős már fel van véve!"
                                    alone.visibility = View.GONE
                                    return
                                }

                                if (friend.child("uid").getValue(String::class.java) == user!!.uid) {
                                    email.error = "R U That Forever Alone?"
                                    Glide.with(activity!!)
                                            .load(R.drawable.gifforever)
                                            .apply(RequestOptions()
                                                    .fitCenter())
                                            .into(alone)
                                    alone.visibility = View.VISIBLE
                                    return
                                }

                                usersRef.child(user!!.uid).child("friends").child(friend.child("uid").getValue(String::class.java)!!).child("uid").setValue(friend.child("uid").getValue(String::class.java))
                                usersRef.child(user!!.uid).child("friends").child(friend.child("uid").getValue(String::class.java)!!).child("status").setValue("sent")

                                usersRef.child(friend.child("uid").getValue(String::class.java)!!).child("friends").child(user!!.uid).child("uid").setValue(user!!.uid)
                                usersRef.child(friend.child("uid").getValue(String::class.java)!!).child("friends").child(user!!.uid).child("status").setValue("received")

                                sendNotificationToUser(friend.child("uid").getValue(String::class.java)!!, "Új ismerős felkérés", username + " ismerősnek jelölt")

                                userExists = true
                                break
                            }
                        }

                        if ((!userExists!!))
                            email.error = "Nincs ilyen felhasználó!"
                        else
                            d.dismiss()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            })

    }

}
