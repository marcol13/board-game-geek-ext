package com.example.boardgamegeekext

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.boardgamegeekext.database.Game
import com.example.boardgamegeekext.database.HistoryRanking
import com.example.boardgamegeekext.database.Synchronization
import com.example.boardgamegeekext.database.User
import java.text.SimpleDateFormat

class DatabaseHelper(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_GAME_TABLE = ("CREATE TABLE $GAME_TABLE_NAME ($GAME_ID INTEGER PRIMARY KEY, $GAME_TITLE_POLISH TEXT, $GAME_TITLE_ORIGINAL TEXT, $GAME_IS_EXT BOOLEAN, $GAME_PUBLISH_DATE DATE, $GAME_IMG_THUMB BLOB)")

        val CREATE_SYNC_TABLE = ("CREATE TABLE $SYNC_TABLE_NAME($SYNC_ID INTEGER PRIMARY KEY, $SYNC_DATE DATE)")

        val CREATE_HISTORY_TABLE = ("CREATE TABLE $HISTORY_TABLE_NAME ($HISTORY_GAME_ID INTEGER, $HISTORY_SYNC_ID INTEGER, $HISTORY_RANKING_POSITION INTEGER, FOREIGN KEY($HISTORY_GAME_ID) REFERENCES $GAME_TABLE_NAME($GAME_ID), FOREIGN KEY($HISTORY_SYNC_ID) REFERENCES $SYNC_TABLE_NAME($SYNC_ID))")

        val CREATE_USER_TABLE = ("CREATE TABLE $USER_TABLE_NAME($USER_ID INTEGER PRIMARY KEY, $USER_NAME TEXT, $USER_NICK TEXT)")

        db?.execSQL(CREATE_GAME_TABLE)
        db?.execSQL(CREATE_SYNC_TABLE)
        db?.execSQL(CREATE_HISTORY_TABLE)
        db?.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldBersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $HISTORY_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $SYNC_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $GAME_TABLE_NAME")
        onCreate(db)
    }

    fun addGame(game: Game){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val values = ContentValues()
        values.put(GAME_TITLE_POLISH, game.polishTitle)
        values.put(GAME_TITLE_ORIGINAL, game.title)
        values.put(GAME_IS_EXT, game.isExtension)
        values.put(GAME_PUBLISH_DATE, dateFormat.format(game.publishDate))
        values.put(GAME_IMG_THUMB, game.thumbnail)
        val db = this.writableDatabase
        db.insert(GAME_TABLE_NAME, null, values)
        db.close()
    }

    fun addSync(sync: Synchronization){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val values = ContentValues()
        values.put(SYNC_DATE, dateFormat.format(sync.syncDate))
        val db = this.writableDatabase
        db.insert(SYNC_TABLE_NAME, null, values)
        db.close()
    }

    fun addHistory(history: HistoryRanking){
        val values = ContentValues()
        values.put(HISTORY_GAME_ID, history.gameId)
        values.put(HISTORY_SYNC_ID, history.syncId)
        values.put(HISTORY_RANKING_POSITION, history.ranking)
        val db = this.writableDatabase
        db.insert(HISTORY_TABLE_NAME, null, values)
        db.close()
    }

    fun addUser(user: User){
        val values = ContentValues()
        values.put(USER_NAME, user.name)
        values.put(USER_NICK, user.nickname)
        val db = this.writableDatabase
        db.insert(USER_TABLE_NAME, null, values)
        db.close()
    }

    fun selectUserInfo() : User?{
        val query = "SELECT * FROM $USER_TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var user: User? = null

        if(cursor.moveToFirst()){
            val name = cursor.getString(1)
            val nickname = cursor.getString(2)
            user = User(name, nickname)
            cursor.close()
        }
        db.close()
        return user
    }

    companion object {
        private val DATABASE_NAME : String = "BoardGames.db"
        private val DATABASE_VERSION : Int = 1

        val GAME_TABLE_NAME = "games"
        val GAME_ID = "_id"
        val GAME_TITLE_POLISH = "game_polish_title"
        val GAME_TITLE_ORIGINAL = "game_english_title"
        val GAME_IS_EXT = "game_is_extension"
        val GAME_PUBLISH_DATE = "game_publish_date"
        val GAME_IMG_THUMB = "game_image_thumbnail"

        val SYNC_TABLE_NAME = "sync"
        val SYNC_ID = "_id"
        val SYNC_DATE = "sync_date"

        val HISTORY_TABLE_NAME = "history"
        val HISTORY_GAME_ID = "history_game_id"
        val HISTORY_SYNC_ID = "history_sync_id"
        val HISTORY_RANKING_POSITION = "history_ranking_position"

        val USER_TABLE_NAME = "user"
        val USER_ID = "_id"
        val USER_NAME = "user_name"
        val USER_NICK = "user_nick"

    }

}