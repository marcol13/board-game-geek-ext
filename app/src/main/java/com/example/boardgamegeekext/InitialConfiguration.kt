package com.example.boardgamegeekext

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class InitialConfiguration : AppCompatActivity() {

    lateinit var goToApp : Button
    lateinit var registerInWebsite : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_configuration)

        goToApp = findViewById(R.id.accept_nick_name_initial_button)
        registerInWebsite = findViewById(R.id.go_to_website_initial_button)

        registerInWebsite.setOnClickListener(object : View.OnClickListener {

            var uriUrl = Uri.parse("https://boardgamegeek.com")
            var launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)

            override fun onClick(arg0: View?) {
                startActivity(launchBrowser)
            }
        })
    }
}