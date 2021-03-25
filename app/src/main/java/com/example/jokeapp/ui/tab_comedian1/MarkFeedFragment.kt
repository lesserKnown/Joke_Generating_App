package com.example.jokeapp.ui.tab_comedian1

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jokeapp.R
import com.example.jokeapp.adapters.TweetAdapter
import com.example.jokeapp.items.Sentence
import com.example.jokeapp.items.Tweet
import io.realm.Realm
import io.realm.RealmResults
import kotlin.random.Random

class MarkFeedFragment(var textArray: RealmResults<*>? = null, private val mode: String = "", val realm: Realm? = null, private val uuid: String = "") : Fragment() {

    private lateinit var viewModel: MarkFeedViewModel
    private var tweets: MutableList<Tweet> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_marknormfeed, container, false)

        populateTweets()

        //init recycler view
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerview_tweets)
        val adapter = TweetAdapter(tweets, realm, uuid)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val refreshLayout: SwipeRefreshLayout = root.findViewById(R.id.tweets_refresh_layout)
        refreshLayout.setOnRefreshListener {
            tweets.clear()
            adapter.notifyDataSetChanged()
            populateTweets()
            refreshLayout.isRefreshing = false
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MarkFeedViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun populateTweets() { //create tweets
        if (textArray != null){
            if(textArray!!.size < 10){
                for (i in 0 until textArray!!.size){
                    val item = textArray!![i]
                    if(item != null){
                        when(item){
                            is Sentence->{
                                val sentence = item.sentence.toString()
                                populateTweetProcess(sentence)
                            }
                        }
                    }
                }
            }
            else{
                //avoid duplicate tweet
                val intArray = IntArray(10){0}
                var counter = 0
                //show 10 tweets
                while (tweets.size < 10){
                    val ranNumber: Int = Random.nextInt(0, textArray!!.size - 1)
                    if(!intArray.contains(ranNumber)){
                        intArray.set(counter, ranNumber)
                        val item = textArray!![ranNumber]
                        if(item != null){
                            when(item){
                                is Sentence->{
                                    val sentence = item.sentence.toString()
                                    populateTweetProcess(sentence)
                                    counter += 1
                                }
                            }
                        }
                    }
                }
            }
/*            for (item in intArray){
                val number = item.toString()
                Log.e("Check",number)
            }*/
        }
    }

    private fun populateTweetProcess(text:String?){
        if(text is String){ //avoid String? error
            var sentence = text
            //remove newline
            if (sentence.endsWith("\n")){
                sentence = sentence.removeSuffix("\n")
            }
            //format tweet
            val tweet = Tweet(
                    tweetText = sentence,//getString(R.string.sampleText),
                    profileDrawableId = R.drawable.ic_menu_camera
            )
            when (mode){
                "comedian1" -> { //mark normand
                    tweet.setName(getString(R.string.comedian1Name))
                    tweet.setHandle(getString(R.string.menu_comedian1))
                    tweet.setDrawable(R.drawable.comedian1_profile400x400)
                }
                "comedian2" -> { //ronny chieng
                    tweet.setName(getString(R.string.comedian2Name))
                    tweet.setHandle(getString(R.string.menu_comedian2))
                    tweet.setDrawable(R.drawable.comedian2_profile400x400)
                }
                "comedian3"-> {
                    tweet.setName(getString(R.string.comedian3Name))
                    tweet.setHandle(getString(R.string.menu_comedian3))
                    tweet.setDrawable((R.drawable.comedian3_profile400x400))
                }
            }
            tweets.add(tweet)
        }
    }
}