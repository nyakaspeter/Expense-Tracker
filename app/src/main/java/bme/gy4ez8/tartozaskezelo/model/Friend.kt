package bme.gy4ez8.tartozaskezelo.model

import java.util.Comparator

class Friend(var uid: String, var name: String, var status: String) {

    var mydebt: Int = 0
    var friendsdebt: Int = 0
    var sum: Int = 0

    override fun toString(): String {
        return name
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
