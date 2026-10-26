package com.scanify.assist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Read-only provider for the current screenshot. It exists so Lens can be
 * granted access to the PNG without pulling in AndroidX FileProvider.
 */
public class ScreenshotProvider extends ContentProvider {
    private static final String AUTHORITY = "com.scanify.assist.screenshots";
    private static final String FILE_NAME = "screen.png";

    public static Uri uri() {
        return new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(FILE_NAME)
                .build();
    }

    private File file() {
        return new File(getContext().getCacheDir(), FILE_NAME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        check(uri);
        String[] columns = projection != null
                ? projection
                : new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        Object[] row = new Object[columns.length];
        File file = file();
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) {
                row[i] = FILE_NAME;
            } else if (OpenableColumns.SIZE.equals(columns[i])) {
                row[i] = file.length();
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
        File file = file();
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    private void check(Uri uri) {
        if (!AUTHORITY.equals(uri.getAuthority())
                || uri.getPathSegments().size() != 1
                || !FILE_NAME.equals(uri.getPathSegments().get(0))) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
