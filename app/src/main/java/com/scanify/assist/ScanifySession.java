package com.scanify.assist;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Receives the platform screenshot that the "Use screenshot" assist setting
 * allows, then opens Lens with it.
 */
public class ScanifySession extends VoiceInteractionSession {
    private static final String TAG = "Scanify";
    private static final long SCREENSHOT_TIMEOUT_MS = 1200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::noScreenshot;
    private boolean handled;

    ScanifySession(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        return new View(getContext());
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.i(TAG, "voice session shown, flags=" + showFlags);
        handled = false;
        handler.removeCallbacks(timeoutRunnable);
        makeWindowInvisible();

        if ((showFlags & SHOW_WITH_SCREENSHOT) == 0) {
            Log.w(TAG, "system screenshot was not requested");
            noScreenshot();
        } else {
            handler.postDelayed(timeoutRunnable, SCREENSHOT_TIMEOUT_MS);
        }
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
        Log.i(TAG, "onHandleScreenshot: "
                + (screenshot == null ? "null" : screenshot.getWidth() + "x" + screenshot.getHeight()));

        if (handled) {
            if (screenshot != null) {
                screenshot.recycle();
            }
            return;
        }
        if (screenshot == null) {
            noScreenshot();
            return;
        }

        handled = true;
        handler.removeCallbacks(timeoutRunnable);

        Uri uri = CaptureFlow.saveScreenshot(getContext(), screenshot);
        screenshot.recycle();
        if (uri == null) {
            finish();
            return;
        }
        launchLens(uri);
    }

    @Override
    public void onHide() {
        handler.removeCallbacks(timeoutRunnable);
        super.onHide();
    }

    private void launchLens(Uri uri) {
        Intent send = CaptureFlow.lensIntent(uri);
        send.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startAssistantActivity(send);
        } catch (ActivityNotFoundException e) {
            try {
                Intent chooser = CaptureFlow.chooserIntent(getContext(), uri);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startAssistantActivity(chooser);
            } catch (RuntimeException chooserFailure) {
                Toast.makeText(getContext(), R.string.share_failed, Toast.LENGTH_LONG).show();
            }
        } catch (RuntimeException e) {
            try {
                getContext().startActivity(send);
            } catch (RuntimeException ignored) {
                Toast.makeText(getContext(), R.string.share_failed, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    private void noScreenshot() {
        if (handled) {
            return;
        }
        handled = true;
        handler.removeCallbacks(timeoutRunnable);
        Log.i(TAG, "no system screenshot");
        Toast.makeText(getContext(), R.string.voice_no_screenshot, Toast.LENGTH_LONG).show();
        finish();
    }

    private void makeWindowInvisible() {
        try {
            Dialog dialog = getWindow();
            if (dialog == null) {
                return;
            }
            Window window = dialog.getWindow();
            if (window == null) {
                return;
            }
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0f;
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(params);
        } catch (RuntimeException ignored) {
            // A transparent session window is cosmetic; never let it break capture.
        }
    }
}
