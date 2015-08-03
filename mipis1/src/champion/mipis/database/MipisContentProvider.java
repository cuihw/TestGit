package champion.mipis.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MipisContentProvider extends ContentProvider {

    private static final String TAG = "MipisContentProvider";

    public static final String AUTHORITY = "champion.mipis.database.MipisContentProvider";

    public static final Uri BASE_URI = Uri
            .parse("content://" + AUTHORITY + "/");

    // user table
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI,
            DatabaseHelper.TABLE_MESSAGE);

    // The URI for this table.
    public static final Uri CONTENT_USER_URI = Uri.withAppendedPath(BASE_URI,
            DatabaseHelper.TABLE_USER);

    // The URI for this table.
    public static final Uri CONTENT_THREAD_URI = Uri.withAppendedPath(BASE_URI,
            DatabaseHelper.TABLE_THREAD_USER);

    private DatabaseHelper mDatabaseHelper;

    private static final UriMatcher uriMatcher;

    private static final int MESSAGE = 1;

    private static final int MESSAGE_ID = 2;

    private static final int USER = 3;

    private static final int USER_ID = 4;

    private static final int THREAD_USER = 5;

    private static final int THREAD_USER_ID = 6;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, "message", MESSAGE);
        uriMatcher.addURI(AUTHORITY, "message/#", MESSAGE_ID);

        uriMatcher.addURI(AUTHORITY, "user", USER);
        uriMatcher.addURI(AUTHORITY, "user/#", USER_ID);

        uriMatcher.addURI(AUTHORITY, "thread_user", THREAD_USER);
        uriMatcher.addURI(AUTHORITY, "thread_user/#", THREAD_USER_ID);
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        mDatabaseHelper = DatabaseHelper.getInstance(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        switch (uriMatcher.match(uri)) {
            case MESSAGE:
                return "vnd.android.cursor.dir/champion.mipis.provider.message";
            case MESSAGE_ID:
                return "vnd.android.cursor.item/champion.mipis.provider.message";
            case USER:
                return "vnd.android.cursor.dir/champion.mipis.provider.user";
            case USER_ID:
                return "vnd.android.cursor.item/champion.mipis.provider.user";
            case THREAD_USER:
                return "vnd.android.cursor.dir/champion.mipis.provider.thread_user";
            case THREAD_USER_ID:
                return "vnd.android.cursor.item/champion.mipis.provider.thread_user";
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        long rowId;

        switch (match) {
            case MESSAGE:
            case MESSAGE_ID:
                rowId = db.insert(DatabaseHelper.TABLE_MESSAGE,
                        DatabaseHelper._ID, initialValues);
                uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
                break;

            case USER:
            case USER_ID:
                rowId = db.insert(DatabaseHelper.TABLE_USER,
                        DatabaseHelper._ID, initialValues);
                uri = ContentUris.withAppendedId(CONTENT_USER_URI, rowId);
                break;

            case THREAD_USER:
            case THREAD_USER_ID:
                rowId = db.insert(DatabaseHelper.TABLE_THREAD_USER,
                        DatabaseHelper._ID, initialValues);
                uri = ContentUris.withAppendedId(CONTENT_THREAD_URI, rowId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        String id = uri.getPathSegments().get(1);

        int count = 0;
        switch (match) {
            case MESSAGE:

                count = db.delete(DatabaseHelper.TABLE_MESSAGE, where,
                        whereArgs);
                break;

            case MESSAGE_ID:
                where = DatabaseHelper._ID
                        + "="
                        + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                : "");

                count = db.delete(DatabaseHelper.TABLE_MESSAGE, where,
                        whereArgs);
                break;

            case USER:

                count = db.delete(DatabaseHelper.TABLE_USER, where, whereArgs);
                break;
            case USER_ID:
                where = DatabaseHelper._ID
                        + "="
                        + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                : "");
                count = db.delete(DatabaseHelper.TABLE_USER, where, whereArgs);
                break;

            case THREAD_USER:
                count = db.delete(DatabaseHelper.TABLE_THREAD_USER, where,
                        whereArgs);

                break;
            case THREAD_USER_ID:
                where = DatabaseHelper._ID
                        + "="
                        + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                : "");
                count = db.delete(DatabaseHelper.TABLE_THREAD_USER, where,
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        String groupBy = null;
        int match = uriMatcher.match(uri);
        Log.d(TAG, "query match >" + match);
        switch (match) {
            case MESSAGE:
                qb.setTables(DatabaseHelper.TABLE_MESSAGE);

                break;
            case MESSAGE_ID:

                qb.setTables(DatabaseHelper.TABLE_MESSAGE);
                qb.appendWhere(DatabaseHelper._ID + "="
                        + uri.getPathSegments().get(1));
                break;
            case USER:
                qb.setTables(DatabaseHelper.TABLE_USER);

                break;
            case USER_ID:
                qb.setTables(DatabaseHelper.TABLE_USER);

                qb.appendWhere(DatabaseHelper._ID + "="
                        + uri.getPathSegments().get(1));

                break;
            case THREAD_USER:
                qb.setTables(DatabaseHelper.TABLE_THREAD_USER);

            case THREAD_USER_ID:

                qb.setTables(DatabaseHelper.TABLE_THREAD_USER);

                qb.appendWhere(DatabaseHelper._ID + "="
                        + uri.getPathSegments().get(1));
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor c = qb.query(db, projectionIn, selection, selectionArgs,
                groupBy, null, sort);

        // Register the contexts ContentResolver to be notified if
        // the cursor result set changes.
        if (c != null) {
            Log.d(TAG, "query c.getCount() >" + c.getCount());

            c.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return c;

    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        int count;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        String segment = uri.getPathSegments().get(1);
        int id = Integer.parseInt(segment);

        switch (match) {
            case MESSAGE:
                count = db.update(DatabaseHelper.TABLE_MESSAGE, values, where,
                        whereArgs);
                break;
            case MESSAGE_ID:
                count = db.update(DatabaseHelper.TABLE_MESSAGE, values,
                        DatabaseHelper._ID + "=" + id, null);

                break;
            case USER:
                count = db.update(DatabaseHelper.TABLE_USER, values, where,
                        whereArgs);
                break;
            case USER_ID:
                count = db.update(DatabaseHelper.TABLE_USER, values,
                        DatabaseHelper._ID + "=" + id, null);
                break;

            case THREAD_USER:

                count = db.update(DatabaseHelper.TABLE_THREAD_USER, values,
                        where, whereArgs);
                break;
            case THREAD_USER_ID:
                count = db.update(DatabaseHelper.TABLE_THREAD_USER, values,
                        DatabaseHelper._ID + "=" + id, null);
                break;

            default:
                throw new UnsupportedOperationException("Cannot update URI "
                        + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
