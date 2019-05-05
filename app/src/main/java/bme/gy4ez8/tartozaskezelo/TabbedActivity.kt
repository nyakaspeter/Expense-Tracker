package bme.gy4ez8.tartozaskezelo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.*
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View

import bme.gy4ez8.tartozaskezelo.dialog.AddFriendDialog
import bme.gy4ez8.tartozaskezelo.firebase.Firebase
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.auth
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.loadData
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.user
import bme.gy4ez8.tartozaskezelo.firebase.Firebase.userRef
import bme.gy4ez8.tartozaskezelo.fragment.FriendsFragment
import bme.gy4ez8.tartozaskezelo.fragment.SummaryFragment
import bme.gy4ez8.tartozaskezelo.fragment.TransactionsFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_tabbed.*

class TabbedActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.AppTheme_NoActionBar);
        setTheme(R.style.Theme_AppCompat_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager = container
        mViewPager!!.offscreenPageLimit = 2
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))

        Firebase.init()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tabbed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_addfriend) {
            val dialog = AddFriendDialog()
            dialog.show(supportFragmentManager, "Ismerős felvétele")
        }

        if (id == R.id.menu_logout) {

            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(p0.child("uid").getValue(String::class.java))
                }

            })

            auth.signOut()

            val loginIntent = Intent(this@TabbedActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            var fragment: Fragment? = null
            when (position) {
                0 -> fragment = TransactionsFragment()
                1 -> fragment = FriendsFragment()
                2 -> fragment = SummaryFragment()
            }
            return fragment
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    public override fun onStart() {
        super.onStart()

        if (user == null) {
            val loginIntent = Intent(this@TabbedActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
        else {
            loadData()
        }
    }

}
