package bme.gy4ez8.tartozaskezelo.firebase

import bme.gy4ez8.tartozaskezelo.model.Friend
import bme.gy4ez8.tartozaskezelo.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging



object Firebase {
    lateinit var auth : FirebaseAuth
    var user : FirebaseUser? = null
    lateinit var db : FirebaseDatabase

    lateinit var userRef : DatabaseReference
    lateinit var usersRef : DatabaseReference
    lateinit var notificationsRef : DatabaseReference
    lateinit var transactionsRef : DatabaseReference
    lateinit var friendsRef : DatabaseReference

    var username : String? = null

    var transactions = ArrayList<Transaction>()
    var friends = ArrayList<Friend>()

    fun init() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        db = FirebaseDatabase.getInstance()
        usersRef = db.getReference("users")
        transactionsRef = db.getReference("transactions")
        notificationsRef = db.getReference("notifications")
    }

    fun loadData() {
        userRef = usersRef.child(user!!.uid)
        friendsRef = userRef.child("friends")

        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                FirebaseMessaging.getInstance().subscribeToTopic(p0.child("uid").getValue(String::class.java))
                username = p0.child("username").getValue(String::class.java)
            }

        })
    }

    fun sendNotificationToUser(uid: String, title: String, body: String) {

        val notification = HashMap<String, String>()
        notification.put("uid", uid)
        notification.put("title", title)
        notification.put("body", body)

        notificationsRef.push().setValue(notification)
    }
}