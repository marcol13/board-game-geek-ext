package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime

class Synchronization {
    @RequiresApi(Build.VERSION_CODES.O)
    var syncDate : LocalDateTime? = LocalDateTime.now()

    constructor(syncDate: LocalDateTime?){
        this.syncDate = syncDate
    }
}