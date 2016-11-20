package com.choosemuse.example.libmuse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
/**
 * Created by User on 2016-11-19.
 */


public class WorkSessionTemplate implements java.io.Serializable{

    private String name;
    private int workInterval;
    private int restInterval;
    private int numIntervals;


    public WorkSessionTemplate(String n, int work, int rest, int num){
        //for now assume the values of the inputs can be controlled at the UI level, if not we will need to add a bunch of checks and throw statements
        name = n;
        workInterval = work;
        restInterval = rest;
        numIntervals = num;

    }
    String getName(){
        return name;
    }

    int getRest(){
        return restInterval;
    }

    int getWork(){
        return workInterval;
    }
    int getNumIntervals(){
        return numIntervals;
    }

    void setName(String n){
        name = n;
    }

    void setRest(int r){
        restInterval = r;
    }

    void setWork(int w){
        workInterval = w;
    }
    void setNumIntervals(int n){
        numIntervals = n;
    }

}


