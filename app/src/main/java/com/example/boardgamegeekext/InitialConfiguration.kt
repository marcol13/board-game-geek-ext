package com.example.boardgamegeekext

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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

    var userNickname = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_configuration)

        goToApp = findViewById(R.id.accept_nick_name_initial_button)
        registerInWebsite = findViewById(R.id.go_to_website_initial_button)
        nicknameEdit = findViewById(R.id.nick_name_initial_edit)

        val flag = intent.getStringExtra("ERASE_DATA")
        if (!flag.equals("true")) {
            val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
            val db =dbHandler.writableDatabase
//            dbHandler.onUpgrade(db, 1, 1)
//            dbHandler.clearDatabase()
            val user = dbHandler.selectUserInfo()
            if (user != null) {
                goToMainActivity()
            }
        }


        goToApp.setOnClickListener(object: View.OnClickListener {

            override fun onClick(arg0: View?){
                val nicknameText = nicknameEdit.text.toString()
                var fetchUser = false
                do{
                    fetchUser = findUser(nicknameText)
                    Thread.sleep(1_000)
                }while(fetchUser)
//                val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
//                val user = dbHandler.selectUserInfo()
//                Log.d("UWAGA", userNickname)
//                synchronizeGames(userNickname)


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

        var apiService : ApiRequest = RetrofitInstance.api
        var call : Call<DetailedGameDataApi> = apiService.getDetailedGameData(id)

        runBlocking {
            val job: Job = launch(Dispatchers.Default) {

                try {
                    var response: Response<DetailedGameDataApi> = call.execute()
                    Log.d("DUPA", "teraz jest git")
                    thumbnail = response?.body()?.name?.thumbnail.toString()
                    type = response?.body()?.name?.type.toString()
                    Log.d("SIEMA22222", type)
                    flag = true

                } catch (e: Exception) {
                    //            Thread.sleep(1_000)
                    //            getDetailedData(id)
                    Log.d("DUPA", e.stackTraceToString())
                }
            }
        }



//        runBlocking{
//            val job : Job = launch {
//                RetrofitInstance.api.getDetailedGameData(id)
//                    .enqueue(object : Callback<DetailedGameDataApi> {
//                        override fun onResponse(
//                            call: Call<DetailedGameDataApi>,
//                            response: Response<DetailedGameDataApi>
//                        ) {
//                            thumbnail = response?.body()?.name?.thumbnail.toString()
//                            type = response?.body()?.name?.type.toString()
//                            Log.d("SIEMA22222", type)
//                            flag = true
//                        }
//
//                        override fun onFailure(call: Call<DetailedGameDataApi>, t: Throwable) {
//                            Log.v("retrofit", t.stackTrace.toString())
//                        }
//                    })
//            }
//        }
        Log.d("TU COS", "HALOOOO")
        if(!flag){
            return arrayListOf("", "")
        }
        return arrayListOf(thumbnail, type)
    }

    fun synchronizeGames(nickname : String){
        var games : ArrayList<Game> = ArrayList()
        var ranks  : ArrayList<HistoryRanking> = ArrayList()
        val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
        RetrofitInstance.api.getCollection(nickname, "1").enqueue(object : Callback<CollectionApi> {

//            @RequiresApi(Build.VERSION_CODES.O)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<CollectionApi>,
                response: Response<CollectionApi>
            ) {
                var name = response?.body()?.itemList?.get(1)?.stats?.rating?.ranks?.rank?.get(0)?.value.toString()
                val gamesResponse = response?.body()?.itemList
                val nextId = dbHandler.selectNextSyncIndex()

                val synchronization = Synchronization(LocalDateTime.now())
                dbHandler.addSync(synchronization)
//
                for(i in gamesResponse?.indices!!){
                    var el = gamesResponse.get(i)
                    var resultData = getDetailedData(el.objectid)
                    var imageData = ByteArray(0)
                    var isExtension = false
                    var rankPositionList = el.stats?.rating?.ranks?.rank
                    var rankingPosition = getRanking(rankPositionList!!)

                    Log.d("TU MOZE BYC COS ZLE", resultData[0])

                    if(resultData[0] != ""){
                        runBlocking {
                            val job: Job = launch(Dispatchers.Default) {
                                imageData = loadImage(resultData[0])
                            }
                        }
                    }
                    Log.d("CAN WE SKIP", resultData[1])
                    if(resultData[1] == "boardgameexpansion"){
                        isExtension = true
                    }
                    Log.d("TO THE GOOD PART", isExtension.toString())
                    games.add(Game(el.name, isExtension, el.year, imageData))
                    ranks.add(HistoryRanking(el.objectid.toInt(), nextId!!, rankingPosition))
                }
                games.forEach{el -> dbHandler.addGame(el)}
                ranks.forEach{el -> dbHandler.addHistory(el)}

                goToMainActivity()

////                var detailedResponse = ArrayList()
//
//                gamesResponse?.forEach { getDetailedData(it.objectid) }
//


//                var name = response?.body()?.amount.toString()
                Log.d("SIEMA2137", name)
            }

            override fun onFailure(call: Call<CollectionApi>, t: Throwable) {
                Log.v("retrofit321", t.stackTraceToString())
            }
        })
    }

    fun findUser(nickname : String): Boolean {
        var flag = false
        lateinit var user : User
        RetrofitInstance.api.getUser(nickname).enqueue(object : Callback<UserApi> {


            override fun onFailure(call: Call<UserApi>?, t: Throwable?) {
                Log.v("retrofit", "call failed")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<UserApi>?, response: Response<UserApi>?) {
                val name = response?.body()?.name?.name.toString()
                val nickname = response?.body()?.nickname.toString()
                val image = response?.body()?.avatar?.avatar.toString()
                Log.d("SIEMA", nickname)
                if(nickname == ""){
                    Log.d("SIEMA2", nickname)
                    val toast = Toast.makeText(applicationContext, "Nie znaleziono użytkownika", Toast.LENGTH_LONG)
                    toast.show()
                }
                else{
                    runBlocking {
                        val job = launch(Dispatchers.Default){
                            lateinit var avatar : ByteArray
                            if(image != "" && image != "N/A"){
                                Log.d("SIEMA", image)
                                avatar = loadImage(image)
                                Log.d("SIEMA", "sukces")
                            }
                            else{
                                avatar = ByteArray(0)
                            }

                            user = User(name, nickname, avatar)

//                            val synchronization = Synchronization(LocalDateTime.now())
//                            val dateFormat = SimpleDateTimeFormat("yyyy-MM-dd HH:mm:ss")
//                            Log.d("SIEMA2", synchronization.syncDate.toString())
//                            Log.d("SIEMA3", dateFormat.format(synchronization.syncDate))


                            val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
                            dbHandler.clearDatabase()
                            dbHandler.addUser(user)
                            Log.d("UWAGA2", nickname)
                            synchronizeGames(nickname)
//                            dbHandler.addSync(synchronization)


//                            goToMainActivity()
                        }
                    }
                    flag = true
                }
            }
        })
        return flag
    }

    fun getRanking(rankPositionList : List<RankInstance>) : Int{
        for(j in 0 until (rankPositionList?.size!!)){
            var temp = rankPositionList.get(j)
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