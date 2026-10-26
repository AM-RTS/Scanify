package com.scanify.assist;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String LENS_PACKAGE = "com.google.ar.lens";

    private TextView assistantStatus;
    private TextView voiceStatus;
    private TextView lensStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assistantStatus = findViewById(R.id.status_assistant);
        voiceStatus = findViewById(R.id.status_voice);
        lensStatus = findViewById(R.id.status_lens);

        findViewById(R.id.btn_assistant).setOnClickListener(v -> openAssistantSettings());
        findViewById(R.id.btn_test).setOnClickListener(v -> testCapture());
        requestNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        assistantStatus.setText(isAssistantSelected()
                ? R.string.status_assistant_on
                : R.string.status_assistant_off);

        voiceStatus.setText(isVoiceServiceSelected()
                ? R.string.status_voice_on
                : R.string.status_voice_off);

        lensStatus.setText(isLensInstalled()
                ? R.string.status_lens_on
                : R.string.status_lens_off);
    }

    private void openAssistantSettings() {
        if (tryStart(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))) {
            return;
        }
        if (tryStart(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))) {
            return;
        }
        tryStart(new Intent(Settings.ACTION_SETTINGS));
    }

    private boolean tryStart(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAssistantSelected() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager != null
                && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)
                && roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
            return true;
        }
        String value = Settings.Secure.getString(getContentResolver(), "assistant");
        if (value == null) {
            return false;
        }
        ComponentName ours = new ComponentName(this, AssistActivity.class);
        return ours.equals(ComponentName.unflattenFromString(value));
    }

    private void testCapture() {
        Toast.makeText(this, R.string.hold_home_to_test, Toast.LENGTH_LONG).show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private boolean isLensInstalled() {
        try {
            getPackageManager().getPackageInfo(LENS_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isVoiceServiceSelected() {
        String value = Settings.Secure.getString(getContentResolver(), "voice_interaction_service");
        if (value == null) {
            return false;
        }
        ComponentName expected = new ComponentName(this, ScanifyVoiceInteractionService.class);
        return expected.equals(ComponentName.unflattenFromString(value));
    }
}
