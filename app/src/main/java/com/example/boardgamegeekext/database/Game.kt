package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class Game {
    var polishTitle : String? = null
    var title : String = ""
    var isExtension : Boolean = false
    @RequiresApi(Build.VERSION_CODES.O)
    var publishDate : LocalDate = LocalDate.of(2022,1,1)
    var thumbnail : ByteArray = ByteArray(0)

    constructor(title: String, polishTitle: String, isExtension: Boolean, publishDate: LocalDate,
                thumbnail: ByteArray){
        this.title = title
        this.polishTitle = polishTitle
        this.isExtension = isExtension
        this.publishDate = publishDate
        this.thumbnail = thumbnail
    }
}