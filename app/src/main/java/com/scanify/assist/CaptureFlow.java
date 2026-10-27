package com.scanify.assist;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

final class CaptureFlow {
    static final String LENS_PACKAGE = "com.google.ar.lens";
    private static final String SCREENSHOT_FILE = "screen.png";

    private CaptureFlow() {
    }

    static Uri saveScreenshot(Context context, Bitmap bitmap) {
        File file = new File(context.getCacheDir(), SCREENSHOT_FILE);
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return ScreenshotProvider.uri();
    }

    static Intent lensIntent(Uri uri) {
        Intent send = sendIntent(uri);
        send.setPackage(LENS_PACKAGE);
        return send;
    }

    static Intent chooserIntent(Context context, Uri uri) {
        return Intent.createChooser(sendIntent(uri), context.getString(R.string.share_screenshot));
    }

    private static Intent sendIntent(Uri uri) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("image/png");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.setClipData(ClipData.newRawUri("screenshot", uri));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return send;
    }
}
