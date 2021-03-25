package com.example.jokeapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jokeapp.R
import com.example.jokeapp.items.Sentence
import com.example.jokeapp.items.Tweet
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where

class TweetAdapter(private var tweets: List<Tweet>, private val realm: Realm? = null, private val uuid: String): RecyclerView.Adapter<TweetAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.tweet_box,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.twitterName.text = tweets[position].twitterName
        holder.twitterHandle.text = tweets[position].twitterHandle
        holder.twitterText.text = tweets[position].tweetText
        holder.imageView.setBackgroundResource(tweets[position].profileDrawableId)

        var initStatus: Boolean //false is not liked, true is liked
        //find liked status
        val item: Sentence = realm?.where<Sentence>()?.contains("sentence",holder.twitterText.text as String)?.findFirst()!!
        initStatus = if(item.likedUsers.contains(uuid)){
            holder.heartButton.setImageResource(R.drawable.heartred)
            true
        } else{
            holder.heartButton.setImageResource(R.drawable.heart)
            false
        }

        /*when(mode){
            "comedian1"->{
                val item: com.example.jokeapp.items.Sentence = realm?.where<com.example.jokeapp.items.Sentence>()?.contains("sentence",holder.twitterText.text as String)?.findFirst()!!
                initStatus = if(item.liked == true){
                    holder.heartButton.setImageResource(R.drawable.heartred)
                    true
                } else{
                    holder.heartButton.setImageResource(R.drawable.heart)
                    false
                }
            }
            "comedian2"->{
                val item: com.example.jokeapp.items.Sentence = realm?.where<com.example.jokeapp.items.Sentence>()?.contains("sentence",holder.twitterText.text as String)?.findFirst()!!
                initStatus = if(item.liked == true){
                    holder.heartButton.setImageResource(R.drawable.heartred)
                    true
                } else{
                    holder.heartButton.setImageResource(R.drawable.heart)
                    false
                }
            }
        }*/

        //set on click: change heart color and update realm entry
        holder.heartButton.setOnClickListener{
            if(!initStatus){ //if false/not liked
                holder.heartButton.setImageResource(R.drawable.heartred)
                realm?.executeTransaction{ transactionRealm->
                    val item: Sentence = transactionRealm.where<Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                    item.likedUsers.add(uuid)
                }
                /*when(mode){
                    "comedian1"->{
                        realm?.executeTransaction{ transactionRealm->
                            val item: com.example.jokeapp.items.Sentence = transactionRealm.where<com.example.jokeapp.items.Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                            item.liked = true
                        }
                    }
                    "comedian2"->{
                        realm?.executeTransaction{ transactionRealm->
                            val item: com.example.jokeapp.items.Sentence = transactionRealm.where<com.example.jokeapp.items.Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                            item.liked = true
                        }
                    }
                }*/
                initStatus = true
            }
            else{
                holder.heartButton.setImageResource(R.drawable.heart)
                realm?.executeTransaction{ transactionRealm->
                    val item: Sentence = transactionRealm.where<Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                    item.likedUsers.remove(uuid)
                }
                /*when(mode){
                    "comedian1"->{
                        realm?.executeTransaction{ transactionRealm->
                            val item: com.example.jokeapp.items.Sentence = transactionRealm.where<com.example.jokeapp.items.Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                            item.liked = false
                        }
                    }
                    "comedian2"->{
                        realm?.executeTransaction{ transactionRealm->
                            val item: com.example.jokeapp.items.Sentence = transactionRealm.where<com.example.jokeapp.items.Sentence>().contains("sentence",holder.twitterText.text as String).findFirst()!!
                            item.liked = false
                        }
                    }
                }*/
                initStatus = false
            }
        }
    }

    override fun getItemCount(): Int {
        return tweets.size
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        internal val twitterName: TextView = itemView.findViewById(R.id.twitterName)
        internal val twitterHandle: TextView = itemView.findViewById(R.id.twitterHandle)
        internal val twitterText: TextView = itemView.findViewById(R.id.textTweet)
        internal val heartButton: ImageButton = itemView.findViewById(R.id.heartButton)
        internal val imageView: ImageView = itemView.findViewById(R.id.profileImage)
    }
}