package com.emrhmrc.addcontactlist.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.Expose;

public class BaseObservableModel extends BaseObservable {
    @Expose
    private String filterName = "";
    @Expose
    private boolean checked;

    @Bindable
    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;


    }

    @Bindable
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
