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
    private static final String path = "/sys/devices/virtual/input/lge_touch/dt_wake_enabled";
    private static final String persistFile = "state.dat";
    private static final String onState = "on";
    private static final String offState = "off";
    private Process suProcess;
    private DataOutputStream toDevice;
    private DataInputStream fromDevice;
    private Context context;
    private boolean rooted;


    public DoubleTapUnlocker(Context context) {
        this.context = context;
        suProcess = null;
        rooted = true;

        // Attempt to start su
        try {
            suProcess = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We will consider the user to be root only if the following criteria is met:
        // 1. We're able to run su and get a process
        // 2. after doing so, we are root.
        if (suProcess != null) {
            fromDevice = new DataInputStream(suProcess.getInputStream());
            toDevice = new DataOutputStream(suProcess.getOutputStream());
            String user = CurrentUser();
            if (user == null || !user.contains("root"))
                rooted = false;
        }
        else {
            rooted = false;
        }
    }

    public void SaveState() {
        // IsActive checked below requires root, so disallow saving state if we don't have root.
        if (!rooted)
            return;

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
        // We can neither activate nor deactivate without root, so return early.
        if (!rooted)
            return;

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
        // We cannot activate without a rooted device
        if (!rooted)
            return;
        try {
            toDevice.writeBytes("echo 1 > " + path + "\n");
            toDevice.flush();
            SaveState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Deactivate() {
        // We cannot deactivate without a rooted device
        if (!rooted)
            return;

        try {
            toDevice.writeBytes("echo 0 > " + path + "\n");
            toDevice.flush();
            SaveState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean IsRooted() {
        return rooted;
    }

    public boolean IsActive() {
        if (!rooted)
            return false;

        try {
            toDevice.writeBytes("cat " + path + "\n");
            toDevice.flush();
            String status = fromDevice.readLine();
            if (status != null && status.contains("1"))
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public String CurrentUser() {
        if (toDevice == null || fromDevice == null)
            return null;

        try {
            toDevice.writeBytes("id\n");
            toDevice.flush();
            String user = fromDevice.readLine();
            return user;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
