package com.example.jokeapp.items

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId

open class Sentence(
        @PrimaryKey
        var _id: ObjectId? = null,
        @Required
        var likedUsers: RealmList<String> = RealmList(),
        var sentence: String? = null,
        var type: String? = null
): RealmObject(){

}