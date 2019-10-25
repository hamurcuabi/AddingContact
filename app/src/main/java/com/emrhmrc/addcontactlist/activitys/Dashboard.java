package com.emrhmrc.addcontactlist.activitys;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.adapters.SectionsPagerAdapter;
import com.emrhmrc.addcontactlist.fragments.FragmentContacts;
import com.emrhmrc.addcontactlist.fragments.FragmentWhatsappContacts;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.addcontactlist.utils.ContacListUtil;
import com.emrhmrc.addcontactlist.utils.ViewAnimation;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.emrhmrc.sweetdialoglib.SweetAlertDialog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class Dashboard extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "Dashboard";
    private static final int PERMISSION_CONTACT = 12345;
    @BindView(R.id.piechart)
    PieChart pieChart;
    @BindView(R.id.view_pager)
    ViewPager view_pager;
    @BindView(R.id.tab_layout)
    TabLayout tab_layout;
    @BindView(R.id.txtWhatsappCount)
    TextView txtWhatsappCount;
    @BindView(R.id.txtContacCount)
    TextView txtContacCount;
    @BindView(R.id.tab1)
    LinearLayout tab1;
    @BindView(R.id.tab2)
    LinearLayout tab2;
    @BindView(R.id.fab_reload)
    FloatingActionButton fab_reload;
    private boolean rotate = false;
    private SectionsPagerAdapter viewPagerAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private ArrayList<WhatsAppNumber> whatsAppNumbers = new ArrayList<>();
    private SweetAlertDialog dialog;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_collapse);
        ButterKnife.bind(this);
        checkContacts();
        MobileAds.initialize(this,
                "ca-app-pub-6791508794346575~9907474527");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6791508794346575/4482105069");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }


        fab_reload.setOnClickListener(view -> {
            rotate = ViewAnimation.rotateFab(fab_reload, !rotate);
            checkContacts();
        });

    }

    private void initChart(int whatsapp, int contact) {

        ArrayList<Entry> contactList = new ArrayList();
        ArrayList<String> pieEntryLabels = new ArrayList<>();
        contactList.add(new BarEntry(whatsapp, 0));
        contactList.add(new BarEntry(contact, 1));
        PieDataSet dataSet = new PieDataSet(contactList,
                "");
        pieEntryLabels.add(getResources().getString(R.string.whatsapp));
        pieEntryLabels.add(getResources().getString(R.string.phone));
        dataSet.setValueTextColor(getResources().getColor(R.color.white));
        PieData data = new PieData(pieEntryLabels, dataSet);
        data.setValueTextColor(getResources().getColor(R.color.amber_50));
        data.setValueTextSize(10f);
        pieChart.setData(data);
        dataSet.setColors(ColorTemplate.createColors(new int[]{getResources().getColor(R.color.colorAccent)
                , getResources().getColor(R.color.colorPrimaryDark)
        }));
        pieChart.animateXY(3000, 3000);
        pieChart.setDescriptionColor(getResources().getColor(R.color.amber_50));
        pieChart.setCenterTextColor(getResources().getColor(R.color.blue_grey_800));
        pieChart.setDescription("");
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTransparentCircleRadius(30f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText(rate());
        pieChart.setHoleRadius(30f);


    }

    private String rate() {

        if (contactList.size() > 0 && whatsAppNumbers.size() > 0) {
            int r = (whatsAppNumbers.size() * 100 / contactList.size());
            return "%" + r;
        } else
            return "";
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // index 0
        viewPagerAdapter.addFragment(new FragmentWhatsappContacts(whatsAppNumbers),
                "WhatsApp");
        viewPagerAdapter.addFragment(new FragmentContacts(contactList), "Contacts");
        // index 1
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void initComponent() {
        setupViewPager(view_pager);
        tab_layout.setupWithViewPager(view_pager);
        tab_layout.getTabAt(0).setIcon(R.drawable.ic_apps);
        tab_layout.getTabAt(1).setIcon(R.drawable.ic_person);
        // set icon color pre-selected
        tab_layout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimaryDark),
                PorterDuff.Mode.SRC_IN);
        tab_layout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_IN);


        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        tab1.setBackgroundColor(getResources().getColor(R.color.teal_700));
                        break;
                    case 1:
                        tab2.setBackgroundColor(getResources().getColor(R.color.teal_700));
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tab1.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
                        break;
                    case 1:
                        tab2.setBackgroundColor(getResources().getColor(R.color.colorAccentDark));
                        break;
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getContactList() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact contact = new Contact();
                        contact.setName(name);
                        contact.setNumber(phoneNo);
                        contact.setFilterName(name);
                        contactList.add(contact);
                    }
                    pCur.close();
                }
            }
            ContacListUtil.contactList = contactList;
        }
        if (cur != null) {
            cur.close();
        }

    }

    private void getAllWhatsappContacts() {

        //This class provides applications access to the content model.
        ContentResolver cr = getContentResolver();
        //RowContacts for filter Account Types
        Cursor contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

//ArrayList for Store Whatsapp Contact
        ArrayList<String> myWhatsappContacts = new ArrayList<>();

        if (contactCursor != null) {
            if (contactCursor.getCount() > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        String whatsappContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));

                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            Cursor whatsAppContactCursor = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{whatsappContactId}, "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst();
                                String id = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                whatsAppContactCursor.close();

                                //Add Number to ArrayList
                                myWhatsappContacts.add(number);
                                WhatsAppNumber whatsAppNumber = new WhatsAppNumber();
                                whatsAppNumber.setWhatsAppId(Integer.parseInt(id));
                                whatsAppNumber.setName(name);
                                whatsAppNumber.setNumber(number);
                                whatsAppNumber.setFilterName(name);
                                whatsAppNumbers.add(whatsAppNumber);

                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }

            }

            ContacListUtil.whatsAppNumbers = whatsAppNumbers;
        }


    }

    public void tab1click(View view) {
        view_pager.setCurrentItem(0);
    }

    public void tab2click(View view) {
        view_pager.setCurrentItem(1);
    }

    @AfterPermissionGranted(PERMISSION_CONTACT)
    private void checkContacts() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (ContacListUtil.whatsAppNumbers == null || ContacListUtil.contactList == null) {
                new GetContactListTask().execute();
            } else {
                whatsAppNumbers = ContacListUtil.whatsAppNumbers;
                contactList = ContacListUtil.contactList;
                initChart(whatsAppNumbers.size(), contactList.size());
                initComponent();
                setTexts();

            }

        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permission),
                    PERMISSION_CONTACT, perms);
        }
    }

    private void setTexts() {
        txtContacCount.setText(String.valueOf(contactList.size()));
        txtWhatsappCount.setText(String.valueOf(whatsAppNumbers.size()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onBackPressed() {

        switch (view_pager.getCurrentItem()) {
            case 1:
                view_pager.setCurrentItem(0);
                break;
            default:
                super.onBackPressed();
                break;

        }


    }

    private class GetContactListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            dialog = DialogCreater.loadingDialog(Dashboard.this);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            getAllWhatsappContacts();
            getContactList();
            whatsAppNumbers = ContacListUtil.whatsAppNumbers;
            contactList = ContacListUtil.contactList;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initChart(whatsAppNumbers.size(), contactList.size());
            initComponent();
            setTexts();
            dialog.dismissWithAnimation();

        }
    }
}
