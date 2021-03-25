package com.rheotv.android.ui.activities.gamify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.VoucherItem;
import com.rheotv.android.databinding.ListItemVoucherBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class VoucherCodeAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private ArrayList<VoucherItem> items;

    public VoucherCodeAdapter(ArrayList<VoucherItem> items) {
        this.items = items;
    }

    public void addVouchers(ArrayList<VoucherItem> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemVoucherBinding binding = ListItemVoucherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VoucherCodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    class VoucherCodeViewHolder extends BaseViewHolder {
        ListItemVoucherBinding binding;

        public VoucherCodeViewHolder(ListItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            String code = items.get(position).getCodes();
            String sku = items.get(position).getSku();
            binding.setCode(code);
            binding.getRoot().setOnClickListener(view -> copyToClipboard(sku, code, view.getContext()));
        }
    }

    private void copyToClipboard(String label, String text, Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, context.getString(R.string.voucher_clipboard_message), Toast.LENGTH_SHORT).show();
    }
}
