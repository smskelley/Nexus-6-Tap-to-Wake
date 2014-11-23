/* Copyright (c) 2014 Sean Kelley */
package com.codechron.n6doubletapunlock;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by psion on 11/21/14.
 */
public class DoubleTapUnlocker {
    private static final String path = "/sys/bus/i2c/devices/1-004a/tsp";
    private static final String persistFile = "state.dat";
    private static final String onState = "on";
    private static final String offState = "off";
    private Process suProcess;
    private DataOutputStream toDevice;
    private DataInputStream fromDevice;
    private Context context;


    public DoubleTapUnlocker(Context context) {
        this.context = context;
        suProcess = null;

        // Attempt to start su
        try {
            suProcess = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fromDevice = new DataInputStream(suProcess.getInputStream());
        toDevice = new DataOutputStream(suProcess.getOutputStream());
    }

    public void SaveState() {
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(persistFile, Context.MODE_PRIVATE);

            if (IsActive())
                outputStream.write(onState.getBytes());
            else
                outputStream.write(offState.getBytes());

            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RestoreState() {
        FileInputStream inputStream = null;
        String buffer;
        try {
            byte[] byte_buffer = new byte[80];
            inputStream = context.openFileInput(persistFile);
            inputStream.read(byte_buffer);
            buffer = new String(byte_buffer);

            inputStream.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (buffer.contains(onState))
            Activate();
        else
            Deactivate();
    }

    public void Activate() {
        try {
            toDevice.writeBytes("echo AUTO > " + path + "\n");
            toDevice.flush();
            SaveState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Deactivate() {
        try {
            toDevice.writeBytes("echo OFF > " + path + "\n");
            toDevice.flush();
            SaveState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean IsActive() {
        if (suProcess == null)
            return false;

        try {
            toDevice.writeBytes("cat " + path + "\n");
            toDevice.flush();
            String status = fromDevice.readLine();
            if (status != null &&
                    (status.contains("ON") || status.contains("AUTO")))
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}
