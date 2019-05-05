package bme.gy4ez8.tartozaskezelo.adapter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.model.Transaction
import bme.gy4ez8.tartozaskezelo.dialog.EditTransactionDialog
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef

class TransactionAdapter(internal var transactions: List<Transaction>, internal var context: Context) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_transaction, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val transaction = transactions[i]

        if (transaction.buyer == user!!.uid) {
            viewHolder.card.setBackgroundResource(R.color.colorPurchase)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        if (userSnapshot.child("uid").getValue(String::class.java) == transaction.receiver) {
                            val receiver = userSnapshot.child("username").getValue(String::class.java)
                            viewHolder.person.text = String.format("Kapta: %s", receiver)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        } else {
            viewHolder.card.setBackgroundResource(R.color.colorDebt)
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        if (userSnapshot.child("uid").getValue(String::class.java) == transaction.buyer) {
                            val buyer = userSnapshot.child("username").getValue(String::class.java)
                            viewHolder.person.text = String.format("Adta: %s", buyer)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        viewHolder.item.text = transaction.item

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date = Date()
        try {
            date = dateFormat.parse(transaction.date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val myFormat = SimpleDateFormat("yyyy. MM. dd., EEEE")
        viewHolder.date.text = String.format("Dátum: %s", myFormat.format(date))

        viewHolder.price.text = String.format("%d Ft", transaction.price)

        viewHolder.card.setOnClickListener {
            val dialog = EditTransactionDialog(transaction)
            dialog.show((context as AppCompatActivity).supportFragmentManager, "Tétel")
        }

    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var card: CardView
        internal var item: TextView
        internal var date: TextView
        internal var person: TextView
        internal var price: TextView

        init {

            card = itemView.findViewById<View>(R.id.transaction_card) as CardView
            item = itemView.findViewById<View>(R.id.transaction_item) as TextView
            person = itemView.findViewById<View>(R.id.transaction_person) as TextView
            date = itemView.findViewById<View>(R.id.transaction_date) as TextView
            price = itemView.findViewById<View>(R.id.transaction_price) as TextView
        }
    }
}
