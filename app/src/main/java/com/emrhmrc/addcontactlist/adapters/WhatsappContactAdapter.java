package com.emrhmrc.addcontactlist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.databinding.WhatsappItemBinding;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.genericrecycler.adapters.BaseFilterAdapter;
import com.emrhmrc.genericrecycler.adapters.GenericAdapter;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;


public class WhatsappContactAdapter extends GenericAdapter<WhatsAppNumber,
        IOnItemClickListener<WhatsAppNumber>,
        WhatsappContactViewHolder> implements Filterable {


    public WhatsappContactAdapter(Context context, IOnItemClickListener listener, RelativeLayout emptyView) {
        super(context, listener, emptyView);
    }

    @Override
    public WhatsappContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        WhatsappItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.whatsapp_item, parent, false);
        return new WhatsappContactViewHolder(binding);

    }


    @Override
    public int getItemViewType(int position) {
        final WhatsAppNumber item = getItem(position);
        return position;
    }

    @Override
    public Filter getFilter() {
        return new BaseFilterAdapter<WhatsAppNumber>(this, this.getItemsFilter());
    }


}