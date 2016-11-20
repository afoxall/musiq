package com.choosemuse.example.libmuse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by User on 2016-11-19.
 */


public class NewSessionActivity extends Activity{

    Button btnReset;
    Button btnCreateSession;

    EditText n;
    String name;
    EditText rest;
    int restValue;
    EditText work;
    int workValue;
    EditText numC;
    int numCValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_session);

        btnReset = (Button)findViewById(R.id.resetbtn);
        btnCreateSession = (Button)findViewById(R.id.createSessionbtn);
        
        n = (EditText)findViewById(R.id.sessionName);
        name= n.getText().toString();
        rest = (EditText) findViewById(R.id.restLength) ;
        restValue = Integer.parseInt( rest.getText().toString());
        work = (EditText) findViewById(R.id.workLength) ;
        workValue = Integer.parseInt( work.getText().toString());
        numC = (EditText) findViewById(R.id.numCycles) ;
        numCValue = Integer.parseInt( numC.getText().toString());


        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                n.setText(null);
                rest.setText(null);
                work.setText(null);
                numC.setText(null);
            }
        });

        btnCreateSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Create WorkSessionTemplate
                try {
                    addSessionTemplate(NewSessionActivity.this,name, workValue, restValue, numCValue);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //return to front page
                Intent i = new Intent(NewSessionActivity.this, MainActivity.class );
                startActivity(i);
            }
        });
    }
    // Add session template obj
    static void addSessionTemplate(Context ctx, String n, int work, int rest, int num) throws IOException {

        WorkSessionTemplate workSes = new WorkSessionTemplate(n,work,rest,num);
        File dir = ctx.getFilesDir(); // get app directory
        String path = dir.getAbsolutePath() + "session_templates.ser";
        FileOutputStream stream = new FileOutputStream(path, false);
        ObjectOutputStream objStream = new ObjectOutputStream(stream);
        objStream.writeObject(workSes);
    }
}


