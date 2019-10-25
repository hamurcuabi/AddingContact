package com.emrhmrc.addcontactlist.activitys;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.emrhmrc.addcontactlist.CSVFile;
import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.api.ApiClient;
import com.emrhmrc.addcontactlist.api.JsonApi;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.addcontactlist.models.Number;
import com.emrhmrc.addcontactlist.models.RangeModel;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.emrhmrc.sweetdialoglib.SweetAlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_CHOOSE_FILE = 2019;
    private Button btnAddContact;
    private Button btnGetList;
    private Button btnDelete;
    private Button btnWhatsAppContact;
    private Button btnPostContact;
    private Button btnExportCsv;
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
    private int totalRepeat = 0;
    private File file;
    private ArrayList<WhatsAppNumber> whatsAppNumbers = new ArrayList<>();
    private ArrayList<WhatsAppNumber> whatsAppNumbersCSV = new ArrayList<>();
    private ArrayList<Contact> contactNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnGetList = findViewById(R.id.btnGetList);
        btnDelete = findViewById(R.id.btnDelete);
        btnWhatsAppContact = findViewById(R.id.btnWhatsAppContact);
        btnPostContact = findViewById(R.id.btnPostContact);
        btnExportCsv = findViewById(R.id.btnExportCsv);
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
        btnExportCsv.setOnClickListener(this);
        jsonApi = ApiClient.getClient().create(JsonApi.class);
        checkContacts();
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
        totalRepeat += 1;
        if (totalRepeat <= 2) {
            dialog = DialogCreater.loadingDialog(this, "Adding");
            for (Number item : numberList
            ) {
                addContact("+90535" + item.getNumber1(), "A-" + item.getNumber1());
                addContact("+90536" + item.getNumber1(), "B-" + item.getNumber1());
                addContact("+90537" + item.getNumber1(), "C-" + item.getNumber1());
                addContact("+90538" + item.getNumber1(), "D-" + item.getNumber1());
                addContact("+90539" + item.getNumber1(), "E-" + item.getNumber1());
            }

            dialog.dismissWithAnimation();
            getCount();
            showAllWhatsappContacts();
            postAll();
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
                end += 20;


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
                        retypingNumber(numberList);
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
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            contentResolver.delete(uri, null, null);
        }
        dialog.dismissWithAnimation();
        getCount();
    }

    private void deleteAllContactsNotWhatsApp() {
        dialog = DialogCreater.loadingDialog(this, "Deleting");
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);
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
                    deleteAllContactsNotWhatsApp();
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

    private void postAll() {
        if (whatsAppNumbers.size() > 0)
            postWhatsappContact(whatsAppNumbers);
    }

    private void retypingNumber(List<Number> list) {

        for (Number item : list
        ) {
            item.getNumber1().replace("+90535", "");
        }

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
            case R.id.btnExportCsv:
                exportContactCsv();
                break;
        }

    }

    @AfterPermissionGranted(123)
    private void checkContacts() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (EasyPermissions.hasPermissions(this, perms)) {
            getCount();
            showAllWhatsappContacts();
        } else {
            EasyPermissions.requestPermissions(this, "Permission for Contacts",
                    123, perms);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                file = new File(Objects.requireNonNull(Objects.requireNonNull(data.getData()).getPath()));
            }

        }
    }

    public void exportWhatsappCsv() {
        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Name,Number");
        for (WhatsAppNumber item : whatsAppNumbers
        ) {
            data.append("\n" + item.getName() + "," + item.getNumber());
        }

        try {
            //saving the file into device
            FileOutputStream out = openFileOutput("whatsappcontactlist.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "whatsappcontactlist.csv");
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

    public void exportContactCsv() {
        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Name,Number");
        for (Contact item : contactNumbers
        ) {
            data.append("\n" + item.getName() + "," + item.getNumber());
        }

        try {
            //saving the file into device
            FileOutputStream out = openFileOutput("whatsappcontactlist.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "whatsappcontactlist.csv");
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

    public void importCsv() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        CSVFile csvFile = new CSVFile(inputStream);
        List<String[]> cvsList = csvFile.read();

        for (String[] item : cvsList) {
            whatsAppNumbersCSV.add(new WhatsAppNumber(item[0], item[1]));
        }
    }

    private void selectCSVFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_FILE);
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
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                        contactNumbers.add(new Contact(name, phoneNo));
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }


}
