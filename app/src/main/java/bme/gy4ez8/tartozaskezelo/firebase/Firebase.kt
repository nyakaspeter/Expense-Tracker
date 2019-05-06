package bme.gy4ez8.tartozaskezelo.firebase

import bme.gy4ez8.tartozaskezelo.adapter.FriendAdapter
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.notificationsRef
import bme.gy4ez8.tartozaskezelo.fragment.FriendsFragment
import bme.gy4ez8.tartozaskezelo.fragment.TransactionsFragment
import bme.gy4ez8.tartozaskezelo.fragment.TransactionsFragment.Companion.adapter
import bme.gy4ez8.tartozaskezelo.model.Friend
import bme.gy4ez8.tartozaskezelo.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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

        transactionsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                loadFriends()
                loadTransactions()
            }
        })

        friendsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                loadFriends()
                loadTransactions()
            }
        })

    }

    fun loadTransactions() {
        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val prevcount = transactions.count()
                transactions.clear()

                for (transactionSnapshot in dataSnapshot.children) {
                    if (transactionSnapshot.child("buyer").getValue(String::class.java) == user!!.uid || transactionSnapshot.child("receiver").getValue(String::class.java) == user!!.uid) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        transactions.add(transaction!!)
                    }
                }
                val transactionComparator = Transaction.OrderByDateDescending()
                Collections.sort(transactions, transactionComparator)

                TransactionsFragment.adapter!!.notifyItemRangeRemoved(0, prevcount)
                TransactionsFragment.adapter!!.notifyItemRangeInserted(0, transactions.count())
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    fun loadFriends() {
        friendsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                friends.clear()

                for(friend in p0.children) friends.add(friend.getValue(Friend::class.java)!!)

                for(friend in friends) {
                    friend.loadName()
                    friend.loadSum()
                }

                val friendComparator = Friend.OrderBySumDescending()
                Collections.sort(friends, friendComparator)

                //FriendsFragment.adapter!!.notifyDataSetChanged()
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