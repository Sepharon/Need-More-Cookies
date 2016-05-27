package sersilinc.needmorecookies;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * This class shows information on how to contact with the developers of the project.
 */

public class ContactUs extends AppCompatActivity {

    /**
     * Override onCreate method.
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_contact);
        assert fab != null;
        //Send email
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mail_intent = new Intent(Intent.ACTION_SEND);
                mail_intent.setType("message/rfc822");
                mail_intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"webmaster@centrethailam.com"});
                // Body of mail
                Intent final_intent = Intent.createChooser(mail_intent,"Choose mail client");
                final_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Start Mail chooser
                startActivity(final_intent);
            }
        });

        //Set portrait orientation for phones and landscape for tablets.
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
     * @param newConfig New configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
