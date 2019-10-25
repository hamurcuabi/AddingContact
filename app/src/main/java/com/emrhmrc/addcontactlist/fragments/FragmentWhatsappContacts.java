package com.emrhmrc.addcontactlist.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.adapters.WhatsappContactAdapter;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.addcontactlist.utils.ViewAnimation;
import com.emrhmrc.genericrecycler.helpers.GRVHelper;
import com.emrhmrc.genericrecycler.interfaces.IOnItemClickListener;
import com.emrhmrc.genericrecycler.interfaces.IOnSwipe;
import com.emrhmrc.sweetdialoglib.DialogButtonListener;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentWhatsappContacts extends BaseFragment implements IOnItemClickListener, IOnSwipe,
        DialogButtonListener {

    private static final String TAG = "FragmentContacts";
    @BindView(R.id.generic_recylerview)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    RelativeLayout emptyView;
    @BindView(R.id.searchView)
    SearchView searchView;
    WhatsappContactAdapter whatsappContactAdapter;
    @BindView(R.id.fab_csv_import)
    FloatingActionButton fab_csv_import;
    @BindView(R.id.fab_csv)
    FloatingActionButton fab_cvs;
    @BindView(R.id.fab_add)
    FloatingActionButton fab_add;
    @BindView(R.id.lyt_csv_import)
    View lyt_csv_import;
    @BindView(R.id.lyt_csv)
    View lyt_csv;
    private ArrayList<WhatsAppNumber> whatsAppNumbers = new ArrayList<>();
    private boolean checked = false;
    private boolean rotate = false;
    private InterstitialAd mInterstitialAd;

    public FragmentWhatsappContacts() {
    }

    public FragmentWhatsappContacts(ArrayList<WhatsAppNumber> whatsAppNumbers) {
        this.whatsAppNumbers = whatsAppNumbers;
    }

    public static FragmentWhatsappContacts newInstance() {
        FragmentWhatsappContacts fragment = new FragmentWhatsappContacts();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabs_contacts, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    private void setSearchView() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                whatsappContactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                whatsappContactAdapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_csv);
            ViewAnimation.showIn(lyt_csv_import);

        } else {
            ViewAnimation.showOut(lyt_csv);
            ViewAnimation.showOut(lyt_csv_import);
        }
    }

    private void setAdapter() {
        whatsappContactAdapter = new WhatsappContactAdapter(getActivity(), this, emptyView);
        GRVHelper.setup(whatsappContactAdapter, recyclerView);

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
        whatsappContactAdapter.setItems(whatsAppNumbers);
        setSearchView();
        ViewAnimation.initShowOut(lyt_csv_import);
        ViewAnimation.initShowOut(lyt_csv);

        fab_add.setOnClickListener(this::toggleFabMode);
        fab_csv_import.setOnClickListener(v -> {
            selectCSVFile();
        });

        fab_cvs.setOnClickListener(v -> {
            showAdd();
            if (whatsAppNumbers.size() == 0) {
                DialogCreater.warningDialog(getActivity(),
                        getResources().getString(R.string.no_item_found));
            } else {
                boolean selected = false;
                for (WhatsAppNumber item : whatsAppNumbers
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


    @OnClick(R.id.checkbox)
    public void checkBoxClicked() {
        checked = !checked;
        for (WhatsAppNumber item : whatsAppNumbers) {
            item.setChecked(checked);

        }
        whatsappContactAdapter.notifyDataSetChanged();
    }

    public void exportContactCsv(String name) {
        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Name,Number");
        for (WhatsAppNumber item : whatsAppNumbers
        ) {
            if (item.isChecked())
                data.append("\n" + item.getName() + "," + item.getNumber());
        }

        try {
            //saving the file into device
            FileOutputStream out = getActivity().openFileOutput(name,
                    Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getActivity();
            File filelocation = new File(getActivity().getFilesDir(), name);
            Uri path = FileProvider.getUriForFile(context, "com.emrhmrc.addcontactlist", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Save Contacts"));
        } catch (
                Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onConfirmButton(int id) {
        switch (id) {
            case 1:
                exportContactCsv("ContacterWhatsAppContactList.csv");
                break;

        }
    }

    @Override
    public void onCancelButton(int id) {

    }

    private void addMob() {
        MobileAds.initialize(getActivity(),
                "ca-app-pub-6791508794346575~9907474527");
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-6791508794346575/4482105069");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    private void showAdd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }
}