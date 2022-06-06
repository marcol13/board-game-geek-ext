package com.example.boardgamegeekext

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.boardgamegeekext.database.Game
import com.example.boardgamegeekext.database.HistoryRanking
import com.example.boardgamegeekext.database.Synchronization
import com.example.boardgamegeekext.database.User
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_GAME_TABLE = ("CREATE TABLE $GAME_TABLE_NAME ($GAME_ID INTEGER PRIMARY KEY, $GAME_TITLE_ORIGINAL TEXT, $GAME_IS_EXT BOOLEAN, $GAME_PUBLISH_DATE STRING, $GAME_IMG_THUMB BLOB)")

        val CREATE_SYNC_TABLE = ("CREATE TABLE $SYNC_TABLE_NAME($SYNC_ID INTEGER PRIMARY KEY, $SYNC_DATE DATE)")

        val CREATE_HISTORY_TABLE = ("CREATE TABLE $HISTORY_TABLE_NAME ($HISTORY_GAME_ID INTEGER, $HISTORY_SYNC_ID INTEGER, $HISTORY_RANKING_POSITION INTEGER, FOREIGN KEY($HISTORY_GAME_ID) REFERENCES $GAME_TABLE_NAME($GAME_ID), FOREIGN KEY($HISTORY_SYNC_ID) REFERENCES $SYNC_TABLE_NAME($SYNC_ID))")

        val CREATE_USER_TABLE = ("CREATE TABLE $USER_TABLE_NAME($USER_ID INTEGER PRIMARY KEY, $USER_NAME TEXT, $USER_NICK TEXT, $USER_IMAGE BLOB)")

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
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val values = ContentValues()
        values.put(GAME_ID, game.id)
        values.put(GAME_TITLE_ORIGINAL, game.title)
        values.put(GAME_IS_EXT, game.isExtension)
        values.put(GAME_PUBLISH_DATE, game.publishDate)
        values.put(GAME_IMG_THUMB, game.thumbnail)
        val db = this.writableDatabase
        try{
//            @Insert(onConflict = OnConflictStrategy.REPLACE)
//            db.insert(GAME_TABLE_NAME, null, values)
            db.insertWithOnConflict(GAME_TABLE_NAME, null, values, CONFLICT_REPLACE)
        }catch(e: SQLiteConstraintException){
            Log.d("BLAD", "COS NIE DZIALA")
        }

        db.close()
    }

    fun addSync(sync: Synchronization){
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val values = ContentValues()
        values.put(SYNC_DATE, sync.syncDate.toString())
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
        values.put(USER_IMAGE, user.image)
        val db = this.writableDatabase
        db.insert(USER_TABLE_NAME, null, values)
        db.close()
    }

    fun clearDatabase(){
        val db = this.writableDatabase
        db?.execSQL("DELETE FROM $USER_TABLE_NAME")
        db?.execSQL("DELETE FROM $HISTORY_TABLE_NAME")
        db?.execSQL("DELETE FROM $SYNC_TABLE_NAME")
        db?.execSQL("DELETE FROM $GAME_TABLE_NAME")
    }

    fun clearGames(){
        val db = this.writableDatabase
        db?.execSQL("DELETE FROM $GAME_TABLE_NAME")
    }

    fun selectLastRanking(gameId : Int) : Int{
        val query = "SELECT $HISTORY_RANKING_POSITION FROM $HISTORY_TABLE_NAME WHERE $HISTORY_GAME_ID = $gameId ORDER BY $HISTORY_SYNC_ID DESC LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var result = 0
        if(cursor.moveToFirst()){
            result = cursor.getInt(0)
            cursor.close()
        }
        db.close()
        return result
    }

    fun selectNextSyncIndex() : Int{
        val query = "SELECT $SYNC_ID FROM $SYNC_TABLE_NAME ORDER BY $SYNC_ID DESC LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var nextId = 0

        if(cursor.moveToFirst()){
            nextId = cursor.getInt(0)
            cursor.close()
        }
        db.close()
        return nextId + 1
    }

    fun selectGamesList() : ArrayList<GameListResponse>{
        val syncId = selectNextSyncIndex() - 1
        Log.d("DEEBUG2115", syncId.toString())
//        val query = "SELECT $GAME_TABLE_NAME.$GAME_ID, $GAME_TITLE_ORIGINAL, $GAME_IS_EXT, $GAME_PUBLISH_DATE, $GAME_IMG_THUMB, $HISTORY_RANKING_POSITION FROM $GAME_TABLE_NAME INNER JOIN $HISTORY_TABLE_NAME ON $GAME_TABLE_NAME.$GAME_ID = $HISTORY_TABLE_NAME.$HISTORY_GAME_ID WHERE $HISTORY_TABLE_NAME.$HISTORY_SYNC_ID = $syncId"
//        val query = "SELECT $GAME_TABLE_NAME.$GAME_ID, $GAME_TITLE_ORIGINAL, $GAME_IS_EXT, $GAME_PUBLISH_DATE, $GAME_IMG_THUMB, $HISTORY_RANKING_POSITION FROM $GAME_TABLE_NAME INNER JOIN $HISTORY_TABLE_NAME ON $GAME_TABLE_NAME.$GAME_ID = $HISTORY_TABLE_NAME.$HISTORY_GAME_ID WHERE $HISTORY_TABLE_NAME.$HISTORY_SYNC_ID = $syncId"
        val query = "SELECT $GAME_TABLE_NAME.$GAME_ID, $GAME_TITLE_ORIGINAL, $GAME_IS_EXT, $GAME_PUBLISH_DATE, $GAME_IMG_THUMB FROM $GAME_TABLE_NAME"
        val db = this.readableDatabase
        Log.d("UUUUU", query)
        val cursor = db.rawQuery(query, null)
        var gameList = ArrayList<GameListResponse>()
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                Log.d("DEBUG001", "AAAAAAAAAAAAAAA")
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val isExt = cursor.getInt(2) > 0
                Log.d("TUUUU TUUUU UUUUU UUU", isExt.toString())
                val publishDate = cursor.getString(3)
                val thumbnail = cursor.getBlob(4)
                val ranking = selectLastRanking(id)
//                val ranking = 1

                gameList.add(GameListResponse(id,  title, publishDate, thumbnail, ranking, isExt))
                cursor.moveToNext()
            }
            cursor.close()
        }
        db.close()
        return gameList
    }

    fun selectUserInfo() : User?{
        val query = "SELECT * FROM $USER_TABLE_NAME LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var user: User? = null

        if(cursor.moveToFirst()){
            val name = cursor.getString(1)
            val nickname = cursor.getString(2)
            val image = cursor.getBlob(3)
            user = User(name, nickname, image)
            cursor.close()
        }
        db.close()
        return user
    }

    fun selectGamesAmount() : Int{
        val query = "SELECT COUNT(*) FROM $GAME_TABLE_NAME WHERE $GAME_IS_EXT = false"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var result = 0
        if(cursor.moveToFirst()){
            result = cursor.getInt(0)
            cursor.close()
        }
        db.close()
        return result
    }

    fun selectExtensionAmount() : Int{
        val query = "SELECT COUNT(*) FROM $GAME_TABLE_NAME WHERE $GAME_IS_EXT = true"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var result = 0
        if(cursor.moveToFirst()){
            result = cursor.getInt(0)
            cursor.close()
        }
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun selectFirstSyncInfo() : Synchronization?{
        val query = "SELECT * FROM $SYNC_TABLE_NAME LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var sync: Synchronization? = null

        if(cursor.moveToFirst()){
            val syncDate = cursor.getString(1)
//            val formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatter : DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            sync = Synchronization(LocalDateTime.parse(syncDate, formatter))
            cursor.close()
        }
        db.close()
        return sync
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun selectLastSyncInfo() : Synchronization?{
        val query = "SELECT * FROM $SYNC_TABLE_NAME ORDER BY $SYNC_ID DESC LIMIT 1"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        var sync: Synchronization? = null

        if(cursor.moveToFirst()){
            val syncDate = cursor.getString(1)
//            val formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatter : DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            sync = Synchronization(LocalDateTime.parse(syncDate, formatter))
            cursor.close()
        }
        db.close()
        return sync
    }

    class GameListResponse{
        var id = 0
        var title = ""
        var publishedDate = "2022"
        var thumbnail = ByteArray(0)
        var rank = -1
        var isExtension = false
        constructor(id : Int, title : String, publishedDate : String, thumbnail : ByteArray, rank : Int, isExtension : Boolean){
            this.id = id
            this.title = title
            this.publishedDate = publishedDate
            this.thumbnail = thumbnail
            this.rank = rank
            this.isExtension = isExtension
        }
    }

    companion object {
        private val DATABASE_NAME : String = "BoardGames.db"
        private val DATABASE_VERSION : Int = 1

        val GAME_TABLE_NAME = "games"
        val GAME_ID = "_id"
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
        val USER_IMAGE = "user_image"
    }
}