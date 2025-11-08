package com.github.andreyasadchy.xtra.ui.settings

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R

class RankedStreamersAdapter(private val streamers: MutableList<Pair<Int, String>>) :
    RecyclerView.Adapter<RankedStreamersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: EditText = view.findViewById(R.id.rank)
        val streamerName: EditText = view.findViewById(R.id.streamerName)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranked_streamer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (rank, name) = streamers[position]
        holder.rank.setText(rank.toString())
        holder.streamerName.setText(name)

        holder.rank.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newRank = s.toString().toIntOrNull() ?: 0
                streamers[position] = Pair(newRank, streamers[position].second)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.streamerName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                streamers[position] = Pair(streamers[position].first, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.deleteButton.setOnClickListener {
            streamers.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = streamers.size

    fun addEmptySlot() {
        streamers.add(Pair(streamers.size + 1, ""))
        notifyItemInserted(streamers.size - 1)
    }
}
