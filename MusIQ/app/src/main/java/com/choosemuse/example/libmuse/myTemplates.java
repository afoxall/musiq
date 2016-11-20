package com.choosemuse.example.libmuse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;

/**
 * Created by User on 2016-11-20.
 */

public class myTemplates {

    static ArrayList<WorkSessionTemplate> getTemplates(Context ctx) throws FileNotFoundException, IOException{
        ArrayList<WorkSessionTemplate> templates = new ArrayList<WorkSessionTemplate>();
        File dir = ctx.getFilesDir(); // get app directory

        String path = dir.getAbsolutePath() + "session_templates.ser"; // get path of templates file


        String line;
        try{
            FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            while(in.available() > 0) {
                templates.add((WorkSessionTemplate) in.readObject());
            }
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        return templates;
    }

    static WorkSessionTemplate getTemplate(String t, Context ctx) throws IOException{
        ArrayList<WorkSessionTemplate> templates = myTemplates.getTemplates(ctx);
        for(WorkSessionTemplate template:templates){
            if(template.getName().equals(t)){
                return template;
            }
        }
        return null;
    }
}
