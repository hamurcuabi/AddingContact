package com.emrhmrc.addcontactlist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.emrhmrc.addcontactlist.api.ApiClient;
import com.emrhmrc.addcontactlist.api.JsonApi;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.emrhmrc.sweetdialoglib.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button btnAddContact;
    private Button btnGetList;
    private Button btnDelete;
    private Button btnWhatsAppContact;
    private Button btnPostContact;
    private TextView txtCount;
    private TextView txtApi;
    private TextView txtWhatsappCount;
    private EditText edtStart;
    private EditText edtEnd;
    private JsonApi jsonApi;
    private SweetAlertDialog dialog;
    private List<Number> numberList;
    private int start = -1;
    private int end = -1;
    private int totalCount;
    private ArrayList<WhatsAppNumber> whatsAppNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnGetList = findViewById(R.id.btnGetList);
        btnDelete = findViewById(R.id.btnDelete);
        btnWhatsAppContact = findViewById(R.id.btnWhatsAppContact);
        btnPostContact = findViewById(R.id.btnPostContact);
        txtCount = findViewById(R.id.txtCount);
        txtApi = findViewById(R.id.txtApi);
        txtWhatsappCount = findViewById(R.id.txtWhatsappCount);
        edtStart = findViewById(R.id.edtStart);
        edtEnd = findViewById(R.id.edtEnd);
        btnAddContact.setOnClickListener(this);
        btnGetList.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnWhatsAppContact.setOnClickListener(this);
        btnPostContact.setOnClickListener(this);
        jsonApi = ApiClient.getClient().create(JsonApi.class);
        getCount();
        showAllWhatsappContacts();
//
    }

    public void addContact(String phone, String name) {
        ContentValues values = new ContentValues();
        values.put(Contacts.People.NUMBER, phone);
        values.put(Contacts.People.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
        values.put(Contacts.People.LABEL, name);
        values.put(Contacts.People.NAME, name);
        Uri dataUri = getContentResolver().insert(Contacts.People.CONTENT_URI, values);
        Uri updateUri = Uri.withAppendedPath(dataUri, Contacts.People.Phones.CONTENT_DIRECTORY);
        values.clear();
        values.put(Contacts.People.Phones.TYPE, Contacts.People.TYPE_MOBILE);
        values.put(Contacts.People.NUMBER, phone);
        updateUri = getContentResolver().insert(updateUri, values);
        getCount();

    }

    private void addAll() {
        if (totalCount <= 500) {
            dialog = DialogCreater.loadingDialog(this, "Adding");
            for (Number item : numberList
            ) {
                addContact(item.getNumber1(), "Name-" + item.getNumber1());
            }

            dialog.dismissWithAnimation();
            getCount();
            getList();
        } else {

            DialogCreater.succesDialog(this, "Total Contact: " + totalCount);
        }

    }

    private void getCount() {
        Cursor cursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        totalCount = cursor.getCount();
        txtCount.setText("Total: " + totalCount);
    }

    private RangeModel validate() {

        if (TextUtils.isEmpty(edtStart.getText())) {
            edtStart.setError("Empty");
            return null;
        }
        if (TextUtils.isEmpty(edtEnd.getText())) {
            edtEnd.setError("Empty");
            return null;
        } else {
            if (start == -1 || end == -1) {
                start = Integer.parseInt(edtStart.getText().toString());
                end = Integer.parseInt(edtEnd.getText().toString());
            } else {

                start = end;
                end += 100;


            }
            return new RangeModel(start, end);
        }

    }

    private void getList() {

        RangeModel rangeModel = validate();
        if (rangeModel != null) {
            dialog = DialogCreater.loadingDialog(this);
            Call<List<Number>> call = jsonApi.getNumbersByRange(rangeModel);
            call.enqueue(new Callback<List<Number>>() {
                @Override
                public void onResponse(Call<List<Number>> call, Response<List<Number>> response) {
                    dialog.dismissWithAnimation();
                    if (response.isSuccessful()) {
                        numberList = response.body();
                        txtApi.setText("Api List count: " + numberList.size());
                        //DialogCreater.succesDialog(MainActivity.this, "Başarılı");
                        addAll();
                    } else {
                        DialogCreater.errorDialog(MainActivity.this, response.message());
                    }
                }

                @Override
                public void onFailure(Call<List<Number>> call, Throwable t) {
                    dialog.dismissWithAnimation();
                    DialogCreater.errorDialog(MainActivity.this, t.getLocalizedMessage());
                    Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                }
            });
        }

    }

    private void deleteAllContacts() {
        dialog = DialogCreater.loadingDialog(this, "Deleting");
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            contentResolver.delete(uri, null, null);
        }
        dialog.dismissWithAnimation();
        getCount();
    }

    private void showAllWhatsappContacts() {
        //This class provides applications access to the content model.
        ContentResolver cr = this.getContentResolver();
        //RowContacts for filter Account Types
        Cursor contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);

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
                                    new String[]{whatsappContactId}, null);

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
                                whatsAppNumbers.add(whatsAppNumber);
                                Log.d(TAG, " WhatsApp contact id  :  " + id);
                                Log.d(TAG, " WhatsApp contact name :  " + name);
                                Log.d(TAG, " WhatsApp contact number :  " + number);
                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }
            }
        }

        Log.d(TAG, " WhatsApp contact size :  " + myWhatsappContacts.size());
        txtWhatsappCount.setText("WhatsApp Total: " + myWhatsappContacts.size());
    }

    private void postWhatsappContact(WhatsAppNumber number) {
        dialog = DialogCreater.loadingDialog(this);
        Call<WhatsAppNumber> call = jsonApi.postWhatsAppNumber(number);
        call.enqueue(new Callback<WhatsAppNumber>() {
            @Override
            public void onResponse(Call<WhatsAppNumber> call, Response<WhatsAppNumber> response) {
                dialog.dismissWithAnimation();
                if (response.isSuccessful()) {
                    //do something
                } else {
                    DialogCreater.errorDialog(MainActivity.this, response.message());
                }
            }

            @Override
            public void onFailure(Call<WhatsAppNumber> call, Throwable t) {
                dialog.dismissWithAnimation();
                DialogCreater.errorDialog(MainActivity.this, t.getLocalizedMessage());
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });


    }

    private void postWhatsappContact(List<WhatsAppNumber> list) {
        dialog = DialogCreater.loadingDialog(this);
        Call<Void> call = jsonApi.postWhatsAppNumberList(list);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dialog.dismissWithAnimation();
                if (response.isSuccessful()) {
                    DialogCreater.succesDialog(MainActivity.this, "All contacts Posted");
                } else {
                    DialogCreater.errorDialog(MainActivity.this, response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                dialog.dismissWithAnimation();
                DialogCreater.errorDialog(MainActivity.this, t.getLocalizedMessage());
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnAddContact:
                addAll();
                break;
            case R.id.btnGetList:
                getList();
                break;
            case R.id.btnDelete:
                deleteAllContacts();
                break;
            case R.id.btnWhatsAppContact:
                showAllWhatsappContacts();
                break;
            case R.id.btnPostContact:
                postAll();
                break;
        }

    }

    private void postAll() {

        postWhatsappContact(whatsAppNumbers);
      /*  for (WhatsAppNumber item : whatsAppNumbers
        ) {
            postWhatsappContact(item);
        }*/
    }

}
