/* Copyright (c) 2014 Sean Kelley */
package com.codechron.n6doubletapunlock;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
    private Button activateButton;
    private TextView statusText;
    private DoubleTapUnlocker unlocker;
    private boolean unlocked;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        unlocker = new DoubleTapUnlocker(context);
        unlocked = unlocker.IsActive();

        activateButton = (Button)findViewById(R.id.activate_button);
        statusText = (TextView)findViewById(R.id.status_text);

        updateStatusText();
        updateButtonText();

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!unlocked)
                    unlocker.Activate();
                else
                    unlocker.Deactivate();

                // Check the results..
                unlocked = unlocker.IsActive();
                updateStatusText();
                updateButtonText();
                unlocker.SaveState();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStatusText() {
        if (unlocked) {
            statusText.setText("Double Tap to unlock is enabled.");
            statusText.setTextColor(getResources().getColor(R.color.status_text_activated));
        }
        else {
            statusText.setText("Double Tap to unlock is disabled.");
            statusText.setTextColor(getResources().getColor(R.color.status_text_deactivated));
        }
    }

    private void updateButtonText() {
        if (unlocked)
            activateButton.setText("Deactivate");
        else
            activateButton.setText("Activate");
    }
}
