package com.emrhmrc.addcontactlist.adapters;

import androidx.annotation.Nullable;

import com.emrhmrc.addcontactlist.databinding.ContactItemBinding;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.genericrecycler.adapters.BaseViewHolder;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;

import butterknife.ButterKnife;

public class ContactViewHolder extends BaseViewHolder<Contact,
        IOnItemClickListener<Contact>> {

    private ContactItemBinding binding;

    public ContactViewHolder(ContactItemBinding binding) {
        super(binding.getRoot());
        ButterKnife.bind(this, binding.getRoot());
        this.binding = binding;

    }


    @Override
    public void onBind(final Contact item, final @Nullable
            IOnItemClickListener<Contact> listener) {
        binding.setItem(item);
        binding.checkbox.setOnCheckedChangeListener((compoundButton, b) -> {
            item.setChecked(b);
        });


    }


}
