package com.project.meetu.arcityguide;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener  {


    SignInButton signinbutton;
    Button signoutbutton;
    TextView statusTextView;
    GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    CallbackManager mcallbackManager;
    LoginButton fbloginButton;

    private static final String LOGTAG="Sign-in Activity";
    private static final int RC_Sign_in=9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId("157794314726687");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        //Google Sign-in
        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        statusTextView=(TextView) findViewById(R.id.textview1);
        signoutbutton=(Button) findViewById(R.id.SignOutButton);
        signoutbutton.setOnClickListener(this);

        signinbutton=(SignInButton) findViewById(R.id.GoogleSignInButton);
        signinbutton.setOnClickListener(this);
        //firebase authentication with google(getting firebase credentials and logging in
        //Facebook Log-In
        mcallbackManager=CallbackManager.Factory.create();
        fbloginButton=(LoginButton) findViewById(R.id.FacebookSignInButton);
        fbloginButton.setReadPermissions("email","public_profile");
        fbloginButton.registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOGTAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(LOGTAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LOGTAG, "facebook:onError", error);
            }
        });
        //Authorization listener and other firebase activities
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGTAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(LOGTAG, "onAuthStateChanged:signed_out");
                }
                // ...

            }
        };

    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOGTAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOGTAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOGTAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.GoogleSignInButton: signin();
                break;

            case R.id.SignOutButton:
                signout();
                break;
        }
    }

    private void signin(){
        Intent signInIntent= Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_Sign_in);
    }
    @Override
    public void onActivityResult(int RequestCode,int resultcode,Intent data){
        super.onActivityResult(RequestCode,resultcode,data);
        if(RequestCode==RC_Sign_in){
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            return;
        }
        mcallbackManager.onActivityResult(RequestCode, resultcode, data);

    }

    private void handleSignInResult(GoogleSignInResult result){
        Log.d(LOGTAG,"Handle Sign In Result"+ result.isSuccess());
        if(result.isSuccess()){
            GoogleSignInAccount acct= result.getSignInAccount();
            statusTextView.setText("Hello "+ acct.getDisplayName());
            firebaseAuthWithGoogle(acct);
            //Move to Input Activity
            movetoInputActivity();
        }
        else{
            statusTextView.setText("Sign-In Failed!");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.d(LOGTAG,"Connection Failed:"+connectionResult);
    }

    private void signout(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                statusTextView.setText("Signed Out");
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(LOGTAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOGTAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOGTAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void movetoInputActivity(){
        Intent I1= new Intent(this,InputActivity.class);
        startActivity(I1);
    }
}
