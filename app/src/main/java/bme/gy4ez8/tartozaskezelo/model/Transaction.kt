package bme.gy4ez8.tartozaskezelo.model

import java.util.Comparator

class Transaction {
    var id: String = ""
    var item: String = ""
    var buyer: String = ""
    var date: String = ""
    var receiver: String = ""
    var price: Int = 0

    constructor() {}

    constructor(id: String, item: String, buyer: String, date: String, receiver: String, price: Int) {
        this.id = id
        this.item = item
        this.buyer = buyer
        this.date = date
        this.receiver = receiver
        this.price = price
    }

    class OrderByDateDescending : Comparator<Transaction> {
        override fun compare(o1: Transaction, o2: Transaction): Int {
            return o2.date.compareTo(o1.date)
        }
    }
}
