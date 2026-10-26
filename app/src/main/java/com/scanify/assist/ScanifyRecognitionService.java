package com.scanify.assist;

import android.content.Intent;
import android.speech.RecognitionService;

/**
 * The framework requires every VoiceInteractionService to name a
 * RecognitionService in its metadata. Scanify never uses voice recognition,
 * so this is a quiet stub.
 */
public class ScanifyRecognitionService extends RecognitionService {
    @Override
    protected void onStartListening(Intent recognizerIntent, RecognitionService.Callback listener) {
    }

    @Override
    protected void onStopListening(RecognitionService.Callback listener) {
    }

    @Override
    protected void onCancel(RecognitionService.Callback listener) {
    }
}
