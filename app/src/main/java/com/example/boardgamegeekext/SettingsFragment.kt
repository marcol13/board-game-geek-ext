package com.example.boardgamegeekext

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.boardgamegeekext.api.*
import com.example.boardgamegeekext.database.Game
import com.example.boardgamegeekext.database.HistoryRanking
import com.example.boardgamegeekext.database.Synchronization
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var disabledEditTextEndDate : EditText

    lateinit var progressBar : ProgressBar

    lateinit var dbHandler : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val settingsView : View = inflater.inflate(R.layout.fragment_settings, container, false)

        progressBar = settingsView.findViewById(R.id.settings_progress_bar)

        val syncButton : Button = settingsView.findViewById(R.id.syncButton)
        val eraseButton : Button = settingsView.findViewById(R.id.eraseButton)

        dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        val user = dbHandler.selectUserInfo()
        val firstSync = dbHandler.selectFirstSyncInfo()
        val lastSync = dbHandler.selectLastSyncInfo()

        syncButton.setOnClickListener{
            val nowDate = LocalDateTime.now()
            if(ChronoUnit.HOURS.between(lastSync?.syncDate, nowDate) < 24){
                showTimeDialog(user!!.nickname)
            }
            else{
                showDialog(user!!.nickname)
            }
        }

        eraseButton.setOnClickListener {
            val intent = Intent(requireActivity(), InitialConfiguration::class.java)
            intent.putExtra("ERASE_DATA", "true");
            startActivity(intent)
        }

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val disabledEditTextNick : EditText = settingsView.findViewById(R.id.editDisabledNick)
        disabledEditTextNick.setText((user?.nickname ?: "nickname"), TextView.BufferType.EDITABLE);
        disabledEditTextNick.isEnabled = false

        val disabledEditTextStartDate : EditText = settingsView.findViewById(R.id.editDisabledStartDate)
        val startDateValue = firstSync?.syncDate
        disabledEditTextStartDate.setText((startDateValue?.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        disabledEditTextStartDate.isEnabled = false

        disabledEditTextEndDate = settingsView.findViewById(R.id.editDisabledEndDate)
        val endDateValue = lastSync?.syncDate
        disabledEditTextEndDate.setText((endDateValue?.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        disabledEditTextEndDate.isEnabled = false

        return settingsView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showTimeDialog(nickname : String){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_time)

        val timeYes = dialog.findViewById<Button>(R.id.time_yes)
        val timeNo = dialog.findViewById<Button>(R.id.time_no)

        timeYes.setOnClickListener {
            dialog.dismiss()
            showDialog(nickname)
        }

        timeNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showDialog(nickname: String){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog)

        val deleteYes = dialog.findViewById<Button>(R.id.delete_yes)
        val deleteNo = dialog.findViewById<Button>(R.id.delete_no)

        deleteYes.setOnClickListener {
            dialog.dismiss()
            synchronizeGames(nickname, true)
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val lastSync = LocalDateTime.now()
            disabledEditTextEndDate.setText((lastSync.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        }

        deleteNo.setOnClickListener {
            dialog.dismiss()
            synchronizeGames(nickname, false)
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val lastSync = LocalDateTime.now()
            disabledEditTextEndDate.setText((lastSync.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        }

        dialog.show()
    }

    fun synchronizeGames(nickname : String, isDeleted : Boolean){
        val games : ArrayList<Game> = ArrayList()
        val ranks  : ArrayList<HistoryRanking> = ArrayList()
        progressBar.visibility = View.VISIBLE
        val dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        RetrofitInstance.api.getCollection(nickname, "1").enqueue(object : Callback<CollectionApi> {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<CollectionApi>,
                response: Response<CollectionApi>
            ) {
                val name = response.body()?.itemList?.get(1)?.stats?.rating?.ranks?.rank?.get(0)?.value.toString()
                val gamesResponse = response.body()?.itemList
                val nextId = dbHandler.selectNextSyncIndex()

                val synchronization = Synchronization(LocalDateTime.now())
                dbHandler.addSync(synchronization)

                try{
                    if(response.body()?.amount?.toInt()!! > 0) {
                        for (i in gamesResponse?.indices!!) {
                            val el = gamesResponse[i]
                            val resultData = getDetailedData(el.objectid)
                            var imageData = ByteArray(0)
                            var isExtension = false
                            val rankPositionList = el.stats?.rating?.ranks?.rank
                            val rankingPosition = getRanking(rankPositionList!!)

                            if (resultData[0] != "") {
                                runBlocking {
                                    launch(Dispatchers.Default) {
                                        imageData = try {
                                            loadImage(resultData[0])
                                        } catch (e: Exception) {
                                            ByteArray(0)
                                        }
                                    }
                                }
                            }
                            if (resultData[1] == "boardgameexpansion") {
                                isExtension = true
                            }
                            games.add(
                                Game(
                                    el.objectid.toInt(),
                                    el.name,
                                    isExtension,
                                    el.year ?: "-1",
                                    imageData
                                )
                            )
                            ranks.add(
                                HistoryRanking(
                                    el.objectid.toInt(),
                                    nextId,
                                    rankingPosition
                                )
                            )
                        }
                        if (isDeleted) {
                            dbHandler.clearGames()
                        }
                        games.forEach { el -> dbHandler.addGame(el) }
                        ranks.forEach { el -> dbHandler.addHistory(el) }
                    }
                    else{
                        if (isDeleted) {
                            dbHandler.clearGames()
                        }
                    }
                }catch(e: Exception){
                    runBlocking {
                        launch(Dispatchers.Default) {
                            delay(3_000)
                        }
                    }
                    synchronizeGames(nickname, isDeleted)
                }

                progressBar.visibility = View.GONE
                val toast = Toast.makeText(requireContext(), "Dane zostały zsynchronizowane", Toast.LENGTH_LONG)
                toast.show()
            }

            override fun onFailure(call: Call<CollectionApi>, t: Throwable) {
                Log.v("retrofit321", t.stackTraceToString())
                runBlocking {
                    launch(Dispatchers.Default) {
                        delay(10_000)
                    }
                }
                synchronizeGames(nickname, isDeleted)
            }
        })
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
                    runBlocking {
                        launch(Dispatchers.Default) {
                            delay(10_000)
                        }
                    }
                    getDetailedData(id)
                }
            }
        }
        if(!flag){
            return arrayListOf("", "")
        }
        return arrayListOf(thumbnail, type)
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}