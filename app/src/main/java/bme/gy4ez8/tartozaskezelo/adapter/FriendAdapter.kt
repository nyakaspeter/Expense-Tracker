package bme.gy4ez8.tartozaskezelo.adapter

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef
import bme.gy4ez8.tartozaskezelo.model.Friend

class FriendAdapter(internal var friends: List<Friend>, internal var context: Context) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_friend, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val friend = friends[i]

        viewHolder.name.text = friend.name

        if (friend.status == "received") {
            viewHolder.status1.text = "ismerősnek jelölt"

            viewHolder.status2.visibility = View.GONE
            viewHolder.sum.visibility = View.GONE
        }

        if (friend.status == "sent") {
            viewHolder.status1.text = "nincs megerősítve"

            viewHolder.status2.visibility = View.GONE
            viewHolder.sum.visibility = View.GONE
            viewHolder.accept.visibility = View.GONE
        }

        if (friend.status == "confirmed") {
            viewHolder.status1.text = String.format("Adott: %d Ft", friend.mydebt)
            viewHolder.status2.text = String.format("Kapott: %d Ft", friend.friendsdebt)

            if (friend.sum < 0) {
                viewHolder.card.setBackgroundResource(R.color.colorDebt)
                viewHolder.sum.text = String.format("Tartozom: %d Ft", Math.abs(friend.sum))
            } else if (friend.sum > 0) {
                viewHolder.card.setBackgroundResource(R.color.colorPurchase)
                viewHolder.sum.text = String.format("Tartozik: %d Ft", friend.sum)
            } else {
                viewHolder.sum.text = "Nincs tartozás"
            }

            viewHolder.accept.visibility = View.GONE
            viewHolder.decline.visibility = View.GONE
        }

        viewHolder.accept.setOnClickListener {
            usersRef.child(friend.uid).child("friends").child(user!!.uid).child("status").setValue("confirmed")
            usersRef.child(user!!.uid).child("friends").child(friend.uid).child("status").setValue("confirmed")
        }
        viewHolder.decline.setOnClickListener {
            usersRef.child(friend.uid).child("friends").child(user!!.uid).removeValue()
            usersRef.child(user!!.uid).child("friends").child(friend.uid).removeValue()
        }

    }

    override fun getItemCount(): Int {
        return friends.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView
        internal var status1: TextView
        internal var status2: TextView
        internal var sum: TextView
        internal var accept: ImageButton
        internal var decline: ImageButton
        internal var card: CardView

        init {

            name = itemView.findViewById<View>(R.id.friend_name) as TextView
            accept = itemView.findViewById<View>(R.id.friend_accept) as ImageButton
            decline = itemView.findViewById<View>(R.id.friend_decline) as ImageButton
            status1 = itemView.findViewById<View>(R.id.friend_status1) as TextView
            status2 = itemView.findViewById<View>(R.id.friend_status2) as TextView
            sum = itemView.findViewById<View>(R.id.friend_sum) as TextView
            card = itemView.findViewById<View>(R.id.friend_card) as CardView

        }
    }
}
