package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class Game {
    var polishTitle : String? = null
    var title : String = ""
    var isExtension : Boolean = false
    var publishDate : String = ""
    var thumbnail : ByteArray = ByteArray(0)

    constructor(title: String, isExtension: Boolean, publishDate: String,
                thumbnail: ByteArray){
        this.title = title
        this.isExtension = isExtension
        this.publishDate = publishDate
        this.thumbnail = thumbnail
    }
}