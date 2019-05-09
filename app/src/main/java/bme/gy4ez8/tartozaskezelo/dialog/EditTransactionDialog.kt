package bme.gy4ez8.tartozaskezelo.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatDialogFragment
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

import bme.gy4ez8.tartozaskezelo.R
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friends
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.friendsRef
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.transRef
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.usersRef
import bme.gy4ez8.tartozaskezelo.model.Transaction

class EditTransactionDialog(var tran: Transaction) : AppCompatDialogFragment() {

    internal lateinit var name: TextInputLayout
    internal lateinit var price: TextInputLayout
    internal lateinit var dateinput: TextInputLayout

    internal lateinit var ki: TextView
    internal lateinit var buyer: TextView

    internal lateinit var addspinner: Spinner
    internal lateinit var spinner: Spinner

    internal var myCalendar = Calendar.getInstance()

    private val friendnames = ArrayList<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_addtransaction, null)

        ki = view.findViewById(R.id.addtransaction_kinek)
        buyer = view.findViewById(R.id.addtransaction_buyer)

        addspinner = view.findViewById(R.id.addtransaction_spinner)
        addspinner.visibility = View.GONE

        name = view.findViewById(R.id.addtransaction_name)
        price = view.findViewById(R.id.addtransaction_price)

        for (f in friends) {
            if (f.status == "confirmed") friendnames.add(f.name)
        }

        spinner = view.findViewById(R.id.edittransaction_spinner)
        val adapter = ArrayAdapter(activity!!, R.layout.custom_simple_spinner_item, friendnames)
        spinner.adapter = adapter

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    if (tran.buyer == user!!.uid && userSnapshot.child("uid").getValue(String::class.java) == tran.receiver) {
                        ki.text = "Kapta"
                        buyer.visibility = View.GONE
                        spinner.visibility = View.VISIBLE
                        spinner.setSelection(adapter.getPosition(userSnapshot.child("username").getValue(String::class.java)))
                    }

                    if (tran.receiver == user!!.uid && userSnapshot.child("uid").getValue(String::class.java) == tran.buyer) {
                        ki.text = "Adta"
                        ki.setPadding(0, 0, 0, 0)
                        buyer.text = userSnapshot.child("username").getValue(String::class.java)
                        spinner.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        name.editText!!.setText(tran.item)
        price.editText!!.setText(tran.price.toString())

        val format = SimpleDateFormat("yyyy-MM-dd")

        var trandate = Calendar.getInstance().time
        try {
            trandate = format.parse(tran.date)
        } catch (e: Exception) {
        }

        val myFormat = "yyyy. MM. dd., EEEE" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        myCalendar.time = trandate
        dateinput = view.findViewById(R.id.addtransaction_date2)
        dateinput.editText!!.inputType = InputType.TYPE_NULL
        dateinput.editText!!.setText(sdf.format(trandate))

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)



            dateinput.editText!!.setText(sdf.format(myCalendar.time))
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

        builder.setView(view).setNegativeButton("Vissza") { dialog, which -> }

        if (tran.buyer == user!!.uid) {
            builder.setTitle("Adott kölcsön")

            builder.setPositiveButton("Mentés") { dialog, which -> }

            builder.setNeutralButton("Tétel törlése") { dialog, which ->
                transRef.child(tran.id).removeValue()
                usersRef.child(tran.receiver).child("transactions").child(tran.id).removeValue()
            }
        } else {
            builder.setTitle("Kapott kölcsön")
            buyer.setTextColor(resources.getColor(R.color.colorWhite))
            disableEditText(name.editText!!)
            disableEditText(price.editText!!)
            disableEditText(dateinput.editText!!)
        }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.setOnClickListener(View.OnClickListener {
                var isError: Boolean? = false
                //Do stuff, possibly set wantToCloseDialog to true then...
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

                if (isError!!) return@OnClickListener

                val receiverUid: String

                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (userSnapshot in dataSnapshot.children) {
                            if (userSnapshot.child("username").getValue(String::class.java) == spinner.selectedItem) {
                                val receiverUid = userSnapshot.child("uid").getValue(String::class.java)

                                val transaction = Transaction(
                                        tran.id,
                                        name.editText!!.text.toString(),
                                        user!!.uid,
                                        SimpleDateFormat("yyyy-MM-dd").format(myCalendar.time),
                                        receiverUid!!,
                                        Integer.parseInt(price.editText!!.text.toString())
                                )

                                transRef.child(tran.id).setValue(transaction)
                                usersRef.child(tran.receiver).child("transactions").child(tran.id).removeValue()
                                usersRef.child(receiverUid).child("transactions").child(tran.id).setValue(transaction)

                                d.dismiss()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            })
        }
    }

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
        editText.keyListener = null
        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.isClickable = false
        editText.setTextColor(resources.getColor(R.color.colorWhite))
    }
}
