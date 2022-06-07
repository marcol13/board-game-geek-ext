package com.example.boardgamegeekext

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

private const val ARG_PARAM1 = "id"
private const val ARG_PARAM2 = "name"
private const val ARG_PARAM3 = "year"
private const val ARG_PARAM4 = "isExt"
private const val ARG_PARAM5 = "thumbnail"
private const val ARG_PARAM6 = "rank"
private const val ARG_PARAM7 = "idGame"

class ListItemFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var id: String? = null
    private var name: String? = null
    private var year: String? = null
    private var isExt: Boolean? = null
    private var thumbnail: ByteArray? = null
    private var rank : Int? = null
    private var idGame : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
            name = it.getString(ARG_PARAM2)
            year = it.getString(ARG_PARAM3)
            isExt = it.getBoolean(ARG_PARAM4)
            thumbnail = it.getByteArray(ARG_PARAM5)
            rank = it.getInt(ARG_PARAM6)
            idGame = it.getInt(ARG_PARAM7)
        }

        savedInstanceState?.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val itemView : View = inflater.inflate(R.layout.fragment_list_item, container, false)
        val idText : TextView = itemView.findViewById(R.id.list_index)
        val gameNameText : TextView = itemView.findViewById(R.id.game_name)
        val gameInfoText : TextView = itemView.findViewById(R.id.game_info)
        val thumbnailImageView : ImageView = itemView.findViewById(R.id.game_thumbnail)
        val rankText : TextView = itemView.findViewById(R.id.rank_position)

        idText.text = arguments?.getString("id") ?: "0."

        gameNameText.text = (arguments?.getString("name") + " (" + arguments?.getString("year") +")") ?: "Kawerna: Rolnicy z Jaskiń"

        if(isExt == true){
            gameInfoText.text = "✅ dodatek"
        }else{
            gameInfoText.text = "❌ dodatek"
        }

        if(!arguments?.getByteArray("thumbnail").contentEquals(ByteArray(0))){
            val options = BitmapFactory.Options()
            options.inDither = false
            options.inPurgeable = true
            options.inInputShareable = true

            val bm = BitmapFactory.decodeByteArray(arguments?.getByteArray("thumbnail"), 0, arguments?.getByteArray("thumbnail")!!.size, options);
            thumbnailImageView.setImageBitmap(bm);
        }

        var rankPositionText = arguments?.getInt("rank").toString()
        if(rankPositionText == "-1")
            rankPositionText = "0"
        rankText.text = "\uD83C\uDFC6\n" + rankPositionText

        if(isExt == false){
            itemView.setOnClickListener {
                val intent : Intent? = Intent(requireActivity(), HistoryActivity::class.java)
                val b = Bundle()

                b.putString("name", arguments?.getString("name"))
                b.putString("year", arguments?.getString("year"))
                b.putInt("idGame", idGame!!)

                intent?.putExtras(b)

                startActivity(intent)
            }
        }

        return itemView
    }

    companion object {
       @JvmStatic
        fun newInstance(id: String, name: String, year: String, isExt : Boolean, thumbnail : ByteArray, rank : Int, idGame : Int) =
            ListItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, id)
                    putString(ARG_PARAM2, name)
                    putString(ARG_PARAM3, year)
                    putBoolean(ARG_PARAM4, isExt)
                    putByteArray(ARG_PARAM5, thumbnail)
                    putInt(ARG_PARAM6, rank)
                    putInt(ARG_PARAM7, idGame)
                }
            }
    }
}