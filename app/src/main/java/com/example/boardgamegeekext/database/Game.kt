package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class Game {
    var id : Int = 0
    var title : String = ""
    var isExtension : Boolean = false
    var publishDate : String = ""
    var thumbnail : ByteArray = ByteArray(0)

    constructor(id : Int, title: String, isExtension: Boolean, publishDate: String,
                thumbnail: ByteArray){
        this.id = id
        this.title = title
        this.isExtension = isExtension
        this.publishDate = publishDate
        this.thumbnail = thumbnail
    }
}