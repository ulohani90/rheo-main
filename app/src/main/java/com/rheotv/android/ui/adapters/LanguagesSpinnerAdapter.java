package com.rheotv.android.ui.adapters;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;

import java.util.ArrayList;
import java.util.List;

public class LanguagesSpinnerAdapter extends ArrayAdapter<LanguageObject> {
    Context mContext;
    LayoutInflater mInflater;
    int mResource;
    List<LanguageObject> items;

    List<String> selectedIds = new ArrayList<>();

    OnLanguageChangedListener mListener;

    public LanguagesSpinnerAdapter(@NonNull Context context, int resource,
                                   @NonNull List<LanguageObject> objects) {
        super(context, resource, 0, objects);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
        createSelectedIdsList();
    }

    private void createSelectedIdsList() {
        for (LanguageObject obj : items) {
            if (obj.isSelected()) {
                selectedIds.add(obj.getId());
            }
        }
    }


    public void setmListener(OnLanguageChangedListener mListener) {
        this.mListener = mListener;
    }

    public List<String> getSelectedIds() {
        return selectedIds;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        final View view = mInflater.inflate(mResource, parent, false);

        TextView languageText = (TextView) view.findViewById(R.id.language_text);
        TextView languageTick = (TextView) view.findViewById(R.id.language_tick);
        languageText.setText(items.get(position).getDisplayName());
        if (selectedIds.contains(items.get(position).getId())) {
            languageTick.setVisibility(View.VISIBLE);
        } else {
            languageTick.setVisibility(View.INVISIBLE);
        }
        LinearLayout itemParent = (LinearLayout) view.findViewById(R.id.item_parent);
        itemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (languageTick.getVisibility() == View.VISIBLE) {
                    selectedIds.remove(items.get(position).getId());
                    languageTick.setVisibility(View.GONE);

                } else {
                    selectedIds.add(items.get(position).getId());
                    languageTick.setVisibility(View.VISIBLE);
                }

                mListener.onLanguageChanged(getSelectedLanguages());
            }
        });

        return view;
    }

    private List<String> getSelectedLanguages() {
        List<String> selectedLanguages = new ArrayList<>();
        for (LanguageObject item : items) {
            if (selectedIds.contains(item.getId())) {
                selectedLanguages.add(item.getDisplayName());
            }
        }
        return selectedLanguages;
    }

    public interface OnLanguageChangedListener {
        void onLanguageChanged(List<String> selectedLanguages);
    }
}
