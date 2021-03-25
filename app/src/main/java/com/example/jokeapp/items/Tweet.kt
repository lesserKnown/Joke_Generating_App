package com.example.jokeapp.items

class Tweet (var twitterName: String ="", var twitterHandle: String="", val tweetText: String, var profileDrawableId: Int){

    fun setName(name:String){
        this.twitterName = name
    }
    fun setHandle(handle:String){
        this.twitterHandle = handle
    }
    fun setDrawable(drawableInt: Int){
        this.profileDrawableId = drawableInt
    }
}