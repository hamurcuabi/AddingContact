package com.emrhmrc.addcontactlist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.databinding.ContactItemBinding;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.genericrecycler.adapters.BaseFilterAdapter;
import com.emrhmrc.genericrecycler.adapters.GenericAdapter;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;


public class ContactAdapter extends GenericAdapter<Contact,
        IOnItemClickListener<Contact>,
        ContactViewHolder> implements Filterable {


    public ContactAdapter(Context context, IOnItemClickListener listener, RelativeLayout emptyView) {
        super(context, listener, emptyView);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ContactItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.contact_item, parent, false);
        return new ContactViewHolder(binding);

    }


    @Override
    public int getItemViewType(int position) {
        final Contact item = getItem(position);
        return position;
    }

    @Override
    public Filter getFilter() {
        return new BaseFilterAdapter<Contact>(this, this.getItemsFilter());
    }


}