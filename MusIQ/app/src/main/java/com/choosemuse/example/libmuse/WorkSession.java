package com.choosemuse.example.libmuse;

import java.io.FileOutputStream;

import java.util.Date;
import java.util.ArrayList;

import android.content.Context;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.MuseDataPacket;


public class WorkSession {

    private WorkSessionTemplate template;
    private Date startTime;
    private double timeLeft;
    private ArrayList<DataPoint> data;
    boolean working;


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
    public boolean eegStale;
    private final double[] alphaBuffer = new double[6];
    public boolean alphaStale;
    private final double[] betaBuffer = new double[6];
    public boolean betaStale;
    private final double[] thetaBuffer = new double[6];
    public boolean thetaStale;

    private final double[] accelBuffer = new double[3];
    private boolean accelStale;

    public WorkSession(WorkSessionTemplate t) {
        template = t;
        startTime = new Date();
    }

    public void setBuffers(final MuseDataPacket p) {
        // valuesSize returns the number of data values contained in the packet.
        final long n = p.valuesSize();
        switch (p.packetType()) {
            case EEG:
                assert(eegBuffer.length >= n);
                getEegChannelValues(eegBuffer,p);
                eegStale = true;
                break;
            case ACCELEROMETER:
                assert(accelBuffer.length >= n);
                getAccelValues(p);
                accelStale = true;
                break;
            case ALPHA_ABSOLUTE:
                assert(alphaBuffer.length >= n);
                getEegChannelValues(alphaBuffer,p);
                alphaStale = true;
                break;
            case BETA_ABSOLUTE:
                assert(betaBuffer.length >= n);
                getEegChannelValues(betaBuffer,p);
                betaStale = true;
                break;
            case THETA_ABSOLUTE:
                assert(thetaBuffer.length >= n);
                getEegChannelValues(thetaBuffer,p);
                thetaStale = true;
                break;

            case BATTERY:
            case DRL_REF:
            case QUANTIZATION:
            default:
                break;
        }
    }
    public void write(Context ctx) {
        String filename = startTime.toString() + ".csv";
        String string = "sessions/"+template.getName() + "\n";
        for (DataPoint d : data) {
            string = string + d.toString();
        }
        FileOutputStream outputStream;

        try {
            outputStream = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void start() {
        timeLeft = template.getWork();
        working = true;
    }

    public void update() {
        DataPoint p = new DataPoint(max(alphaBuffer, 4), max(betaBuffer, 4), max(thetaBuffer, 4));
        data.add(p);

        if(working){
            if(p.isFocused()){
                timeLeft -= 1/60; //because refresh is currently 60hz, so change when that changes
            }
        }
        if(timeLeft <= 0){
            //change music, tell user they are on a break
            working = false;
            timeLeft = template.getRest();
        }
    }

    private double max(double[] array, int n){
        double max = array[0];
        for(int i = 1; i < n; i++){
            if(array[i] > max){
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Helper methods to get different packet values.  These methods simply store the
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

    private void getAccelValues(MuseDataPacket p) {
        accelBuffer[0] = p.getAccelerometerValue(Accelerometer.X);
        accelBuffer[1] = p.getAccelerometerValue(Accelerometer.Y);
        accelBuffer[2] = p.getAccelerometerValue(Accelerometer.Z);
    }
}