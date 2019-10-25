package com.emrhmrc.addcontactlist.activitys;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.addcontactlist.utils.ContacListUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashScreen extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "HomeActivity";
    @BindView(R.id.txt_title)
    TextView txt_title;
    @BindView(R.id.txt_start)
    TextView txt_start;
    @BindView(R.id.txt_subtitle)
    ImageView img_logo;
    @BindView(R.id.img_splash)
    ImageView img_splash;
    @BindView(R.id.prog)
    ProgressBar progressBar;
    private Animation small_to_big, flip;
    private AnimationDrawable animationDrawable;
    private List<Contact> contactList = new ArrayList<>();
    private ArrayList<WhatsAppNumber> whatsAppNumbers = new ArrayList<>();
    private Thread timerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        init();
        initAnim();
        setGradiendt();
        img_logo.setAnimation(flip);
        timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    goHome();

                }
            }
        };

        new GetContactListTask().execute();

    }

    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

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
                        String type = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.RawContacts.ACCOUNT_TYPE));
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

    private void goHome() {
        Intent intent = new Intent(SplashScreen.this, Dashboard.class);
        startActivity(intent);
        overridePendingTransition(0, android.R.anim.slide_out_right);
    }

    private void initAnim() {
        txt_title.setTranslationX(600);
        txt_start.setTranslationX(800);
        txt_title.setAlpha(0);
        txt_start.setAlpha(0);
        txt_title.animate().translationX(0).alpha(1).setDuration(900).setStartDelay(800).start();
        txt_start.animate().translationX(0).alpha(1).setDuration(900).setStartDelay(1000).start();
    }

    private void init() {
        flip = AnimationUtils.loadAnimation(this, R.anim.dialog_enter_from_top);
        small_to_big = AnimationUtils.loadAnimation(this, R.anim.small_to_big);
        img_splash.setAnimation(small_to_big);
    }

    private void setGradiendt() {
        animationDrawable = (AnimationDrawable) img_splash.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            animationDrawable.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @AfterPermissionGranted(123)
    private void checkContacts() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (EasyPermissions.hasPermissions(this, perms)) {
            getAllWhatsappContacts();
            getContactList();

        }
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

    private class GetContactListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            checkContacts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            timerThread.start();
        }
    }
}