package bme.gy4ez8.tartozaskezelo.firebase

import android.provider.ContactsContract
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
    lateinit var transRef : DatabaseReference
    lateinit var usersRef : DatabaseReference
    lateinit var notificationsRef : DatabaseReference
    lateinit var friendsRef : DatabaseReference

    var username : String? = null

    var transactions = ArrayList<Transaction>()
    var friends = ArrayList<Friend>()

    fun init() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        db = FirebaseDatabase.getInstance()
        usersRef = db.getReference("users")
        notificationsRef = db.getReference("notifications")
    }

    fun loadData() {
        userRef = usersRef.child(user!!.uid)
        friendsRef = userRef.child("friends")
        transRef = userRef.child("transactions")

        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                FirebaseMessaging.getInstance().subscribeToTopic(p0.child("uid").getValue(String::class.java))
                username = p0.child("username").getValue(String::class.java)
            }
        })

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                val transcount = transactions.count()
                transactions.clear()

                for (transactionSnapshot in p0.child("transactions").children) {
                    val transaction = transactionSnapshot.getValue(Transaction::class.java)
                    transactions.add(transaction!!)
                }
                val transactionComparator = Transaction.OrderByDateDescending()
                Collections.sort(transactions, transactionComparator)

                TransactionsFragment.adapter!!.notifyItemRangeRemoved(0, transcount)
                TransactionsFragment.adapter!!.notifyItemRangeInserted(0, transactions.count())

                val friendscount = friends.count()
                friends.clear()

                for(friendSnapshot in p0.child("friends").children) {
                    val friend = friendSnapshot.getValue(Friend::class.java)
                    friends.add(friend!!)
                }

                for(f in friends) {
                    f.loadName()
                    if(f.status == "sent" || f.status == "received") {
                        f.sum = Int.MAX_VALUE
                        break
                    }
                    for(t in transactions) {
                        if(t.buyer == f.uid) f.mydebt += t.price
                        if(t.receiver == f.uid) f.friendsdebt += t.price
                    }
                    f.sum = f.friendsdebt - f.mydebt
                }

                val friendsComparator = Friend.OrderBySumDescending()
                Collections.sort(friends, friendsComparator)

                FriendsFragment.adapter!!.notifyItemRangeRemoved(0, friendscount)
                FriendsFragment.adapter!!.notifyItemRangeInserted(0, friends.count())
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