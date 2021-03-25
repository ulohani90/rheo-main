package com.rheotv.android.ui.activities.audioroom.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.rheotv.android.databinding.ListItemAudioRoomSuggestionBinding
import com.rheotv.android.ui.activities.audioroom.model.AudioRoom

class AudioRoomSuggestionAdapter(context: Context, resource: Int) : ArrayAdapter<AudioRoom>(context, resource), Filterable {
    var list: List<AudioRoom> = ArrayList()

    fun submitList(list: List<AudioRoom>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getCount() = list.size

    override fun getItem(position: Int) = list[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ListItemAudioRoomSuggestionBinding?
        if (convertView == null) {
            binding = ListItemAudioRoomSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.root.tag = binding
        } else {
            binding = convertView.tag as? ListItemAudioRoomSuggestionBinding
        }
        binding?.room = list[position]
        return binding?.root!!
    }

    /*override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    val f = list.map {
                        it.groupDetails?.name ?: it.groupDetails?.ownerDetails?.username
                    }
                    filterResults.values = f
                    filterResults.count = list.size
                    Log.i("AudioRoomSuggestion", "performFiltering: ${f.joinToString { "," }}")
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                *//*if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }*//*
            }
        }
    }*/
}