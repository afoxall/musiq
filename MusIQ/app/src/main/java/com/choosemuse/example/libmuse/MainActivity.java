package com.choosemuse.example.libmuse;

/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2016
 */

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.AnnotationData;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.LibmuseVersion;
import com.choosemuse.libmuse.MessageType;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConfiguration;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileFactory;
import com.choosemuse.libmuse.MuseFileReader;
import com.choosemuse.libmuse.MuseFileWriter;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;
import com.choosemuse.libmuse.Result;
import com.choosemuse.libmuse.ResultLevel;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
/**
 * This example will illustrate how to connect to a Muse headband,
 * register for and receive EEG data and disconnect from the headband.
 * Saving EEG data to a .muse file is also covered.
 *
 * For instructions on how to pair your headband with your Android device
 * please see:
 * http://developer.choosemuse.com/hardware-firmware/bluetooth-connectivity/developer-sdk-bluetooth-connectivity-2
 *
 * Usage instructions:
 * 1. Pair your headband if necessary.
 * 2. Run this project.
 * 3. Turn on the Muse headband.
 * 4. Press "Refresh". It should display all paired Muses in the Spinner drop down at the
 *    top of the screen.  It may take a few seconds for the headband to be detected.
 * 5. Select the headband you want to connect to and press "Connect".
 * 6. You should see EEG and accelerometer data as well as connection status,
 *    version information and relative alpha values appear on the screen.
 * 7. You can pause/resume data transmission with the button at the bottom of the screen.
 * 8. To disconnect from the headband, press "Disconnect"
 */
public class MainActivity extends Activity implements View.OnClickListener {

    /**
     * Tag used for logging purposes.
     */
    private final String TAG = "TestLibMuseAndroid";


    /**
     * We will be updating the UI using a handler instead of in packet handlers because
     * packets come in at a very high frequency and it only makes sense to update the UI
     * at about 60fps. The update functions do some string allocation, so this reduces our memory
     * footprint and makes GC pauses less frequent/noticeable.
     */
    private final Handler handler = new Handler();

    /**
     * In the UI, the list of Muses you can connect to is displayed in a Spinner object for this example.
     * This spinner adapter contains the MAC addresses of all of the headbands we have discovered.
     */
    private ArrayAdapter<String> spinnerAdapter;

    /**
     * It is possible to pause the data transmission from the headband.  This boolean tracks whether
     * or not the data transmission is enabled as we allow the user to pause transmission in the UI.
     */
    private boolean dataTransmission = true;

    /**
     * To save data to a file, you should use a MuseFileWriter.  The MuseFileWriter knows how to
     * serialize the data packets received from the headband into a compact binary format.
     * To read the file back, you would use a MuseFileReader.
     */
    private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();

    /**
     * We don't want file operations to slow down the UI, so we will defer those file operations
     * to a handler on a separate thread.
     */
    private final AtomicReference<Handler> fileHandler = new AtomicReference<>();


    //--------------------------------------
    // Lifecycle / Connection code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeakReference<MainActivity> weakActivity =
                new WeakReference<MainActivity>(this);

        // Muse 2016 (MU-02) headbands use Bluetooth Low Energy technology to
        // simplify the connection process.  This requires access to the COARSE_LOCATION
        // or FINE_LOCATION permissions.  Make sure we have these permissions before
        // proceeding.
        ensurePermissions();

        // Load and initialize our UI.
        initUI();

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addsessionbtn) {
            Intent intent = new Intent(getApplicationContext(), NewSessionActivity.class);
            startActivity(intent);
        }else {
            Button b = (Button)v;
            String sessionId = b.getText().toString();
            WorkSessionTemplate template;
            try {
                template = myTemplates.getTemplate(sessionId, this);

            }catch(IOException e){
                return;
            }

            Intent intent = new Intent(getApplicationContext(), LiveSessionActivity.class);
            intent.putExtra("session_id_template", template);
            startActivity(intent);

        }
    }


    //--------------------------------------
    // Permissions

    /**
     * The ACCESS_COARSE_LOCATION permission is required to use the
     * Bluetooth Low Energy library and must be requested at runtime for Android 6.0+
     * On an Android 6.0 device, the following code will display 2 dialogs,
     * one to provide context and the second to request the permission.
     * On an Android device running an earlier version, nothing is displayed
     * as the permission is granted from the manifest.
     *
     * If the permission is not granted, then Muse 2016 (MU-02) headbands will
     * not be discovered and a SecurityException will be thrown.
     */
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have the ACCESS_COARSE_LOCATION permission so create the dialogs asking
            // the user to grant us the permission.

            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            // This is the context dialog which explains to the user the reason we are requesting
            // this permission.  When the user presses the positive (I Understand) button, the
            // standard Android permission dialog will be displayed (as defined in the button
            // listener above).
            AlertDialog introDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .create();
            introDialog.show();
        }
    }

    //--------------------------------------
    // UI Specific methods

    /**
     * Initializes the UI of the example application.
     */
    private void initUI() {
        setContentView(R.layout.activity_main);


        try {
            NewSessionActivity.addSessionTemplate(this, "Demo", 1, 1, 1);
        }catch(IOException e){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(e.getMessage());
        }

        ArrayList<WorkSessionTemplate> templateList = new ArrayList<WorkSessionTemplate>();
        try {
            templateList = myTemplates.getTemplates(this);
        }catch(IOException e){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(e.getMessage());
        }
        Button firstSession = (Button) findViewById(R.id.firstsessionbtn);
        Button secondSession = (Button) findViewById(R.id.secondsessionbtn);
        Button thirdSession = (Button) findViewById(R.id.thirdsessionbtn);
        Button fourthSession = (Button) findViewById(R.id.fourthsessionbtn);
        Button fifthSession = (Button) findViewById(R.id.fifthsessionbtn);
        Button addSession = (Button) findViewById(R.id.addsessionbtn);
        addSession.setOnClickListener(this);

        ArrayList<Button> buttonSessions = new ArrayList<Button>();
        buttonSessions.add(firstSession);
        buttonSessions.add(secondSession);
        buttonSessions.add(thirdSession);
        buttonSessions.add(fourthSession);
        buttonSessions.add(fifthSession);


        for (int i = 0; i < buttonSessions.size(); i++) {
            buttonSessions.get(i).setVisibility(View.INVISIBLE);
        }

        for (int i = 0; i < templateList.size(); i++) {
            String templateName = templateList.get(i).getName();
            Button button = buttonSessions.get(i);
            button.setVisibility(View.VISIBLE);
            button.setText(templateName);
            button.setOnClickListener(this);
        }
        if(templateList.size()>=5){
            addSession.setVisibility(View.INVISIBLE);

        }
    }

}