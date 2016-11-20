package com.choosemuse.example.libmuse;

/**
 * Created by User on 2016-11-19.
 */

public class DataPoint {
    double beta;
    double alpha;
    double gamma;

    public DataPoint(double a, double b, double g){
        alpha = a;
        beta  = b;
        gamma = g;
    }

    public String toString(){
        return alpha + "," + beta + "," + gamma + "\n";
    }

    public boolean isFocused(){
        return true; //figure out the requirements for user to be focused
    }

}
