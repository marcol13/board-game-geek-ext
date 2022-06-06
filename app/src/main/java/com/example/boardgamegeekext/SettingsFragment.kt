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
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.boardgamegeekext.api.*
import com.example.boardgamegeekext.database.Game
import com.example.boardgamegeekext.database.HistoryRanking
import com.example.boardgamegeekext.database.Synchronization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
            showDialog(user!!.nickname)
        }

        eraseButton.setOnClickListener {
            val intent = Intent(requireActivity(), InitialConfiguration::class.java)
            intent.putExtra("ERASE_DATA", "true");
            startActivity(intent)
        }



        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val disabledEditTextNick : EditText = settingsView.findViewById(R.id.editDisabledNick)
//        disabledEditTextNick.text = (user?.nickname ?: "nickname") as Editable?
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

        // Inflate the layout for this fragment
        return settingsView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showDialog(nickname: String){
        val dialog : Dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog)

        val deleteYes = dialog.findViewById<Button>(R.id.delete_yes)
        val deleteNo = dialog.findViewById<Button>(R.id.delete_no)

        deleteYes.setOnClickListener {
            dialog.dismiss()
            synchronizeGames(nickname, true)
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val lastSync = dbHandler.selectLastSyncInfo()
            disabledEditTextEndDate.setText((lastSync?.syncDate?.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        }

        deleteNo.setOnClickListener {
            dialog.dismiss()
            synchronizeGames(nickname, false)
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val lastSync = dbHandler.selectLastSyncInfo()
            disabledEditTextEndDate.setText((lastSync?.syncDate?.format(formatter) ?: "27.05.2022"), TextView.BufferType.EDITABLE)
        }

        dialog.show()
    }

    fun synchronizeGames(nickname : String, isDeleted : Boolean){
        var games : ArrayList<Game> = ArrayList()
        var ranks  : ArrayList<HistoryRanking> = ArrayList()
        progressBar.visibility = View.VISIBLE
        val dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        RetrofitInstance.api.getCollection(nickname, "1").enqueue(object : Callback<CollectionApi> {

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
                                try{
                                    imageData = loadImage(resultData[0])
                                }catch(e: Exception){
                                    imageData = ByteArray(0)
                                }
                            }
                        }
                    }
                    Log.d("CAN WE SKIP", resultData[1])
                    if(resultData[1] == "boardgameexpansion"){
                        Log.d("QWERTY", "CCCCCCCCC")
                        isExtension = true
                    }
                    Log.d("TO THE GOOD PART", isExtension.toString())
                    games.add(Game(el.objectid.toInt(), el.name, isExtension, el.year ?: "-1", imageData))
                    Log.d("ABDER", "${el.objectid} ${nextId} ${rankingPosition}")
                    ranks.add(HistoryRanking(el.objectid.toInt(), nextId!!, rankingPosition))
                }
                if(isDeleted){
                    dbHandler.clearGames()
                }
                games.forEach{el -> dbHandler.addGame(el)}
                ranks.forEach{el -> dbHandler.addHistory(el)}

                progressBar.visibility = View.GONE

                Log.d("SIEMA2137", name)
            }

            override fun onFailure(call: Call<CollectionApi>, t: Throwable) {
                Log.v("retrofit321", t.stackTraceToString())
                Thread.sleep(1_000)
                synchronizeGames(nickname, isDeleted)
            }
        })
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
                    Log.d("RESPONSE", response.toString())
                    if(response.code() != 200){
                        throw Exception()
                    }
                    flag = true

                } catch (e: Exception) {
                    Thread.sleep(500)
                    getDetailedData(id)
                    Log.d("DUPA", e.stackTraceToString())
                }
            }
        }
        Log.d("TU COS", "HALOOOO")
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
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