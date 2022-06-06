package com.example.boardgamegeekext

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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "id"
private const val ARG_PARAM2 = "name"
private const val ARG_PARAM3 = "year"
private const val ARG_PARAM4 = "isExt"
private const val ARG_PARAM5 = "thumbnail"
private const val ARG_PARAM6 = "rank"

/**
 * A simple [Fragment] subclass.
 * Use the [ListItemFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListItemFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var id: String? = null
    private var name: String? = null
    private var year: String? = null
    private var isExt: Boolean? = null
    private var thumbnail: ByteArray? = null
    private var rank : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_PARAM1)
            name = it.getString(ARG_PARAM2)
            year = it.getString(ARG_PARAM3)
            isExt = it.getBoolean(ARG_PARAM4)
            thumbnail = it.getByteArray(ARG_PARAM5)
            rank = it.getInt(ARG_PARAM6)
        }
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

        Log.d("SPRAWDZENIE", arguments?.getBoolean("isExt").toString())

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

            Log.d("NIE DZIALA", arguments?.getByteArray("thumbnail")!!.size.toString())

            val bm = BitmapFactory.decodeByteArray(arguments?.getByteArray("thumbnail"), 0, arguments?.getByteArray("thumbnail")!!.size, options);
//            thumbnailImageView.setImageBitmap(Bitmap.createScaledBitmap(bm, 100, 100, true));
            lateinit var dstBmp : Bitmap
            if (bm.getWidth() >= bm.getHeight()){

                dstBmp = Bitmap.createBitmap(
                    bm,
                    bm.getWidth()/2 - bm.getHeight()/2,
                    0,
                    bm.getHeight(),
                    bm.getHeight()
                );

            }else{

                dstBmp = Bitmap.createBitmap(
                    bm,
                    0,
                    bm.getHeight()/2 - bm.getWidth()/2,
                    bm.getWidth(),
                    bm.getWidth()
                );
            }

            thumbnailImageView.setImageBitmap(bm);
        }

        var rankPositionText = arguments?.getInt("rank").toString()
        if(rankPositionText == "-1")
            rankPositionText = "0"
        rankText.text = "\uD83C\uDFC6\n" + rankPositionText

        // Inflate the layout for this fragment
        return itemView
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListItemFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(id: String, name: String, year: String, isExt : Boolean, thumbnail : ByteArray, rank : Int) =
            ListItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, id)
                    putString(ARG_PARAM2, name)
                    putString(ARG_PARAM3, year)
                    putBoolean(ARG_PARAM4, isExt)
                    putByteArray(ARG_PARAM5, thumbnail)
                    putInt(ARG_PARAM6, rank)
                }
            }
    }
}