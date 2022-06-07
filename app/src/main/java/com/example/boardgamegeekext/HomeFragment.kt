package com.example.boardgamegeekext

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import java.time.format.DateTimeFormatter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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

        val homeView = inflater.inflate(R.layout.fragment_home, container, false)

        val dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        val user = dbHandler.selectUserInfo()
        val lastSync = dbHandler.selectLastSyncInfo()
        val gameAmountValue = dbHandler.selectGamesAmount()
        val extensionAmountValue = dbHandler.selectExtensionAmount()

        val nameTextView : TextView = homeView.findViewById(R.id.hello_name)
        val nickTextView : TextView = homeView.findViewById(R.id.nick_name)
        val lastSyncTextView : TextView = homeView.findViewById(R.id.last_sync_date)
        val avatarImageView : ImageView = homeView.findViewById(R.id.avatar_image)
        val gamesAmount : TextView = homeView.findViewById(R.id.games_amount)
        val extensionAmount : TextView = homeView.findViewById(R.id.extension_amount)

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        nameTextView.text = "Cześć " + (user?.name ?: "Nieznajomy") + "!"
        nickTextView.text = "\uD83D\uDC64 " + (user?.nickname ?: "nickname")
        lastSyncTextView.text = "\uD83D\uDD04 ${lastSync?.syncDate?.format(formatter)}"
        gamesAmount.text = "Posiadanych gier: $gameAmountValue"
        extensionAmount.text = "Dodatki: $extensionAmountValue"

        if(!user?.image.contentEquals(ByteArray(0))){
            val options = BitmapFactory.Options()
            options.inDither = false
            options.inPurgeable = true
            options.inInputShareable = true

            val bm = BitmapFactory.decodeByteArray(user?.image, 0, user?.image!!.size, options);
            avatarImageView.setImageBitmap(bm);
        }

        return homeView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}