package com.scanify.assist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Read-only, memory-backed provider for the current screenshot. No screenshot
 * file is written; each open receives a fresh pipe over the same encoded bytes,
 * because Lens opens the URI more than once.
 */
public class ScreenshotProvider extends ContentProvider {
    private static final String AUTHORITY = "com.scanify.assist.screenshots";
    private static final String FILE_NAME = "screen.png";

    private static volatile byte[] screenshot;

    public static Uri uri() {
        return new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(FILE_NAME)
                .build();
    }

    static void setScreenshot(byte[] png) {
        screenshot = png;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        check(uri);
        byte[] png = screenshot;
        String[] columns = projection != null
                ? projection
                : new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        Object[] row = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) {
                row[i] = FILE_NAME;
            } else if (OpenableColumns.SIZE.equals(columns[i])) {
                row[i] = png == null ? 0 : png.length;
            } else {
                row[i] = null;
            }
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        check(uri);
        return "image/png";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        check(uri);
        if (!mode.startsWith("r")) {
            throw new FileNotFoundException("Read-only provider");
        }
        byte[] png = screenshot;
        if (png == null) {
            throw new FileNotFoundException("No screenshot available");
        }

        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            Thread writer = new Thread(() -> {
                try (ParcelFileDescriptor.AutoCloseOutputStream out =
                             new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])) {
                    out.write(png);
                } catch (IOException ignored) {
                    // Lens closed the stream early.
                }
            }, "scanify-pipe");
            writer.setDaemon(true);
            writer.start();
            return pipe[0];
        } catch (IOException e) {
            throw new FileNotFoundException("Could not create pipe: " + e);
        }
    }

    private void check(Uri uri) {
        if (!AUTHORITY.equals(uri.getAuthority())
                || uri.getPathSegments().size() != 1
                || !FILE_NAME.equals(uri.getPathSegments().get(0))) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
