package sergicarolbosch.needmorecookies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Forked from https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java
 *
 * This class is responsible for the Login logic of the app. It stores the email and the name of the user.
 * */



public class Login extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LogInActivity";
    private static final int RC_SIGN_IN = 9001;

    private User_Info usr_inf;
    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**[START User Interface]**/
        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        /**[END User Interface]**/

        /**[START User Info class]**/
        usr_inf = User_Info.getInstance();
        /**[END User Info class]**/

        /**[START configure_signin]**/
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        /**[END configure_signin]**/

        /**[START build_client]**/
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END build_client]**/

        /**[START customize_button]**/
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        assert signInButton != null;
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        /**[END customize_button]**/

        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (!is_network_available()){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.offline_alert);
            alert.setMessage(R.string.offline_question);
            alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    usr_inf.setOffline_mode(true);
                    launch_next_activity();
                }
            });
            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }

    }

    // Called after onCreate, checks if there is internet connection, if not asks the user if
    // he wants to use Offline Mode
    @Override
    public void onStart() {
        super.onStart();
        if (!is_network_available()){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.offline_alert);
            alert.setMessage(R.string.offline_question);
            alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    usr_inf.setOffline_mode(true);
                    launch_next_activity();
                }
            });
            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }else {
            //Check if the user has signed in before
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    // Checks if the sign in is successful
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!is_network_available()){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.offline_alert);
            alert.setMessage(R.string.offline_question);
            alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    usr_inf.setOffline_mode(true);
                    launch_next_activity();
                }
            });
            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    // Handles the signin result
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        // If there is no connection ask the user if he wants to enter offline mode
        if (!is_network_available()){
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.offline_alert);
            alert.setMessage(R.string.offline_question);
            alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    usr_inf.setOffline_mode(true);
                    launch_next_activity();
                }
            });
            alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
        }
        // If it was successful go the next activity
        if (result.isSuccess()) {
            // acct stores data from the user (email,name...)
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.v(TAG, "" + acct.getDisplayName() + "" + acct.getEmail());
            // Set email and name for user
            usr_inf.setOffline_mode(false);
            usr_inf.setEmail(acct.getEmail());
            usr_inf.setName(acct.getDisplayName());
            launch_next_activity();
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    // Start activity to sign in
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // An unresolvable error has occurred and Google APIs (including Sign-In) will not
    // be available.
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    // Shows progress spinner
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    // Hides progress spinner
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    // Updates User Interface by setting the sign in button visible or invisible
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            mStatusTextView.setText(R.string.Status_SignedOut);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    //Check if network available
    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Checks which button has been clicked
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            default:
                Log.e(TAG,"Unknown button:Check other buttons beside sign in");
                break;
        }
    }

    // Start next activity
    private void launch_next_activity(){
        // Start new activity
        final Intent intent = new Intent(Login.this,MainActivity.class);
        Log.v(TAG,"Launching next activity");
        // Start next activity
        Log.v(TAG,is_network_available()+"");
        startActivity(intent);
        // Finish current activity
        finish();

    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Override onConfigurationChanged method to configure the orientation of the screen
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}