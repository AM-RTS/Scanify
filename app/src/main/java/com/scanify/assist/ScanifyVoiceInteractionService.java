package com.scanify.assist;

import android.service.voice.VoiceInteractionService;
import android.util.Log;

/**
 * Entry point for the platform assistant session. The system binds this service
 * when Scanify is the default assistant. The actual work happens in
 * {@link ScanifySession}, which receives the system screenshot.
 */
public class ScanifyVoiceInteractionService extends VoiceInteractionService {
    private static final String TAG = "Scanify";
    private static volatile boolean ready;

    public static boolean isReady() {
        return ready;
    }

    @Override
    public void onReady() {
        super.onReady();
        ready = true;
        Log.i(TAG, "VoiceInteractionService ready");
    }

    @Override
    public void onShutdown() {
        ready = false;
        super.onShutdown();
        Log.i(TAG, "VoiceInteractionService shutdown");
    }
}
