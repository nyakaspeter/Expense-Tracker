package bme.gy4ez8.tartozaskezelo.model

import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transactionsRef
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef
import bme.gy4ez8.tartozaskezelo.fragment.FriendsFragment
import bme.gy4ez8.tartozaskezelo.fragment.SummaryFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class Friend {

    public constructor()

    public constructor(_uid: String, _name: String, _status: String) {
        uid = _uid
        name = _name
        status = _status
    }

    var uid: String = ""
    var name: String = ""
    var status: String = ""

    var mydebt: Int = 0
    var friendsdebt: Int = 0
    var sum: Int = 0

    override fun toString(): String {
        return name
    }

    public fun loadName() {
        usersRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for(user in p0.children) {
                    if(user.child("uid").value == uid) {
                        name = user.child("username").value as String
                        val index = friends.indexOf(this@Friend)
                        FriendsFragment.adapter!!.notifyItemChanged(index)
                    }
                }
            }
        })
    }

    public fun loadSum() {
        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (transactionSnapshot in p0.children) {
                    if (transactionSnapshot.child("buyer").getValue(String::class.java) == uid && transactionSnapshot.child("receiver").getValue(String::class.java) == user!!.uid) {
                        mydebt += transactionSnapshot.child("price").getValue(Int::class.java)!!
                    }

                    if (transactionSnapshot.child("buyer").getValue(String::class.java) == user!!.uid && transactionSnapshot.child("receiver").getValue(String::class.java) == uid) {
                        friendsdebt += transactionSnapshot.child("price").getValue(Int::class.java)!!
                    }
                }

                if (status == "confirmed") {
                    sum = friendsdebt - mydebt
                } else {
                    sum = Integer.MAX_VALUE
                }

                val friendComparator = OrderBySumDescending()
                Collections.sort(friends, friendComparator)

                FriendsFragment.adapter!!.notifyDataSetChanged()
                SummaryFragment.refreshView()

            }

        })


    }

    class OrderBySumDescending : Comparator<Friend> {
        override fun compare(o1: Friend, o2: Friend): Int {
            return if (o1.sum < o2.sum)
                1
            else
                -1
        }
    }
}
