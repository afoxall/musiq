package com.musiq;

/**
 * Created by User on 2016-11-19.
 */


public class WorkSession {
    int maxNameLength = 20;


    String name;
    int workInterval;
    int restInterval;
    int numIntervals;
    WorkSession(String n, int work, int rest, int num){
        //for now assume the values of the inputs can be controlled at the UI level, if not we will need to add a bunch of checks and throw statements
        name = n;
        workInterval = work;
        restInterval = rest;
        numIntervals = num;

    }

}


