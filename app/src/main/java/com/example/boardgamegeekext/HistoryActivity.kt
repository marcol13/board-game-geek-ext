package com.example.boardgamegeekext

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val b = intent.extras

        val dbHandler = DatabaseHelper(applicationContext, null, null, 1)
        var history = dbHandler.selectGameHistory(b!!.getInt("idGame"))

        val gameName = findViewById<TextView>(R.id.game_title)
        val gameYear = findViewById<TextView>(R.id.game_year)
        val backButton = findViewById<Button>(R.id.back_button)
        val list = findViewById<ListView>(R.id.list_history)

        gameName.text = b?.getString("name")
        gameYear.text = "(${b?.getString("year")})"

        Log.d("GGGGG", b?.getInt("idGame").toString())

//        val listItems = arrayOfNulls<String>(history.size)

        val listItems: MutableList<HashMap<String, String>> = ArrayList()

        val adapter = SimpleAdapter(this, listItems, R.layout.list_item, arrayOf("First Line", "Second Line"),
            intArrayOf(R.id.item_ranking, R.id.item_date))

        val iter: Iterator<*> = createMap(history).entries.iterator()
        while (iter.hasNext()) {
            val resultsMap: HashMap<String, String> = HashMap()
            val (key, value) = iter.next() as Map.Entry<*, *>
            resultsMap["First Line"] = value.toString()
            resultsMap["Second Line"] = key.toString()
            listItems.add(resultsMap)
        }

        list.adapter = adapter


        backButton.setOnClickListener { finish() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createMap(arr :  ArrayList<DatabaseHelper.HistoryListResponse>) : HashMap<String, String>{
        var result = HashMap<String, String>()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        for(i in arr){
            Log.d("RRRRRR", i.syncDate)

            var tempDate = LocalDateTime.parse(i.syncDate, ISO_DATE_TIME)
            result.put(tempDate.format(formatter), "🏆 ${i.rankPosition}")
        }
        return result
    }
}