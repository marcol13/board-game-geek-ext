package com.example.boardgamegeekext

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val settingsView : View = inflater.inflate(R.layout.fragment_settings, container, false)

        val dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        val user = dbHandler.selectUserInfo()

        val disabledEditTextNick : EditText = settingsView.findViewById(R.id.editDisabledNick)
//        disabledEditTextNick.text = (user?.nickname ?: "nickname") as Editable?
        disabledEditTextNick.setText((user?.nickname ?: "nickname"), TextView.BufferType.EDITABLE);
        disabledEditTextNick.isEnabled = false

        val disabledEditTextStartDate : EditText = settingsView.findViewById(R.id.editDisabledStartDate)
        disabledEditTextStartDate.isEnabled = false

        val disabledEditTextEndDate : EditText = settingsView.findViewById(R.id.editDisabledEndDate)
        disabledEditTextEndDate.isEnabled = false

        // Inflate the layout for this fragment
        return settingsView
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