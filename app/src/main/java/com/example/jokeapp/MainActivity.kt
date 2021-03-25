package com.example.jokeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.jokeapp.items.Sentence
import com.example.jokeapp.ui.tab_comedian1.MarkFeedFragment
import com.example.jokeapp.ui.tab_comedian1.MarkFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncSession
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var app: App
    private lateinit var realm: Realm
    private var comedianMode: String = "comedian1"
    private lateinit var stringUUID: String
    /*private var storyItems: MutableList<StoryItem> = ArrayList()*/

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //shared pref init
        val pref = this.getSharedPreferences("UUID", MODE_PRIVATE)
        val editor = pref.edit()
        if(pref.getString("UUID",null)==null){ //no uuid key
            stringUUID = UUID.randomUUID().toString() //generate uuid key
            editor.putString("UUID",stringUUID)
            editor.commit()
        }
        else{
            stringUUID = pref.getString("UUID", null)!!
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) //show up button in toolbar

        //setup Drawer navigation
        val navView: NavigationView = findViewById(R.id.nav_view)
        setDrawerContent(navView)

        //drawer hamburger setup, with animation
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val drawerToggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        drawerLayout.addDrawerListener(drawerToggle)

        /*val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_comedian1display), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)*/

        /*//init recycler view
        val recyclerView: RecyclerView = findViewById(R.id.recyclerview_story_item)
        storyItems.add(StoryItem("comedian1"))
        storyItems.add(StoryItem("comedian2"))
        val adapter = StoryItemAdapter(storyItems)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter*/

        //init realm
        Realm.init(this)

        val appID = "capooapplication-calzh"
        app = App(AppConfiguration.Builder(appID).build())

        //login realm
        val credential: Credentials = Credentials.anonymous()
        app.loginAsync(credential){
            if(it.isSuccess){
                val user: User? = app.currentUser()
                //sync with realm
                val partitionValue = getString(R.string.menu_comedian1).replace("@","")

                try {
                    val config = SyncConfiguration.Builder(user, partitionValue) //might crash during realm to mongoDB sync
                            .allowWritesOnUiThread(true)
                            .build()
                    realm = Realm.getInstance(config)
                    val senObject = realm.where<Sentence>().findAll()
                    //Log.e(partitionValue,senObject.toString())

                    //fragment transaction
                    supportFragmentManager!!.beginTransaction().add(R.id.nav_host_fragment, MarkFeedFragment(senObject, comedianMode, realm, stringUUID), "MarkFeedFragment").commit()

                    updateFabListener(senObject, comedianMode)
                    setImageButtonListener(user)
                }
                catch(e:Exception){
                    Log.e("ERROR","Failed to connect to Realm. Error: ${e.message}")
                }
            }
            else{
                Log.e("ERROR","Failed to login to Realm.")
                updateFabListener()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_settings->{ //go to about fragment/activity
                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                startActivity(intent)
                /*supportFragmentManager!!.
                beginTransaction().replace(R.id.nav_host_fragment, AboutFragment(), "AboutFragment").commit()
                comedianMode = "null"*/
                true
            }
            else->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
        app.currentUser()?.logOutAsync(){
            if(it.isSuccess){
                Log.v("EXIT", "Successfully logged out.")
            }
            else{
                Log.e("EXIT", "Failed to log out. Error: ${it.error}")
            }
        }
    }

    private fun updateFabListener(array: RealmResults<*>? = null, mode:String = ""){
        val fab: FloatingActionButton = findViewById(R.id.fab)
        if(array != null && mode != ""){
            fab.setOnClickListener {
                if(supportFragmentManager.findFragmentByTag("MarkFeedFragment") != null||
                    supportFragmentManager.findFragmentByTag("AboutFragment") != null) {
                    supportFragmentManager!!.
                    beginTransaction().replace(R.id.nav_host_fragment, MarkFragment(mode), "MarkFragment").commit()
                    supportActionBar?.hide()
                }
                else if(supportFragmentManager.findFragmentByTag("MarkFragment") != null){
                    supportFragmentManager!!.
                    beginTransaction().replace(R.id.nav_host_fragment, MarkFeedFragment(array,mode,realm,stringUUID), "MarkFeedFragment").commit()
                    supportActionBar?.show()
                }
            }
        }
        else{
            fab.setOnClickListener {
                if(supportFragmentManager.findFragmentByTag("MarkFeedFragment") != null||
                    supportFragmentManager.findFragmentByTag("AboutFragment") != null) {
                    supportFragmentManager!!.
                    beginTransaction().replace(R.id.nav_host_fragment, MarkFragment(), "MarkFragment").commit()
                    supportActionBar?.hide()
                }
                else if(supportFragmentManager.findFragmentByTag("MarkFragment") != null){
                    supportFragmentManager!!.
                    beginTransaction().replace(R.id.nav_host_fragment, MarkFeedFragment(), "MarkFeedFragment").commit()
                    supportActionBar?.show()
                }
            }
        }
    }

    private fun setImageButtonListener(user: User?){ //set story bar image buttons
        val buttonArray: MutableList<ImageButton> = ArrayList()
        buttonArray.add(findViewById(R.id.profileButton1))
        buttonArray.add(findViewById(R.id.profileButton2))
        buttonArray.add(findViewById(R.id.profileButton3))

        for(button in buttonArray){
            button.setOnClickListener{
                var partitionValue = ""
                when(button.id){
                    R.id.profileButton1->{
                        if(comedianMode!="comedian1"){
                            comedianMode="comedian1"
                            partitionValue = getString(R.string.menu_comedian1).replace("@","")
                        }
                    }
                    R.id.profileButton2->{
                        if(comedianMode!="comedian2"){
                            comedianMode="comedian2"
                            partitionValue = getString(R.string.menu_comedian2).replace("@","")
                        }
                    }
                    R.id.profileButton3->{
                        if(comedianMode!="comedian3"){
                            comedianMode="comedian3"
                            partitionValue = getString(R.string.menu_comedian3).replace("@","")
                        }
                    }
                    else->{
                        partitionValue=""
                    }
                }

                if(partitionValue!=""){
                    try{
                        val config = SyncConfiguration.Builder(user, partitionValue)
                            .allowWritesOnUiThread(true)
                            .build()
                        realm.close()
                        realm = Realm.getInstance(config)
                        val array = realm.where<Sentence>().findAll()
                        //Log.e(partitionValue,array.toString())

                        updateFabListener(array,comedianMode)

                        if(supportFragmentManager.findFragmentByTag("MarkFeedFragment") != null||
                            supportFragmentManager.findFragmentByTag("AboutFragment") != null) {
                            supportFragmentManager!!.
                            beginTransaction().replace(R.id.nav_host_fragment, MarkFeedFragment(array, comedianMode, realm,stringUUID), "MarkFeedFragment").commit()
                        }
                        else if (supportFragmentManager.findFragmentByTag("MarkFragment") != null){
                            supportFragmentManager!!.
                            beginTransaction().replace(R.id.nav_host_fragment, MarkFragment(comedianMode), "MarkFragment").commit()
                        }
                    }
                    catch(e:Exception){
                        Log.e("ERROR","Failed to connect to Realm. Error: ${e.message}")
                    }
                }
            }
        }
    }
    private fun setDrawerContent(navView: NavigationView){
        navView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem){ //set drawer menu items
        var partitionValue = ""
        when(menuItem.itemId){
            R.id.menuItem1->{
                if(comedianMode != "comedian1"){
                    comedianMode = "comedian1"
                    partitionValue = getString(R.string.menu_comedian1).replace("@","")
                }
            }
            R.id.menuItem2->{
                if(comedianMode != "comedian2"){
                    comedianMode = "comedian2"
                    partitionValue = getString(R.string.menu_comedian2).replace("@","")
                }
            }
            R.id.menuItem3->{
                if(comedianMode != "comedian3"){
                    comedianMode = "comedian3"
                    partitionValue = getString(R.string.menu_comedian3).replace("@","")
                }
            }
            else->{
                partitionValue=""
            }
        }

        if(partitionValue != ""){
            try {
                val user: User? = app.currentUser()
                val config = SyncConfiguration.Builder(user, partitionValue)
                    .allowWritesOnUiThread(true)
                    .build()
                realm.close()
                realm = Realm.getInstance(config)
                val array = realm.where<Sentence>().findAll()
                //Log.e(partitionValue,array.toString())

                updateFabListener(array, comedianMode)

                if (supportFragmentManager.findFragmentByTag("MarkFeedFragment") != null||
                    supportFragmentManager.findFragmentByTag("AboutFragment") != null) {
                    supportFragmentManager!!.beginTransaction().replace(R.id.nav_host_fragment, MarkFeedFragment(array, comedianMode, realm,stringUUID), "MarkFeedFragment").commit()
                } else if (supportFragmentManager.findFragmentByTag("MarkFragment") != null) {
                    supportFragmentManager!!.beginTransaction().replace(R.id.nav_host_fragment, MarkFragment(comedianMode), "MarkFragment").commit()
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Failed to connect to Realm. Error: ${e.message}")
            }
        }
        menuItem.isChecked = true
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawers()
    }
}