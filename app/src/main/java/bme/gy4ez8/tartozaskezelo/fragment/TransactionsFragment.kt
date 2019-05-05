package bme.gy4ez8.tartozaskezelo.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
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
import bme.gy4ez8.tartozaskezelo.TabbedActivity
import bme.gy4ez8.tartozaskezelo.model.Transaction
import bme.gy4ez8.tartozaskezelo.adapter.TransactionAdapter
import bme.gy4ez8.tartozaskezelo.dialog.AddTransactionDialog
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transactions
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transactionsRef
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user


class TransactionsFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerView.Adapter<*>? = null

    private var fab: FloatingActionButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        fab = view.findViewById<View>(R.id.fab) as FloatingActionButton
        fab!!.setOnClickListener {
            addTransaction()
        }

        recyclerView = view.findViewById<View>(R.id.recycler_transactions) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy < 0) {
                    fab!!.show()

                } else if (dy > 0) {
                    fab!!.hide()
                }
            }
        })

        adapter = TransactionAdapter(transactions, activity!!)
        recyclerView!!.adapter = adapter

        getTransactions()

        return view
    }

    private fun getTransactions() {

        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                transactions.clear()

                for (transactionSnapshot in dataSnapshot.children) {
                    if (transactionSnapshot.child("buyer").getValue(String::class.java) == user!!.uid || transactionSnapshot.child("receiver").getValue(String::class.java) == user!!.uid) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        transactions.add(transaction!!)
                    }
                }
                val transactionComparator = Transaction.OrderByDateDescending()
                Collections.sort(transactions, transactionComparator)

                adapter = TransactionAdapter(transactions, activity!!)
                recyclerView!!.adapter = adapter

                recyclerView!!.scheduleLayoutAnimation()
                recyclerView!!.invalidate()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    fun addTransaction() {
        val dialog = AddTransactionDialog()
        dialog.show(fragmentManager!!, "Tétel hozzáadása")
    }

}
