package bme.gy4ez8.tartozaskezelo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.firebase.Firebase
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.db
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.username
import bme.gy4ez8.tartozaskezelo.model.Friend
import java.util.*


class SummaryFragment : Fragment() {

    lateinit var textTotal: TextView
    lateinit var textMydebt: TextView
    lateinit var textFrienddebt: TextView
    lateinit var textUsername: TextView
    lateinit var gif: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_summary, container, false)

        textTotal = view.findViewById(R.id.summary_total)
        textMydebt = view.findViewById(R.id.summary_mydebt)
        textFrienddebt = view.findViewById(R.id.summary_frienddebt)
        textUsername = view.findViewById(R.id.summary_username)
        gif = view.findViewById(R.id.gif)

        refreshView()
        Firebase.dataLoaded.plusAssign { refreshView() }

        return view
    }

    fun refreshView() {
        textUsername.text = "Üdv, $username!"

        var mydebt = 0
        var friendsdebt = 0

        for (f in friends) {
            if (f.status != "confirmed") {}
            else if(f.sum > 0) friendsdebt += f.sum
            else mydebt += -f.sum
        }

        val total = friendsdebt - mydebt

        textMydebt.text = String.format("Saját tartozásaim: %d Ft", mydebt)
        textFrienddebt.text = String.format("Tartozások felém: %d Ft", friendsdebt)

        if (isAdded) {
            if (total > 0) {
                textTotal.text = String.format("Várható bevétel: %d Ft", total)
                Glide.with(activity!!)
                        .load(R.drawable.gifmoney)
                        .apply(RequestOptions()
                                .fitCenter())
                        .into(gif)
            } else if (total == 0) {
                textTotal.text = String.format("Várható bevétel: %d Ft", total)
                Glide.with(activity!!)
                        .load(R.drawable.gifzero)
                        .apply(RequestOptions()
                                .fitCenter())
                        .into(gif)
            } else {
                textTotal.text = String.format("Várható kiadás: %d Ft", Math.abs(total))
                Glide.with(activity!!)
                        .load(R.drawable.gifnomoney)
                        .apply(RequestOptions()
                                .fitCenter())
                        .into(gif)
            }
        }
    }

    fun refresh() {

        db.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                friends.clear()

                username = dataSnapshot.child("users").child(user!!.uid).child("username").getValue(String::class.java)



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

                var mydebt = 0
                var frienddebt = 0

                for (f in friends) {
                    mydebt += f.mydebt
                    frienddebt += f.friendsdebt
                }

                val total = frienddebt - mydebt

                textMydebt.text = String.format("Saját tartozásaim: %d Ft", mydebt)
                textFrienddebt.text = String.format("Tartozások felém: %d Ft", frienddebt)

                if (isAdded) {
                    if (total > 0) {
                        textTotal.text = String.format("Várható bevétel: %d Ft", total)
                        Glide.with(activity!!)
                                .load(R.drawable.gifmoney)
                                .apply(RequestOptions()
                                        .fitCenter())
                                .into(gif)
                    } else if (total == 0) {
                        textTotal.text = String.format("Egyenleg: %d Ft", total)
                        Glide.with(activity!!)
                                .load(R.drawable.gifzero)
                                .apply(RequestOptions()
                                        .fitCenter())
                                .into(gif)
                    } else {
                        textTotal.text = String.format("Várható kiadás: %d Ft", Math.abs(total))
                        Glide.with(activity!!)
                                .load(R.drawable.gifnomoney)
                                .apply(RequestOptions()
                                        .fitCenter())
                                .into(gif)
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

}
