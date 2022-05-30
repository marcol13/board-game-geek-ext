package com.example.boardgamegeekext.database

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class HistoryRanking {
    var gameId : Int = 0
    var syncId : Int = 0
    var ranking : Int = 0

    constructor(gameId : Int, syncId : Int, ranking : Int){
        this.gameId = gameId
        this.syncId = syncId
        this.ranking = ranking
    }
}