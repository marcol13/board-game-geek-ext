package com.example.boardgamegeekext

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamegeekext.api.*
import com.example.boardgamegeekext.database.Game
import com.example.boardgamegeekext.database.HistoryRanking
import com.example.boardgamegeekext.database.Synchronization
import com.example.boardgamegeekext.database.User
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.time.LocalDateTime

class InitialConfiguration : AppCompatActivity() {

    private lateinit var goToApp : Button
    lateinit var registerInWebsite : Button
    lateinit var nicknameEdit : EditText
    lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_configuration)

        goToApp = findViewById(R.id.accept_nick_name_initial_button)
        registerInWebsite = findViewById(R.id.go_to_website_initial_button)
        nicknameEdit = findViewById(R.id.nick_name_initial_edit)
        progressBar = findViewById(R.id.initial_progress_bar)

        val flag = intent.getStringExtra("ERASE_DATA")
        if (!flag.equals("true")) {
            val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
//            val db =dbHandler.writableDatabase
//            dbHandler.onUpgrade(db, 1, 1)
//            dbHandler.clearDatabase()
            val user = dbHandler.selectUserInfo()
            if (user != null) {
                goToMainActivity()
            }
        }
        savedInstanceState?.clear();

        goToApp.setOnClickListener(object: View.OnClickListener {

            override fun onClick(arg0: View?){
                val nicknameText = nicknameEdit.text.toString()
                if(nicknameText != ""){
                    progressBar.visibility = View.VISIBLE
                    var fetchUser = false
                    do{
                        fetchUser = findUser(nicknameText)
                        Thread.sleep(1_000)
                    }while(fetchUser)
                }
            }
        })

        registerInWebsite.setOnClickListener(object : View.OnClickListener {
            var uriUrl = Uri.parse("https://boardgamegeek.com")
            var launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)

            override fun onClick(arg0: View?) {
                startActivity(launchBrowser)
            }
        })
    }

    fun goToMainActivity(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getDetailedData(id : String): ArrayList<String> {
        var flag = false
        var thumbnail = ""
        var type = ""

        val apiService : ApiRequest = RetrofitInstance.api
        val call : Call<DetailedGameDataApi> = apiService.getDetailedGameData(id)

        runBlocking {
            launch(Dispatchers.Default) {
                try {
                    val response: Response<DetailedGameDataApi> = call.execute()
                    thumbnail = response.body()?.name?.thumbnail.toString()
                    type = response.body()?.name?.type.toString()
                    if(response.code() != 200){
                        throw Exception()
                    }
                    flag = true
                } catch (e: Exception) {
                    delay(3_000)
                    getDetailedData(id)
                }
            }
        }

        if(!flag){
            return arrayListOf("", "")
        }
        return arrayListOf(thumbnail, type)
    }

    fun synchronizeGames(nickname : String){
        val games : ArrayList<Game> = ArrayList()
        val ranks  : ArrayList<HistoryRanking> = ArrayList()
        val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
        RetrofitInstance.api.getCollection(nickname, "1").enqueue(object : Callback<CollectionApi> {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<CollectionApi>,
                response: Response<CollectionApi>
            ) {
                try{
//                    val name = response.body()?.itemList?.get(1)?.stats?.rating?.ranks?.rank?.get(0)?.value.toString()
                    val gamesResponse = response.body()?.itemList
                    val nextId = dbHandler.selectNextSyncIndex()

                    val synchronization = Synchronization(LocalDateTime.now())
                    dbHandler.addSync(synchronization)

                    if(response.body()?.amount?.toInt()!! > 0){
                        for(i in gamesResponse?.indices!!){
                            val el = gamesResponse[i]
                            val resultData = getDetailedData(el.objectid)
                            var imageData = ByteArray(0)
                            var isExtension = false
                            val rankPositionList = el.stats?.rating?.ranks?.rank
                            val rankingPosition = getRanking(rankPositionList!!)

                            if(resultData[0] != ""){
                                runBlocking {
                                    launch(Dispatchers.Default) {
                                        imageData = try{
                                            loadImage(resultData[0])
                                        }catch(e: Exception){
                                            ByteArray(0)
                                        }
                                    }
                                }
                            }

                            if(resultData[1] == "boardgameexpansion"){
                                isExtension = true
                            }

                            games.add(Game(el.objectid.toInt(), el.name, isExtension, el.year ?: "-1", imageData))
                            ranks.add(HistoryRanking(el.objectid.toInt(), nextId, rankingPosition))
                        }

                        games.forEach{el -> dbHandler.addGame(el)}
                        ranks.forEach{el -> dbHandler.addHistory(el)}
                    }

                    progressBar.visibility = View.GONE

                    goToMainActivity()

                }catch(e: Exception){
                    runBlocking {
                        launch(Dispatchers.Default) {
                            delay(3_000)
                        }
                    }
                    synchronizeGames(nickname)
                }
            }

            override fun onFailure(call: Call<CollectionApi>, t: Throwable) {
                Log.v("retrofit321", t.stackTraceToString())
                runBlocking {
                    launch(Dispatchers.Default) {
                        delay(1_000)
                    }
                }
                synchronizeGames(nickname)
            }
        })
    }

    fun findUser(nickname : String): Boolean {
        var flag = false
        lateinit var user : User
        RetrofitInstance.api.getUser(nickname).enqueue(object : Callback<UserApi> {

            override fun onFailure(call: Call<UserApi>?, t: Throwable?) {
                Log.v("retrofit", "call failed")
                runBlocking {
                    launch(Dispatchers.Default) {
                        delay(3_000)
                    }
                }
                findUser(nickname)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<UserApi>?, response: Response<UserApi>?) {
                val name = response?.body()?.name?.name.toString()
                val nickname = response?.body()?.nickname.toString()
                val image = response?.body()?.avatar?.avatar.toString()
                if(nickname == ""){
                    val toast = Toast.makeText(applicationContext, "Nie znaleziono użytkownika", Toast.LENGTH_LONG)
                    toast.show()
                    progressBar.visibility = View.GONE
                }
                else{
                    runBlocking {
                        launch(Dispatchers.Default){
                            val avatar : ByteArray = if(image != "" && image != "N/A"){
                                loadImage(image)
                            } else{
                                ByteArray(0)
                            }

                            user = User(name, nickname, avatar)

                            val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
                            dbHandler.clearDatabase()
                            dbHandler.addUser(user)

                            synchronizeGames(nickname)
                        }
                    }
                    flag = true
                }
            }
        })
        return flag
    }

    fun getRanking(rankPositionList : List<RankInstance>) : Int{
        for(j in 0 until (rankPositionList.size)){
            val temp = rankPositionList[j]
            var result = -1
            if(temp.type == "subtype")
                result = try{
                    temp.value.toInt()
                }catch (e: NumberFormatException){
                    -1
                }
                return result
        }
        return -1
    }

    fun loadImage(urlArg : String) : ByteArray{
        val url = URL(urlArg)
        val connection = url.openConnection()
        connection.connect()
        val InputStream = connection.getInputStream()
        val result = InputStream.readBytes()
        InputStream.close()
        return result
    }
}