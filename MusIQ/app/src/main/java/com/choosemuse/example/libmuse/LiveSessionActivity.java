package com.choosemuse.example.libmuse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;

import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.List;


/** Ameris Rudland
 * musIQ
 * created @ The Lady Hacks
 * York University, 2016-11-19, 2016-11-20.
 *
 * LiveSessionActivity occurs when a new session is started. It must
 *  - read incoming muse data

 *  - display data in a pretty graph form - lol maybe not
 *  - change music depending on time/mood in the session
 *  - save data from the session
 */

public class LiveSessionActivity extends Activity implements View.OnClickListener {
    /**
     * Tag used for logging purposes.
     */
    private final String TAG = "TestLibMuseAndroid";

    /**
     * The MuseManager is how you detect Muse headbands and receive notifications
     * when the list of available headbands changes.
     */
    private MuseManagerAndroid manager;

    /**
     * A Muse refers to a Muse headband.  Use this to connect/disconnect from the
     * headband, register listeners to receive EEG data and get headband
     * configuration and version information.
     */
    private Muse muse;

    /**
     * The ConnectionListener will be notified whenever there is a change in
     * the connection state of a headband, for example when the headband connects
     * or disconnects.
     *
     * Note that ConnectionListener is an inner class at the bottom of this file
     * that extends MuseConnectionListener.
     */
    private LiveSessionActivity.ConnectionListener connectionListener;

    /**
     * The DataListener is how you will receive EEG (and other) data from the
     * headband.
     *
     * Note that DataListener is an inner class at the bottom of this file
     * that extends MuseDataListener.
     */
    private LiveSessionActivity.DataListener dataListener;

    /**
     * Data comes in from the headband at a very fast rate; 220Hz, 256Hz or 500Hz,
     * depending on the type of headband and the preset configuration.  We buffer the
     * data that is read until we can update the UI.
     *
     * The stale flags indicate whether or not new data has been received and the buffers
     * hold the values of the last data packet received.  We are displaying the EEG, ALPHA_RELATIVE
     * and ACCELEROMETER values in this example.
     *
     * Note: the array lengths of the buffers are taken from the comments in
     * MuseDataPacketType, which specify 3 values for accelerometer and 6
     * values for EEG and EEG-derived packets.
     */
    private final double[] eegBuffer = new double[6];
    private boolean eegStale;
    private final double[] alphaBuffer = new double[6];
    private boolean alphaStale;
    private final double[] betaBuffer = new double[6];
    private boolean betaStale;
    private final double[] thetaBuffer = new double[6];
    private boolean thetaStale;

    private final double[] accelBuffer = new double[3];
    private boolean accelStale;

    private final Handler handler = new Handler();


    private WorkSession liveSession;
    private int progressPercent;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            liveSession.start();
            handler.post(tickUi);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // We need to set the context on MuseManagerAndroid before we can do anything.
        // This must come before other LibMuse API calls as it also loads the library.
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        Intent intent = getIntent();

        WeakReference<LiveSessionActivity> weakActivity =
                new WeakReference<LiveSessionActivity>(this);
        // Register a listener to receive connection state changes.
        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
        WorkSessionTemplate template = (WorkSessionTemplate)getIntent().getSerializableExtra("session_id_template");
        setContentView(R.layout.activity_live_session);
        liveSession = new WorkSession (template); // create a worksession object given the template from the first activity

        manager.setMuseListener(new MuseL(weakActivity));

        initUI();

        TextView WorktimeLabel = (TextView)findViewById(R.id.WorktimeLabel);
        TextView WorktimeAmount = (TextView)findViewById(R.id.worktimeamount);
        TextView breaktimelabel = (TextView)findViewById(R.id.breaktimelabel);
        TextView resttimeamount = (TextView)findViewById(R.id.resttimeamount);
        TextView intervalLabel = (TextView)findViewById(R.id.intervalLabel);
        TextView intervalamount = (TextView)findViewById(R.id.intervalamount);
        TextView productivity = (TextView)findViewById(R.id.productivity);
        Button startButton = (Button)findViewById(R.id.startButton);
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar2);


        startButton.setOnClickListener(this);
        String work = template.getWork()+" mins";
        String rest = template.getRest()+" mins";
        WorktimeAmount.setText(work);
        resttimeamount.setText(rest);
        intervalamount.setText(template.getNumIntervals());

        List<Muse> availableMuses = manager.getMuses();

        // Check that we actually have something to connect to.
        if (availableMuses.size() < 1 ) {
            Log.w(TAG, "There is nothing to connect to");
        } else {

            // Cache the Muse that the user has selected.
            muse = availableMuses.get(0);
            // Unregister all prior listeners and register our data listener to
            // receive the MuseDataPacketTypes we are interested in.  If you do
            // not register a listener for a particular data type, you will not
            // receive data packets of that type.
            muse.unregisterAllListeners();
            muse.registerConnectionListener(connectionListener);
            muse.registerDataListener(dataListener, MuseDataPacketType.EEG);
            muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.BETA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.GAMMA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
            muse.registerDataListener(dataListener, MuseDataPacketType.BATTERY);
            muse.registerDataListener(dataListener, MuseDataPacketType.DRL_REF);
            muse.registerDataListener(dataListener, MuseDataPacketType.QUANTIZATION);

            // Initiate a connection to the headband and stream the data asynchronously.
            muse.runAsynchronously();
        }

    }

    private void initUI() {
        TextView WorktimeLabel = (TextView)findViewById(R.id.WorktimeLabel);
        TextView WorktimeAmount = (TextView)findViewById(R.id.worktimeamount);
        TextView breaktimelabel = (TextView)findViewById(R.id.breaktimelabel);
        TextView resttimeamount = (TextView)findViewById(R.id.resttimeamount);
        TextView intervalLabel = (TextView)findViewById(R.id.intervalLabel);
        TextView intervalamount = (TextView)findViewById(R.id.intervalamount);
        TextView productivity = (TextView)findViewById(R.id.productivity);
        Button startButton = (Button)findViewById(R.id.startButton);
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar2);


    }


    //--------------------------------------
    // Listener translators
    //
    // Each of these classes extend from the appropriate listener and contain a weak reference
    // to the activity.  Each class simply forwards the messages it receives back to the Activity.
    class MuseL extends MuseListener {
        final WeakReference<LiveSessionActivity> activityRef;

        MuseL(final WeakReference<LiveSessionActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void museListChanged() {
            manager.getMuses();
        }
    }
    private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {
            liveSession.update();
            if (liveSession.alphaStale || liveSession.betaStale || liveSession.gammaStale) {
                updateScreen();
            }
            handler.postDelayed(tickUi, 1000 / WorkSession.Herz);
        }
    };


    private void updateScreen(){
        //do stuff to update the data on screen

        TextView productivity = (TextView)findViewById(R.id.productivity);
        String prods = liveSession.getFocus()+"";
        productivity.setText(prods);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class ConnectionListener extends MuseConnectionListener {
        final WeakReference<LiveSessionActivity> activityRef;

        ConnectionListener(final WeakReference<LiveSessionActivity> activityRef) {

            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            activityRef.get().receiveMuseConnectionPacket(p, muse);
        }
    }

    /*
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     * @param p     A packet containing the current and prior connection states
     * @param muse  The headband whose state changed.
     */
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        // Format a message to show the change of connection state in the UI.
        final String status = p.getPreviousConnectionState() + " -> " + current;
        Log.i(TAG, status);


        if (current == ConnectionState.DISCONNECTED) {
            Log.i(TAG, "Muse disconnected:" + muse.getName());

            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
        }
    }
    /**
     * You will receive a callback to this method each time the headband sends a MuseDataPacket
     * that you have registered.  You can use different listeners for different packet types or
     * a single listener for all packet types as we have done here.
     * @param p     The data packet containing the data from the headband (eg. EEG data)
     * @param muse  The headband that sent the information.
     */
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        liveSession.setBuffers(p);
    }

    /* Helper methods to get different packet values.  These methods simply store the
    * data in the buffers for later display in the UI.
    *
            * getEegChannelValue can be used for any EEG or EEG derived data packet type
    * such as EEG, ALPHA_ABSOLUTE, ALPHA_RELATIVE or HSI_PRECISION.  See the documentation
    * of MuseDataPacketType for all of the available values.
    * Specific packet types like ACCELEROMETER, GYRO, BATTERY and DRL_REF have their own
    * getValue methods.
            */
    private void getEegChannelValues(double[] buffer, MuseDataPacket p) {
        buffer[0] = p.getEegChannelValue(Eeg.EEG1);
        buffer[1] = p.getEegChannelValue(Eeg.EEG2);
        buffer[2] = p.getEegChannelValue(Eeg.EEG3);
        buffer[3] = p.getEegChannelValue(Eeg.EEG4);
        buffer[4] = p.getEegChannelValue(Eeg.AUX_LEFT);
        buffer[5] = p.getEegChannelValue(Eeg.AUX_RIGHT);
    }

    /**
     * You will receive a callback to this method each time an artifact packet is generated if you
     * have registered for the ARTIFACTS data type.  MuseArtifactPackets are generated when
     * eye blinks are detected, the jaw is clenched and when the headband is put on or removed.
     * @param p     The artifact packet with the data from the headband.
     * @param muse  The headband that sent the information.
     */
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
    }

    class DataListener extends MuseDataListener {
        final WeakReference<LiveSessionActivity> activityRef;

        DataListener(final WeakReference<LiveSessionActivity> activityRef) {

            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            activityRef.get().receiveMuseDataPacket(p, muse);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
    }
}

