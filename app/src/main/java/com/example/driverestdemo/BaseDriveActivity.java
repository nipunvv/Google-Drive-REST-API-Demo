package com.example.driverestdemo;

import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseDriveActivity extends AppCompatActivity {

    private static final String TAG = "BaseDriveActivity";

    //scopes
    public static final Scope SCOPE_FILE = new Scope("https://www.googleapis.com/auth/drive.file");
    public static final Scope SCOPE_APPFOLDER = new Scope("https://www.googleapis.com/auth/drive.appdata");
    public static final Scope SCOPE_DRIVE = new Scope("https://www.googleapis.com/auth/drive");

    // Request code for Google Sign-in/
    protected static final int REQUEST_CODE_SIGN_IN = 1;

    private GoogleAccountCredential mCredential;

    @Override
    protected void onStart() {
        super.onStart();
//        signIn();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                // required and is fatal. For apps where sign-in is optional, handle
                // appropriately
                Log.e(TAG, "Sign-in failed.");
                return;
            }
            System.out.println("check this"+data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            Task<GoogleSignInAccount> getAccountTask =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            if (getAccountTask.isSuccessful()) {
                initializeDriveClient(getAccountTask.getResult());
            } else {
                Log.e(TAG, "Sign-in failed.");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Starts the sign-in process and initializes the Drive client
    protected void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(SCOPE_FILE);
        requiredScopes.add(SCOPE_APPFOLDER);
        requiredScopes.add(SCOPE_DRIVE);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(SCOPE_FILE, SCOPE_APPFOLDER, SCOPE_DRIVE)
                            .requestEmail()
                            .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(final GoogleSignInAccount signInAccount) {
        mCredential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(SCOPE_FILE.getScopeUri()));
        mCredential.setSelectedAccount(signInAccount.getAccount());
        onDriveClientReady(signInAccount.getDisplayName(), signInAccount.getEmail(), signInAccount.getPhotoUrl());
    }

    public GoogleAccountCredential getCredential() {
        return mCredential;
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected abstract void onDriveClientReady(final String displayName, final String email, final Uri avatar);
}
