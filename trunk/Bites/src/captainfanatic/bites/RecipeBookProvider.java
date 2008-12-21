/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package captainfanatic.bites;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import captainfanatic.bites.RecipeBook.Recipes;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class RecipeBookProvider extends ContentProvider {

    private static final String TAG = "RecipeBookProvider";

    private static final String DATABASE_NAME = "recipe_book.db";
    private static final int DATABASE_VERSION = 1;
    private static final String RECIPE_TABLE_NAME = "recipes";

    private static HashMap<String, String> sRecipesProjectionMap;

    private static final int RECIPES = 1;
    private static final int RECIPE_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + RECIPE_TABLE_NAME + " ("
                    + Recipes._ID + " INTEGER PRIMARY KEY,"
                    + Recipes.TITLE + " TEXT,"
                    + Recipes.RECIPE + " TEXT,"
                    + Recipes.CREATED_DATE + " INTEGER,"
                    + Recipes.MODIFIED_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            qb.setTables(RECIPE_TABLE_NAME);
            qb.setProjectionMap(sRecipesProjectionMap);
            break;

        case RECIPE_ID:
            qb.setTables(RECIPE_TABLE_NAME);
            qb.setProjectionMap(sRecipesProjectionMap);
            qb.appendWhere(Recipes._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = RecipeBook.Recipes.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            return Recipes.CONTENT_TYPE;

        case RECIPE_ID:
            return Recipes.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != RECIPES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(RecipeBook.Recipes.CREATED_DATE) == false) {
            values.put(RecipeBook.Recipes.CREATED_DATE, now);
        }

        if (values.containsKey(RecipeBook.Recipes.MODIFIED_DATE) == false) {
            values.put(RecipeBook.Recipes.MODIFIED_DATE, now);
        }

        if (values.containsKey(RecipeBook.Recipes.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(RecipeBook.Recipes.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(RecipeBook.Recipes.RECIPE) == false) {
            values.put(RecipeBook.Recipes.RECIPE, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(RECIPE_TABLE_NAME, Recipes.RECIPE, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(RecipeBook.Recipes.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            count = db.delete(RECIPE_TABLE_NAME, where, whereArgs);
            break;

        case RECIPE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(RECIPE_TABLE_NAME, Recipes._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            count = db.update(RECIPE_TABLE_NAME, values, where, whereArgs);
            break;

        case RECIPE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(RECIPE_TABLE_NAME, values, Recipes._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(RecipeBook.AUTHORITY, "notes", RECIPES);
        sUriMatcher.addURI(RecipeBook.AUTHORITY, "notes/#", RECIPE_ID);

        sRecipesProjectionMap = new HashMap<String, String>();
        sRecipesProjectionMap.put(Recipes._ID, Recipes._ID);
        sRecipesProjectionMap.put(Recipes.TITLE, Recipes.TITLE);
        sRecipesProjectionMap.put(Recipes.RECIPE, Recipes.RECIPE);
        sRecipesProjectionMap.put(Recipes.CREATED_DATE, Recipes.CREATED_DATE);
        sRecipesProjectionMap.put(Recipes.MODIFIED_DATE, Recipes.MODIFIED_DATE);
    }
}
