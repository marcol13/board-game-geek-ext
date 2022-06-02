package com.example.boardgamegeekext.database

class User {

    var name : String? = ""
    var nickname : String = ""
    var image : ByteArray? = ByteArray(0)

    constructor(name : String?, nickname : String, image : ByteArray?){
        this.name = name
        this.nickname = nickname
        this.image = image
    }
}