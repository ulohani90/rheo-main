package com.rheotv.android.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.databinding.ItemAddModeratorLayoutBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddModeratorsRVAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<String> moderatorEmails = new ArrayList<>();

    public AddModeratorsRVAdapter(String moderators) {
        if (moderators != null) {
            moderatorEmails.addAll(Arrays.asList(moderators.split(",")));
        }
    }

    public void incrementItemCount() {
        moderatorEmails.add("");
        notifyItemInserted(moderatorEmails.size() - 1);
    }


    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddModeratorLayoutBinding binding = ItemAddModeratorLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ModeratorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return moderatorEmails.isEmpty() ? 1 : moderatorEmails.size();
    }

    public class ModeratorViewHolder extends BaseViewHolder {

        ItemAddModeratorLayoutBinding moderatorLayoutBinding;

        public ModeratorViewHolder(ItemAddModeratorLayoutBinding binding) {
            super(binding.getRoot());
            moderatorLayoutBinding = binding;
        }


        @Override
        public void onBind(int position) {
            moderatorLayoutBinding.moderatorTitleTv.setText("Moderator " + (position + 1));
            if (position > 0) {
                moderatorLayoutBinding.removeModerator.setVisibility(View.VISIBLE);
            } else {
                moderatorLayoutBinding.removeModerator.setVisibility(View.GONE);
            }
            EditText firstnameET = moderatorLayoutBinding.firstNameEt;
            firstnameET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        if (editable != null && editable.toString().length() > 0) {
                            if (moderatorEmails.size() > position)
                                moderatorEmails.remove(position);
                            moderatorEmails.add(position, editable.toString());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (moderatorEmails.size() > position) {
                firstnameET.setText(moderatorEmails.get(position));
            } else {
                firstnameET.setText("");
            }
            moderatorLayoutBinding.removeModerator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeCurrentPosition(position);
                }
            });
        }
    }

    private void removeCurrentPosition(int position) {
        moderatorEmails.remove(position);
        notifyDataSetChanged();
    }
}
