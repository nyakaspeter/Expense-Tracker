package bme.gy4ez8.tartozaskezelo.model

import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transRef
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
                    }
                }
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
