package com.example.boardgamegeekext

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import androidx.fragment.app.Fragment


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {
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

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dbHandler = DatabaseHelper(requireContext(), null, null, 1)
        val games = dbHandler.selectGamesList()

        val listView : View = inflater.inflate(R.layout.fragment_list, container, false)

        val scrollContainer : ScrollView = listView.findViewById(R.id.scroll_container)

        val ll = LinearLayout(requireActivity())
        ll.orientation = LinearLayout.VERTICAL
        ll.setId(12345);

        val spinner: Spinner = listView.findViewById(R.id.sort_spinner)
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.sort_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        for(i in 0 until games.size){
            var el = games.get(i)
            Log.d("MOŻE TU DZIALA", el.isExtension.toString())
            var itemList = ListItemFragment.newInstance((i+1).toString() + ".", el.title, el.publishedDate, el.isExtension, el.thumbnail, el.rank)

            fragmentManager?.beginTransaction()?.add(ll.id, itemList, "tag")?.commit()
        }

        scrollContainer.addView(ll)

        return listView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}