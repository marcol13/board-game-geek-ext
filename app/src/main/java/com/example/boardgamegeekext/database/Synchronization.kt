package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class Synchronization {
    @RequiresApi(Build.VERSION_CODES.O)
    var syncDate : LocalDate = LocalDate.of(2022,1,1)

    constructor(syncDate: LocalDate){
        this.syncDate = syncDate
    }
}