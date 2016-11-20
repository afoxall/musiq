package com.choosemuse.example.libmuse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;

/**
 * Created by User on 2016-11-20.
 */

public class myTemplates {

    static ArrayList<WorkSessionTemplate> getTemplates(Context ctx) throws IOException{
        ArrayList<WorkSessionTemplate> templates = new ArrayList<WorkSessionTemplate>();
        File dir = ctx.getFilesDir(); // get app directory

        String path = dir.getName() + "/session_templates.ser"; // get path of templates file


        String line;

            FileInputStream fileIn = ctx.openFileInput("session_templates.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            try {
                while (true) {
                    templates.add((WorkSessionTemplate) in.readObject());
                }
            } catch (OptionalDataException e) {
                if (!e.eof) throw e;
            } catch(FileNotFoundException e) {

            }catch(Exception e){
                e.printStackTrace();
            }finally
         {
                in.close();
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
/*
    static void deleteTemplate(String tName, Context ctx) throws IOException{
        ArrayList<WorkSessionTemplate> templates = myTemplates.getTemplates(ctx);

        File dir = ctx.getFilesDir(); // get app directory
        String path = dir.getAbsolutePath() + "session_templates.ser";
        FileOutputStream stream = new FileOutputStream(path, false);
        ObjectOutputStream objStream = new ObjectOutputStream(stream);

        for(WorkSessionTemplate template:templates){
            if(!template.getName().equals(tName)){
                objStream.writeObject(template);
            }
        }
    }*/
}
