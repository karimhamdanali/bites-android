package com.captainfanatic.bites;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.captainfanatic.bites.RecipeBook.Ingredients;
import com.captainfanatic.bites.RecipeBook.Methods;
import com.captainfanatic.bites.RecipeBook.Recipe;


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


public class RecipesProvider extends ContentProvider {
	
	
	
    private static final String TAG = "RecipesProvider";

    private static final String DATABASE_NAME = "bites_recipes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_INGREDIENTS = "ingredients";
    private static final String TABLE_METHODS = "methods";
    
    private static String SQL_RECIPES;
    private static String SQL_INGREDIENTS;
    private static String SQL_METHODS;

    private static HashMap<String, String> sRecipesProjectionMap;

    private static final int RECIPES = 1;
    private static final int RECIPE_ID = 2;
    private static final int INGREDIENTS = 3;
    private static final int INGREDIENT_ID = 4;
    private static final int METHODS = 5;
    private static final int METHOD_ID = 6;

    private static final UriMatcher sUriMatcher;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL(SQL_RECIPES);
			db.execSQL(SQL_INGREDIENTS);
			db.execSQL(SQL_METHODS);
			
//			db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
//                    + RecipeBook._ID + " INTEGER PRIMARY KEY,"
//                    + RecipeBook.TITLE + " TEXT,"
//                    + RecipeBook.RATING + " INTEGER,"
//                    + RecipeBook.INGREDIENTS + " TEXT,"
//                    + RecipeBook.METHOD + " TEXT,"
//                    + RecipeBook.CREATED_DATE + " INTEGER,"
//                    + RecipeBook.MODIFIED_DATE + " INTEGER"
//                    + ");");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_METHODS);
            onCreate(db);
			
		}
		
	}
	
	private DatabaseHelper mOpenHelper;
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            count = db.delete(TABLE_RECIPES, where, whereArgs);
            break;

        case RECIPE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(TABLE_RECIPES, Recipe._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            return Recipe.CONTENT_TYPE;

        case RECIPE_ID:
            return Recipe.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
		switch (sUriMatcher.match(uri)) {
	        case RECIPES:
	            
		        ContentValues values;
		        if (initialValues != null) {
		            values = new ContentValues(initialValues);
		        } else {
		            values = new ContentValues();
		        }
		
		        Long now = Long.valueOf(System.currentTimeMillis());
		
		        // Make sure that the fields are all set
		        if (values.containsKey(Recipe.CREATED_DATE) == false) {
		            values.put(Recipe.CREATED_DATE, now);
		        }
		
		        if (values.containsKey(Recipe.MODIFIED_DATE) == false) {
		            values.put(Recipe.MODIFIED_DATE, now);
		        }
		
		        if (values.containsKey(Recipe.TITLE) == false) {
		            Resources r = Resources.getSystem();
		            values.put(Recipe.TITLE, r.getString(android.R.string.untitled));
		        }
		        
		        if (values.containsKey(Recipe.RATING) == false) {
		            Resources r = Resources.getSystem();
		            values.put(Recipe.RATING, r.getString(android.R.string.untitled));
		        }
		
		        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		        long rowId = db.insert(TABLE_RECIPES, Recipe.TITLE, values);
		        if (rowId > 0) {
		            Uri noteUri = ContentUris.withAppendedId(Recipe.CONTENT_URI, rowId);
		            getContext().getContentResolver().notifyChange(noteUri, null);
		            return noteUri;
		        }
		        
	        case RECIPE_ID:
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);

		}
	}

	@Override
	public boolean onCreate() {
		Resources r = getContext().getResources();
		SQL_RECIPES = r.getString(R.string.sql_create_table_recipes);
		SQL_INGREDIENTS = r.getString(R.string.sql_create_table_ingredients);
		SQL_METHODS = r.getString(R.string.sql_create_table_methods);
		
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            qb.setTables(TABLE_RECIPES);
            qb.setProjectionMap(sRecipesProjectionMap);
            break;

        case RECIPE_ID:
            qb.setTables(TABLE_RECIPES);
            qb.setProjectionMap(sRecipesProjectionMap);
            qb.appendWhere(Recipe._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = RecipeBook.Recipe.DEFAULT_SORT_ORDER;
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
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case RECIPES:
            count = db.update(TABLE_RECIPES, values, where, whereArgs);
            break;

        case RECIPE_ID:
            String recipeId = uri.getPathSegments().get(1);
            count = db.update(TABLE_RECIPES, values, Recipe._ID + "=" + recipeId
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
        sUriMatcher.addURI(RecipeBook.AUTHORITY, "recipes", RECIPES);
        sUriMatcher.addURI(RecipeBook.AUTHORITY, "recipes/#", RECIPE_ID);

        sRecipesProjectionMap = new HashMap<String, String>();
        sRecipesProjectionMap.put(Recipe._ID, Recipe._ID);
        sRecipesProjectionMap.put(Recipe.TITLE, Recipe.TITLE);
        sRecipesProjectionMap.put(Recipe.RATING, Recipe.RATING);
        sRecipesProjectionMap.put(Ingredients.INGREDIENT, Ingredients.INGREDIENT);
        sRecipesProjectionMap.put(Methods.METHOD, Methods.METHOD);
        sRecipesProjectionMap.put(Recipe.CREATED_DATE, Recipe.CREATED_DATE);
        sRecipesProjectionMap.put(Recipe.MODIFIED_DATE, Recipe.MODIFIED_DATE);
    }	
}
