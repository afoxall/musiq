package com.choosemuse.example.libmuse;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.File;
import android.content.Context;

/**
 * Created by User on 2016-11-19.
 */

public class mySessions {

    final static String sessionDir = "saved_sessions";
    final static String sessionExt = ".csv";


    static ArrayList<WorkSession> getSessions(Context ctx) throws FileNotFoundException, IOException{

        ArrayList<WorkSession> sessions = new ArrayList<WorkSession>();
        File dir = ctx.getFilesDir(); // get app directory

        String path = dir.getAbsolutePath() + sessionDir; // go into the saved_sessions folder
        dir = new File(path);

        File[] directoryListing = dir.listFiles();
        String line;
        String[] buff;
        if (directoryListing != null) {
            for (File child : directoryListing) {
                BufferedReader br = new BufferedReader(new FileReader(child));
                String template = br.readLine();
                String start = child.getName();
                ArrayList<DataPoint> data = new ArrayList<DataPoint>();

                while ((line = br.readLine()) != null) {
                    buff = line.split(",");
                    data.add(new DataPoint(Double.parseDouble(buff[0]),Double.parseDouble(buff[1]),Double.parseDouble(buff[2])));
                }
                br.close();
                sessions.add(new WorkSession(template, start, data, ctx));
            }
        }
        return sessions;
    }
}
