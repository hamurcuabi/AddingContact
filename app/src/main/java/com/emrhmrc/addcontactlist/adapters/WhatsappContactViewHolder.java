package com.emrhmrc.addcontactlist.adapters;

import androidx.annotation.Nullable;

import com.emrhmrc.addcontactlist.databinding.WhatsappItemBinding;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.genericrecycler.adapters.BaseViewHolder;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;

import butterknife.ButterKnife;

public class WhatsappContactViewHolder extends BaseViewHolder<WhatsAppNumber,
        IOnItemClickListener<WhatsAppNumber>> {

    private WhatsappItemBinding binding;

    public WhatsappContactViewHolder(WhatsappItemBinding binding) {
        super(binding.getRoot());
        ButterKnife.bind(this, binding.getRoot());
        this.binding = binding;

    }


    @Override
    public void onBind(final WhatsAppNumber item, final @Nullable
            IOnItemClickListener<WhatsAppNumber> listener) {
        binding.setItem(item);
        binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            item.setChecked(b);
        });


    }


}
