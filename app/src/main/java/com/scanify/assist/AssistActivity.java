package com.scanify.assist;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Kept as the ACTION_ASSIST entry so the system can still select Scanify.
 * The working path is the VoiceInteractionSession; this activity has no
 * screenshot path and the app does not use accessibility.
 */
public class AssistActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, R.string.activity_path_no_voice, Toast.LENGTH_LONG).show();
        finish();
    }
}
