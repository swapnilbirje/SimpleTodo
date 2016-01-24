package com.codepath.simpletodo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codepath.simpletodo.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbirje on 1/23/16.
 */

public class PostsDatabaseHelper extends SQLiteOpenHelper {

    private static PostsDatabaseHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "todoDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_TODO_ITEM = "TODO";

    // Post Table Columns
    private static final String KEY_TODO_ID = "id";
    private static final String KEY_TODO_TASK = "task";

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_TODO_ITEM +
                "(" +
                KEY_TODO_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_TODO_TASK + " TEXT" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO_ITEM);
            onCreate(db);
        }
    }

    public static synchronized PostsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx

        if (sInstance == null) {
            sInstance = new PostsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private PostsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public boolean addPost(Item item) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_TASK, item.text);

            //SQLite auto increments the primary key column.
            itemId = db.insertOrThrow(TABLE_TODO_ITEM, null, values);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.d("tag", "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
        return itemId != -1;
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.

    public boolean addOrUpdateUser(Item item, String originalVal) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;
        int id=-1;
        db.beginTransaction();
        try {
            //find if data already exists
            String ITEMS_SELECT_QUERY =
                    String.format("SELECT * FROM %s where %s = %s",
                            TABLE_TODO_ITEM,
                            KEY_TODO_TASK,
                            "\""+originalVal+"\""
                    );
            Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndex(KEY_TODO_ID));
                    } while(cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.d("", "Error while trying to get posts from database");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            //Update data if already exists
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_TASK, item.getText());

            // This assumes userNames are unique
            itemId = db.update(TABLE_TODO_ITEM, values, KEY_TODO_ID+ "=" + id ,null);

            // Check if update succeeded
            if (itemId != 1) {
               //if Item not updated then insert new item
                itemId = db.insertOrThrow(TABLE_TODO_ITEM, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("", "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return itemId !=-1;
    }

    //get all items from DB for display
    public List<String> getAllItems() {
        List<String> items = new ArrayList<>();
        String ITEMS_SELECT_QUERY = String.format("SELECT * FROM %s ",
                                            TABLE_TODO_ITEM
                                    );
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    items.add(cursor.getString(cursor.getColumnIndex(KEY_TODO_TASK)));
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return items;
    }

    public boolean deleteItem(String name[])
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TODO_ITEM, KEY_TODO_TASK + "= ?",name) > 0;
    }

}

