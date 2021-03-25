package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.data.network.models.general.GameDetails
import com.rheotv.android.databinding.ListItemGameSelectionBinding
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.BindingUtils
import java.util.*
import kotlin.collections.HashMap

class GameSelectionAdapter : RecyclerView.Adapter<BaseViewHolder?>() {
    var selectedGame: MutableMap<String, GameDetails?> = HashMap()
    var mListener: GameInteractionListener? = null
    private var list: List<GameDetails> = ArrayList()

    fun submitList(list: List<GameDetails>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return GameViewHolder(ListItemGameSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class GameViewHolder constructor(val binding: ListItemGameSelectionBinding) : BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            val game = list[position]
            BindingUtils.setImageUrlUsingCache(binding.gameThumbnailImageView, game.thumbnail, false)
            binding.game = game
            binding.root.isSelected = selectedGame.containsKey(game.id)

            binding.root.setOnClickListener { v ->
                if (selectedGame.containsKey(game.id)) {
                    selectedGame.remove(game.id)
                } else {
                    selectedGame[game.id] = game
                }
                binding.root.isSelected = !binding.root.isSelected
                if (mListener != null) mListener?.onGameItemClicked(selectedGame)
            }
        }

    }

    interface GameInteractionListener {
        fun onGameItemClicked(selectedGame: Map<String, GameDetails?>)
    }
}