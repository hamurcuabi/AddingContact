package com.emrhmrc.addcontactlist.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.models.Contact;
import com.emrhmrc.addcontactlist.utils.ContacListUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    private static final int ACTIVITY_CHOOSE_FILE_CSV = 1232;
    public List<Contact> importContacts = new ArrayList<>();

    public void readContactData(Uri uri) {

        BufferedReader mBufferedReader = null;
        String line;

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = mBufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                // Read the data
                Contact sample = new Contact();
                // Setters
                sample.setName(tokens[0]);
                sample.setNumber(tokens[1]);

                // Adding object to a class
                importContacts.add(sample);

            }
            mBufferedReader.close();
            importContacts.remove(0);
            ContacListUtil.importContacts = importContacts;
            FragmentImportContacts fragmentImportContacts = new FragmentImportContacts();
            fragmentImportContacts.show(getActivity().getSupportFragmentManager(), "İmportContacts");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void selectCSVFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent,
                getResources().getString(R.string.open_csv)),
                ACTIVITY_CHOOSE_FILE_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CHOOSE_FILE_CSV) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                readContactData(uri);

            }
        }
    }

}
