package champion.mipis.database;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private SQLiteDatabase db;

    public static final String TABLE_USER = "user";

    public static final String TABLE_MESSAGE = "message";

    public static final String TABLE_THREAD_USER = "thread_user";

    static final String DATABASE_NAME = "mipis.db";

    // user table 
    public static final String _ID = "_id";
    public static final String USERNAME = "username";
    public static final String NICK_NAME = "nick";
    public static final String AVATAR = "avatar";
    public static final String GENDER = "gender";
    public static final String MOOD = "mood";

    // message table 
    // _ID ,
    // USERNAME, 
    public static final String SEND_RECV = "is_send";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String DATE = "date";
    public static final String READ = "readed";
    public static final String STATE = "state"; // failed or succeed.
    public static final String THREAD_ID = "thread_id"; // .

    private static DatabaseHelper mInstance;

    private static int VERSION = 1;

    private Context mContext;

    public static synchronized DatabaseHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DatabaseHelper(context, DATABASE_NAME, null, VERSION);
        }
        return mInstance;
    }

    //thread_user table
    // _ID ,
    // USERNAME, 
    
    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createUserTable(db);
        createMessageTable(db);
        createThreadUserTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_USER
                + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME + "TEXT NOT NULL, "
                + NICK_NAME + "TEXT, "
                + AVATAR + "TEXT, "
                + GENDER + "TEXT, "
                + MOOD + "TEXT)");
    }

    private void createThreadUserTable(SQLiteDatabase db2) {
        db.execSQL("create table if not exists " + TABLE_MESSAGE
                + "( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USERNAME + "TEXT NOT NULL )");
    }

    
    /*
    public static final String SEND_RECV = "is_send";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String DATE = "date";
    public static final String READ = "readed";
    public static final String STATE = "state"; // failed or succeed.
    public static final String THREAD_ID = "thread_id"; // .*/
    private void createMessageTable(SQLiteDatabase db2) {
        db.execSQL("create table if not exists " + TABLE_USER
                + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME + "TEXT NOT NULL, "
                + SEND_RECV + "TEXT, "
                + TYPE + "integer, "
                + BODY + "TEXT, "
                + DATE + "long, "
                + READ + "integer, "
                + STATE + "integer, "
                + THREAD_ID + "integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        onCreate(db);
    }
}

/*
    private void createRcsTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists "
                + RichMessagingCollectionProvider.TABLE
                + " ("
                // Fields for chat
                + RichMessagingCollectionData.KEY_ID
                + " integer primary key autoincrement, "
                + RichMessagingCollectionData.KEY_TYPE + " integer, "
                + RichMessagingCollectionData.KEY_CHAT_SESSION_ID + " TEXT, "
                + RichMessagingCollectionData.KEY_TIMESTAMP + " long, "
                + RichMessagingCollectionData.KEY_SENT_TIMESTAMP + " long, "
                + RichMessagingCollectionData.KEY_CONTACT + " TEXT, "
                + RichMessagingCollectionData.KEY_STATUS + " integer, "
                + RichMessagingCollectionData.KEY_DATA
                + " TEXT, "
                + RichMessagingCollectionData.KEY_MESSAGE_ID
                + " TEXT, "
                + RichMessagingCollectionData.KEY_IS_SPAM
                + " integer, "
                + RichMessagingCollectionData.KEY_CHAT_ID
                + " TEXT, "
                + RichMessagingCollectionData.KEY_CHAT_REJOIN_ID
                + " TEXT, "

                // Fields for file transfer
                + RichMessagingCollectionData.KEY_MIME_TYPE + " TEXT, "
                + RichMessagingCollectionData.KEY_NAME + " TEXT, "
                + RichMessagingCollectionData.KEY_SIZE + " long, "
                + RichMessagingCollectionData.KEY_TOTAL_SIZE + " long, "

                + RichMessagingCollectionData.KEY_NUMBER_MESSAGES + " integer, "
                + RichMessagingCollectionData.KEY_HOST_CONTACT
                + " TEXT, "
                // Changed by Deutsche Telekom
                + RichMessagingCollectionData.KEY_IMDN_RECORD_ROUTE
                + " TEXT, "

                // fields for IMDN in chat or FT group
                + RichMessagingCollectionData.KEY_IMDN_DELIVERED_LIST + " TEXT, "
                + RichMessagingCollectionData.KEY_IMDN_DISPLAYED_LIST + " TEXT,"
                + RichMessagingCollectionData.KEY_DISPLAY_NAME + " TEXT,"
                
                // thumbnail for image/video file transfer
                + RichMessagingCollectionData.KEY_THUMBNAIL + " BLOB,"
                
                // For CMCC public message
                + RichMessagingCollectionData.KEY_PA_UUID + " TEXT,"
                + RichMessagingCollectionData.KEY_MEDIA_TYPE + " integer);"
                );
        // FIXME workaround to start _id from 1000000000 (prevent the same as
        // mmssms table that will cause merge problem)
        db.execSQL("INSERT INTO " + RichMessagingCollectionProvider.TABLE + "("
                + RichMessagingCollectionData.KEY_ID + ") "
                + "SELECT 1000000000 WHERE NOT EXISTS " + "(SELECT "
                + RichMessagingCollectionData.KEY_ID + " FROM "
                + RichMessagingCollectionProvider.TABLE + " WHERE "
                + RichMessagingCollectionData.KEY_ID + "=1000000000 LIMIT 1)");
    }*/
