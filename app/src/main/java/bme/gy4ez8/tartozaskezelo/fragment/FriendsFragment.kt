package bme.gy4ez8.tartozaskezelo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.util.Collections

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.model.Friend
import bme.gy4ez8.tartozaskezelo.adapter.FriendAdapter
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.db
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.fragment.TransactionsFragment.Companion.adapter

class FriendsFragment : Fragment() {

    companion object {
        var adapter: RecyclerView.Adapter<*>? = null
    }

    private var recyclerView: RecyclerView? = null

    internal var friend_name: String? = null
    internal var friend_status: String? = null
    internal var friend_id: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        recyclerView = view.findViewById<View>(R.id.recycler_friends) as RecyclerView
        //recyclerView.setHasFixedSize(true);
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        adapter = FriendAdapter(friends, activity!!)
        recyclerView!!.adapter = adapter

        //getFriends()

        return view
    }

    fun getFriends() {

        db.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friends.clear()

                for (friendSnapshot in dataSnapshot.child("users").child(user!!.uid).child("friends").children) {

                    val status = friendSnapshot.child("status").getValue(String::class.java)

                    if (status != null) {
                        val uid = friendSnapshot.child("uid").getValue(String::class.java)
                        var username: String? = "user"

                        for (userSnapshot in dataSnapshot.child("users").children) {
                            if (userSnapshot.child("uid").exists() && userSnapshot.child("uid").getValue(String::class.java) == uid) {
                                username = userSnapshot.child("username").getValue(String::class.java)
                            }
                        }

                        val friend = Friend(uid!!, username!!, status)

                        for (transactionSnapshot in dataSnapshot.child("transactions").children) {
                            if (transactionSnapshot.child("buyer").getValue(String::class.java) == uid && transactionSnapshot.child("receiver").getValue(String::class.java) == user!!.uid) {
                                friend.mydebt = friend.mydebt + transactionSnapshot.child("price").getValue(Int::class.java)!!
                            }

                            if (transactionSnapshot.child("buyer").getValue(String::class.java) == user!!.uid && transactionSnapshot.child("receiver").getValue(String::class.java) == uid) {
                                friend.friendsdebt = friend.friendsdebt + transactionSnapshot.child("price").getValue(Int::class.java)!!
                            }
                        }

                        if (friend.status == "confirmed") {
                            friend.sum = friend.friendsdebt - friend.mydebt
                        } else {
                            friend.sum = Integer.MAX_VALUE
                        }

                        friends.add(friend)
                    }
                }
                val friendComparator = Friend.OrderBySumDescending()
                Collections.sort(friends, friendComparator)

                adapter = FriendAdapter(friends, activity!!)
                recyclerView!!.adapter = adapter
                recyclerView!!.scheduleLayoutAnimation()
                recyclerView!!.invalidate()

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

}
