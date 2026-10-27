package com.scanify.assist;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;

final class CaptureFlow {
    static final String LENS_PACKAGE = "com.google.ar.lens";

    private CaptureFlow() {
    }

    static byte[] encodeScreenshot(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            return null;
        }
        return out.toByteArray();
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
