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
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user


class TransactionsFragment : Fragment() {

    companion object {
        var adapter: RecyclerView.Adapter<*>? = null
    }

    private var recyclerView: RecyclerView? = null

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

        return view
    }

    fun addTransaction() {
        val dialog = AddTransactionDialog()
        dialog.show(fragmentManager!!, "Tétel hozzáadása")
    }

}
