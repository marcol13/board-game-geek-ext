package com.example.boardgamegeekext.database

class Game(
    var id: Int,
    var title: String,
    var isExtension: Boolean,
    var publishDate: String,
    var thumbnail: ByteArray
)