/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ils.util;
import org.apache.log4j.Logger;

/**
 *
 * @author maverick
 */
interface OnGeekEventListener {
    // this can be any type of method 
    void onGeekEvent();
}

public class B {
         Logger logger = Logger.getLogger(A.class);


    private OnGeekEventListener mListener; // listener field 
    // setting the listener 
    public void registerOnGeekEventListener(OnGeekEventListener mListener) {
        this.mListener = mListener;
    }
    // my synchronous task 
    public void doGeekStuff() {

        // perform any operation 
        logger.info("Performing callback before synchronous Task");
        // check if listener is registered. 
        if (this.mListener != null) {
            // invoke the callback method of class A 
            mListener.onGeekEvent();
        }
    }

    // Driver Function 
    public static void main(String[] args) {
        B obj = new B();
        OnGeekEventListener mListener = new A();
        obj.registerOnGeekEventListener(mListener);
        obj.doGeekStuff();
    }
}

class A implements OnGeekEventListener {
    @Override
    public void onGeekEvent() {
        System.out.println("Performing callback after synchronous Task");
        // perform some routine operation 
    }
    // some class A methods 
}
