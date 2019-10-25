package com.emrhmrc.addcontactlist.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.emrhmrc.addcontactlist.R;
import com.emrhmrc.addcontactlist.databinding.ActivitySignupBinding;
import com.emrhmrc.addcontactlist.models.Member;
import com.emrhmrc.sweetdialoglib.DialogCreater;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int SIGN_IN = 100;
    @BindView(R.id.btnGoogleSignIn)
    SignInButton btnGoogleSignIn;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private GoogleApiClient googleApiClient;
    private Member member = new Member();
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_signup);
        binding.setMember(member);
        setGoogleSign();
        btnGoogleSignIn.setOnClickListener(v -> googleSocialLogin());

    }

    private void setGoogleSign() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();

    }

    public void googleSocialLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResultGoogleSignIn(result);
        }
    }

    private void handleResultGoogleSignIn(GoogleSignInResult result) {
        if (result.isSuccess()) {
            DialogCreater.succesDialog(this);
            GoogleSignInAccount account = result.getSignInAccount();
            String socialFirstName = account.getGivenName();
            String socialLastName = account.getFamilyName();
            String socialEmail = account.getEmail();
            String socialUserId = account.getId();
            String socialImageUrl = account.getPhotoUrl().toString();

        } else {
            DialogCreater.errorDialog(this, result.getStatus().getStatusMessage());
        }
    }

    public void googleLogout() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {

                if (status.isSuccess()) {
                    DialogCreater.succesDialog(SignupActivity.this, "Başarılı Çıkış");
                }
            }
        });
    }

    public void login(View view) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DialogCreater.errorDialog(this, connectionResult.getErrorMessage());
    }
}