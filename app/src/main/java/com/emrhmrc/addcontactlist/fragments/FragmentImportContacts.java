package com.emrhmrc.addcontactlist.fragments;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.adapters.ContactAdapter;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.addcontactlist.utils.ContacListUtil;
import com.emrhmrc.genericrecycler.helpers.GRVHelper;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;
import com.emrhmrc.genericrecycler.interfaces.IOnSwipe;
import com.emrhmrc.sweetdialoglib.DialogButtonListener;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.emrhmrc.sweetdialoglib.SweetAlertDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentImportContacts extends DialogFragment implements IOnItemClickListener, IOnSwipe,
        DialogButtonListener {

    private static final String TAG = "FragmentContacts";
    @BindView(R.id.generic_recylerview)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    RelativeLayout emptyView;
    @BindView(R.id.searchView)
    SearchView searchView;
    ContactAdapter contactAdapter;
    @BindView(R.id.fab_add)
    FloatingActionButton fab_add;
    private boolean checked = false;
    private boolean rotate = false;
    private InterstitialAd mInterstitialAd;
    private SweetAlertDialog dialog;

    public FragmentImportContacts() {
    }

    public static FragmentImportContacts newInstance() {
        FragmentImportContacts fragment = new FragmentImportContacts();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_contacts, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    private void setSearchView() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.getFilter().filter(newText);
                return false;
            }
        });

    }


    private void setAdapter() {
        contactAdapter = new ContactAdapter(getActivity(), this, emptyView);
        GRVHelper.setup(contactAdapter, recyclerView);

    }

    @OnClick(R.id.checkbox)
    public void checkBoxClicked() {
        checked = !checked;
        for (Contact item : ContacListUtil.importContacts) {
            item.setChecked(checked);

        }
        contactAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClicked(Object item, int positon) {
        Log.d(TAG, "onItemClicked:Position:" + positon);
    }

    @Override
    public void OnSwipe(Object item, int position, int direction) {
        Log.d(TAG, "OnSwipe:Position:" + position + " Direction:" + direction);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setAdapter();
        contactAdapter.setItems(ContacListUtil.importContacts);
        setSearchView();
        fab_add.setOnClickListener(view -> {
            showAdd();
            if (ContacListUtil.importContacts.size() == 0) {
                DialogCreater.warningDialog(getActivity(),
                        getResources().getString(R.string.no_item_found));
            } else {
                boolean selected = false;
                for (Contact item : ContacListUtil.importContacts
                ) {
                    if (item.isChecked()) {
                        selected = true;
                        break;
                    }
                }
                if (selected) {
                    DialogCreater.questionDialog(getActivity(), this,
                            getResources().getString(R.string.sure), 1);
                } else {
                    DialogCreater.warningDialog(getActivity(),
                            getResources().getString(R.string.no_item_selected_found));
                }
            }


        });
        addMob();
    }

    public void addContact(String phone, String name) {
        ContentValues values = new ContentValues();
        values.put(Contacts.People.NUMBER, phone);
        values.put(Contacts.People.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
        values.put(Contacts.People.LABEL, name);
        values.put(Contacts.People.NAME, name);
        Uri dataUri = getActivity().getContentResolver().insert(Contacts.People.CONTENT_URI, values);
        Uri updateUri = Uri.withAppendedPath(dataUri, Contacts.People.Phones.CONTENT_DIRECTORY);
        values.clear();
        values.put(Contacts.People.Phones.TYPE, Contacts.People.TYPE_MOBILE);
        values.put(Contacts.People.NUMBER, phone);
        updateUri = getActivity().getContentResolver().insert(updateUri, values);


    }

    private void addAll() {
        showAdd();
        if (ContacListUtil.importContacts != null && ContacListUtil.importContacts.size() > 0) {
            dialog = DialogCreater.loadingDialog(getActivity(),
                    getResources().getString(R.string.loading));
            for (Contact item : ContacListUtil.importContacts
            ) {
                addContact(item.getNumber(), item.getName());

            }
            dialog.dismissWithAnimation();
            dismiss();
        } else {
            DialogCreater.errorDialog(getActivity(), getResources().getString(R.string.no_item_selected_found));
        }


    }

    private void showAdd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    private void addMob() {
        MobileAds.initialize(getActivity(),
                "ca-app-pub-6791508794346575~9907474527");
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-6791508794346575/4482105069");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }


    @Override
    public void onConfirmButton(int id) {
        switch (id) {
            case 1:
                addAll();
                break;
        }
    }

    @Override
    public void onCancelButton(int id) {

    }


    @Override
    public void onResume() {
        super.onResume();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = 8 * width / 9;
        params.height = 4 * height / 9;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}