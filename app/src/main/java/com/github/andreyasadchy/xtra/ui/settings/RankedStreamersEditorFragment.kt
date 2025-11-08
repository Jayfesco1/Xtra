package com.github.andreyasadchy.xtra.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs

class RankedStreamersEditorFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankedStreamersAdapter
    private val streamers = mutableListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranked_streamers_editor, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadStreamers()

        adapter = RankedStreamersAdapter(streamers)
        recyclerView.adapter = adapter

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ranked_streamers_editor, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                adapter.addEmptySlot()
                true
            }
            R.id.action_save -> {
                saveStreamers()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadStreamers() {
        val prefs = requireActivity().prefs()
        val savedStreamers = prefs.getString(C.RANKED_STREAMERS_LIST, "")
        if (!savedStreamers.isNullOrEmpty()) {
            streamers.addAll(savedStreamers.split("\n").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    parts[0].toIntOrNull()?.let { rank ->
                        Pair(rank, parts[1])
                    }
                } else {
                    null
                }
            })
        }
        streamers.add(Pair(streamers.size + 1, "")) // Add empty slot
    }

    private fun saveStreamers() {
        val prefs = requireActivity().prefs()
        val editor = prefs.edit()
        val streamersToSave = streamers.filter { it.second.isNotEmpty() }
        val savedStreamers = streamersToSave.joinToString("\n") { "${it.first}:${it.second}" }
        editor.putString(C.RANKED_STREAMERS_LIST, savedStreamers)
        editor.apply()
    }
}
