package bme.gy4ez8.tartozaskezelo.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatDialogFragment
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashSet
import java.util.Locale

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.adapter.CheckableSpinnerAdapter
import bme.gy4ez8.tartozaskezelo.firebase.Firebase
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transactionsRef
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef
import bme.gy4ez8.tartozaskezelo.model.Friend
import bme.gy4ez8.tartozaskezelo.model.Transaction


class AddTransactionDialog : AppCompatDialogFragment() {

    internal lateinit var name: TextInputLayout
    internal lateinit var price: TextInputLayout
    internal lateinit var dateinput: TextInputLayout

    internal lateinit var kinek: TextView
    internal lateinit var buyer: TextView

    internal lateinit var spinner: Spinner

    internal var myCalendar = Calendar.getInstance()

    internal var dateSet: Boolean? = false

    private val spinner_items = ArrayList<CheckableSpinnerAdapter.SpinnerItem<Friend>>()
    private val selected_items = HashSet<Friend>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_addtransaction, null)

        kinek = view.findViewById(R.id.addtransaction_kinek)
        kinek.visibility = View.GONE
        buyer = view.findViewById(R.id.addtransaction_buyer)
        buyer.visibility = View.GONE


        name = view.findViewById(R.id.addtransaction_name)
        price = view.findViewById(R.id.addtransaction_price)

        for (f in friends) {
            if (f.status == "confirmed") spinner_items.add(CheckableSpinnerAdapter.SpinnerItem(f, f.name))
        }

        spinner = view.findViewById(R.id.addtransaction_spinner)
        val adapter = CheckableSpinnerAdapter(activity, "Kinek", spinner_items, selected_items)
        spinner.adapter = adapter
        adapter.sp_parent = spinner
        adapter.tv_kinek = kinek

        dateinput = view.findViewById(R.id.addtransaction_date2)
        dateinput.editText!!.inputType = InputType.TYPE_NULL

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "yyyy. MM. dd., EEEE"
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

            dateinput.editText!!.setText(sdf.format(myCalendar.time))
            dateSet = true
        }

        dateinput.editText!!.setOnClickListener {
            DatePickerDialog(context!!, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        dateinput.editText!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                DatePickerDialog(context!!, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        builder.setView(view).setTitle("Új kölcsön").setNegativeButton("Mégse") { dialog, which -> }.setPositiveButton("Hozzáadás") { dialog, which -> }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener(View.OnClickListener {
                var isError: Boolean? = false
                if (selected_items.isEmpty()) {
                    Toast.makeText(activity, "Válassz legalább egy személyt!", Toast.LENGTH_LONG).show()
                    isError = true
                }
                if (name.editText!!.text.toString() == "") {
                    name.error = "A mező nem maradhat üresen!"
                    isError = true
                } else
                    name.error = null
                if (price.editText!!.text.toString() == "") {
                    price.error = "A mező nem maradhat üresen!"
                    isError = true
                } else
                    price.error = null
                if ((!dateSet!!)) {
                    dateinput.error = "A mező nem maradhat üresen!"
                    isError = true
                } else
                    dateinput.error = null

                if (isError!!) return@OnClickListener

                val receiverUid: String

                for (f in selected_items) {

                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (userSnapshot in dataSnapshot.children) {
                                if (userSnapshot.child("username").getValue(String::class.java) == f.name) {
                                    val receiverUid = userSnapshot.child("uid").getValue(String::class.java)

                                    val id = transactionsRef.push().key
                                    val transaction = Transaction(
                                            id!!,
                                            name.editText!!.text.toString(),
                                            user!!.uid,
                                            SimpleDateFormat("yyyy-MM-dd").format(myCalendar.time),
                                            receiverUid!!,
                                            Integer.parseInt(price.editText!!.text.toString())
                                    )

                                    transactionsRef.child(id!!).setValue(transaction)

                                    Firebase.sendNotificationToUser(receiverUid, "Új tartozás lett kiírva", Firebase.username + " új tartozást írt ki (" + Integer.parseInt(price.editText!!.text.toString()) + "Ft)")

                                    d.dismiss()

                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })

                }
            })
    }
}
