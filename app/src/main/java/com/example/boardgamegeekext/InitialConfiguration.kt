package com.example.boardgamegeekext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.boardgamegeekext.api.RetrofitInstance
import com.example.boardgamegeekext.api.UserApi
import com.example.boardgamegeekext.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


class InitialConfiguration : AppCompatActivity() {

    private lateinit var goToApp : Button
    lateinit var registerInWebsite : Button
    lateinit var nicknameEdit : EditText

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
                findUser(nicknameText)
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

    fun findUser(nickname : String){
        lateinit var user : User
        RetrofitInstance.api.getUser(nickname).enqueue(object : Callback<UserApi> {


            override fun onFailure(call: Call<UserApi>?, t: Throwable?) {
                Log.v("retrofit", "call failed")
            }

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
                                val url = URL(image)
                                val connection = url.openConnection()
                                connection.connect()
                                val InputStream = connection.getInputStream()
                                avatar = InputStream.readBytes()
                                InputStream.close()
                                Log.d("SIEMA", "sukces")
                            }
                            else{
                                avatar = ByteArray(0)
                            }

                            user = User(name, nickname, avatar)

                            val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
                            dbHandler.clearDatabase()
                            dbHandler.addUser(user)

                            goToMainActivity()
                        }
                    }

                }
            }
        })
    }

//    private inner class resolveApiData(){
//
//    }
}