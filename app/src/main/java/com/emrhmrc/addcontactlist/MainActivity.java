package com.emrhmrc.addcontactlist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnAddContact;
    private TextView txtCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddContact = findViewById(R.id.btnAddContact);
        txtCount = findViewById(R.id.txtCount);
        btnAddContact.setOnClickListener(this);
        getCount();
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

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnAddContact:
                addContact("+5396335745", "Test");

                break;
        }

    }

    private void getCount() {
        Cursor cursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        int count = cursor.getCount();
        txtCount.setText("Toplam: " + count);
    }
}
