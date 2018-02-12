package com.example.ollie.fitnessapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.auth.api.Auth;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private SignInButton mSignInbutton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClientReader;


    private static int RC_SIGN_IN = 9001;
    public String toast = "DEBUG";

    public static final String TAG = "BasicRecordingApi";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;


    ///////////////////////ON CREATE//////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSignInbutton = (SignInButton) findViewById(R.id.sign_in_button);

        //////////GOOGLE SIGN IN/////////////

        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Fitness.RECORDING_API).addApi(Fitness.HISTORY_API).addApi(Fitness.CONFIG_API).addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ)).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "API CONNECTION FAILED");
            }
        }).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                new historyAsyncTask().execute(mGoogleApiClient);
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).build();
        mGoogleApiClient.connect();

        mSignInbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn(); //call the signin method

                        break;
                }
            }
        });

        FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_ACTIVITY_SAMPLES).build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }


        try {
            updateUI(mGoogleSignInAccount);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleSignInAccount != null)
            try {
                updateUI(mGoogleSignInAccount);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_home:
                    setTitle("HOME");
                    homeFragment fragment = new homeFragment();
                    android.support.v4.app.FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.frameLayout, fragment, "homeFragment");
                    fragmentTransaction1.commit();
                    subscribe();



                    return true;

                case R.id.action_graph:
                    setTitle("GRAPH");
                    graphFragment fragment2 = new graphFragment();
                    android.support.v4.app.FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction2.replace(R.id.frameLayout, fragment2, "graphFragment");
                    fragmentTransaction2.commit();
                    return true;

            }
            return false;
        }
    };

    public void buttonSwitchVisibility(SignInButton s){
        if (s.getVisibility() == s.VISIBLE)
            s.setVisibility(s.INVISIBLE);
        else
            s.setVisibility(s.VISIBLE);
    }

    ////////////////////////////////////////////////////////////////
    private void listSubs(){
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .listSubscriptions(DataType.TYPE_ACTIVITY_SAMPLES)
                .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                    @Override
                    public void onSuccess(List<Subscription> subscriptions) {
                        for (Subscription sc : subscriptions) {
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
    }

   /* public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        if (GoogleSignIn.getLastSignedInAccount(this) != null) { //????
            Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .subscribe(DataType.TYPE_ACTIVITY_SAMPLES)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Successfully subscribed!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    });
            // [END subscribe_to_datatype]
        }
    }
    */


    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.w(TAG, "There was a problem subscribing.");
                        }
                    }
                });
    }



    //Override
    protected void onActivityResultFitness(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
                //sadasd
                //sadasd
            }
        }
    }

    /**
     * Fetches a list of all active subscriptions and log it. Since the logger for this sample
     * also prints to the screen, we can see what is happening in this way.
     */
    private void dumpSubscriptionsList() {
        // [START list_current_subscriptions]
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .listSubscriptions(DataType.TYPE_ACTIVITY_SAMPLES)
                .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                    @Override
                    public void onSuccess(List<Subscription> subscriptions) {
                        for (Subscription sc : subscriptions) {
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
        // [END list_current_subscriptions]
    }

    /**
     * Cancels the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    private void cancelSubscription() {
        final String dataTypeStr = DataType.TYPE_ACTIVITY_SAMPLES.toString();
        Log.i(TAG, "Unsubscribing from data type: " + dataTypeStr);

        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
        // [START unsubscribe_from_datatype]
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .unsubscribe(DataType.TYPE_ACTIVITY_SAMPLES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribed for data type: " + dataTypeStr);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Subscription not removed
                        Log.i(TAG, "Failed to unsubscribe for data type: " + dataTypeStr);
                    }
                });
        // [END unsubscribe_from_datatype]
    }

    ///////////////////////////////////////////////////////////





    /////////////////////GOOGLE SIGN IN////////////////////////////////////////////


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                handleSignInResult(task);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) throws ExecutionException, InterruptedException {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) throws ExecutionException, InterruptedException {
        String email = "";

        if (account != null) {
            email = account.getDisplayName();
            mTextMessage.setText(email);
        }
        else{
            Log.d(TAG, "UI UPDATE FAILED");
        }
    }

    //////////////////////////////////////////////////////////////////







}
