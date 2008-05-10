/*Bites - an organiser for cooks
 * Copyright (C) 2008 Ben Caldwell <benny.caldwell@gmail.com>
 * The Bites project lives at http://code.google.com/p/bites-android/
 *
 * Bites is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bites is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bites.  If not, see <http://www.gnu.org/licenses/>.
 * */


package captainfanatic.bites;

import android.os.Bundle;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.SQLException;
import android.net.Uri;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;

public class Provider extends ContentProvider
{
	//Convenience classes representing each type of content
	public static final class Inventory implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/inventory");
		public static final String TABLE = "t_Inventory";
		//Column names
		public static final String INGREDIENT = "c_Ingredient";
		public static final String QTY = "c_Qty";
		public static final String UNIT = "c_Unit";
		public static final String QTY_MIN = "c_QtyMin";
		public static final String QTY_BASKET = "c_QtyBasket";
		//Column index numbers
		public static final int INGREDIENT_INDEX = 1;
		public static final int QTY_INDEX = 2;
		public static final int UNIT_INDEX = 3;
		public static final int QTY_MIN_INDEX = 4;
		public static final int QTY_BASKET_INDEX = 5;
		//keys for special updates etc
		public static final String ADD_SHOPPING_KEY = "addShopping";
		public static final String MAKE_MEAL_KEY = "makeMeal";
		public static final String SERVES_MULTIPLIER_KEY = "servesMultiplier";
		public static final String RECIPE_ID_KEY = "recipeId";
	
		public static final String DEFAULT_SORT_ORDER = INGREDIENT + " ASC";
	}

	public static final class Recipes implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/recipes");
		public static final String TABLE = "t_Recipes";
		//Column names
		public static final String RECIPE = "c_Recipe";
		public static final String SERVES = "c_Serves"; //How many people the recipe serves
		//Column index numbers
		public static final int RECIPE_INDEX = 1;
		public static final int SERVES_INDEX = 2;
		//Triggers
		private static String TRIG_DELETE = "t_" + TABLE + "_delete";
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = RECIPE + " ASC";
	}

	public static final class RecipeIngredients implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/recipeingredients");
		public static final String TABLE = "t_RecipeIngredients";
		//Column names for the table
		public static final String ID_RECIPE = "c_ID_Recipe";
		public static final String ID_INGREDIENT = "c_ID_Ingredient";
		public static final String QTY = "c_Qty";
		//Extra Column names for the view
		public static final String INGREDIENT = Inventory.INGREDIENT;
		public static final String UNIT = Inventory.UNIT;
		//Table Column index numbers
		public static final int ID_RECIPE_INDEX = 1;
		public static final int QTY_INDEX = 2;
		public static final int ID_INGREDIENT_INDEX = 3;
	
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = ID_RECIPE + " ASC";
	}

	public static final class RecipeMethod implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/recipemethod");
		public static final String TABLE = "t_RecipeMethod";
		//Column names for the table
		public static final String ID_RECIPE = "c_ID_Recipe";
		public static final String STEP = "c_Step";
		public static final String METHOD = "c_Method";
		//Table Column index numbers
		public static final int ID_RECIPE_INDEX = 1;
		public static final int STEP_INDEX = 2;
		public static final int METHOD_INDEX = 3;
	
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = STEP + " ASC";
	}

	public static final class RecipeIngredientsView implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/recipeingredientsview");
		public static final String TABLE  = "v_RecipeIngredients";
		//Column names for the table
		public static final String ID_RECIPE = "c_ID_Recipe";
		public static final String ID_INGREDIENT = "c_ID_Ingredient";
		public static final String QTY = "c_Qty";
		//Extra Column names for the view
		public static final String QTY_INV = "c_Qty_Inventory";
		public static final String INGREDIENT = Inventory.INGREDIENT;
		public static final String UNIT = Inventory.UNIT;
		//Triggers
		private static final String TRIG_UPDATE = "t_" + TABLE + "_update";
		private static final String TRIG_INSERT = "t_" + TABLE + "_insert";
		private static final String TRIG_DELETE = "t_" + TABLE + "_delete";	
		//Column index numbers
		public static final int ID_RECIPE_INDEX = 1;
		public static final int ID_INGREDIENT_INDEX = 2;
		public static final int INGREDIENT_INDEX = 3;
		public static final int QTY_INDEX = 4;
		public static final int QTY_INV_INDEX = 5;
		public static final int UNIT_INDEX = 6;
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = ID_RECIPE + " ASC";
	}

	public static final class ShoppingList implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/shoppinglist");
		public static final String TABLE = "v_ShoppingList";
		//Column names
		public static final String INGREDIENT = Inventory.INGREDIENT;
		public static final String QTY = "c_Qty";
		public static final String QTY_BASKET = Inventory.QTY_BASKET;
		public static final String UNIT = Inventory.UNIT;
		//Triggers
		private static final String TRIG_UPDATE = "t_" + TABLE + "_update";
		private static final String TRIG_INSERT = "t_" + TABLE + "_insert";
		//Column index numbers
		public static final int INGREDIENT_INDEX = 1;
		public static final int QTY_INDEX = 2;
		public static final int QTY_BASKET_INDEX = 3;
		public static final int UNIT_INDEX = 4;
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = INGREDIENT + " ASC";
	}

	//Internal used class only
	private static final class MealPlanner implements BaseColumns {
		public static final String TABLE = "t_MealPlanner";
		//Column names
		public static final String ID_RECIPE = "c_ID_Recipe";
		public static final String SERVES = "c_Serves";
		//Triggers
		private static final String TRIG_UPDATE = "t_" + TABLE + "_update";
		private static final String TRIG_INSERT = "t_" + TABLE + "_insert";
		private static final String TRIG_DELETE = "t_" + TABLE + "_delete";	
		//Column index numbers
		public static final int ID_RECIPE_INDEX = 1;
		public static final int SERVES_INDEX = 2;
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = ID_RECIPE + " ASC";
	}
	
	public static final class MealPlannerView implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/mealplanner");
		public static final String TABLE = "v_MealPlanner";
		//Column names
		public static final String ID_RECIPE = "c_ID_Recipe";
		public static final String RECIPE = "c_Recipe";
		public static final String SERVES = "c_Serves";
		public static final String RECIPE_SERVES = "c_Recipe_Serves";
		//Triggers
		private static final String TRIG_UPDATE = "t_" + TABLE + "_update";
		private static final String TRIG_INSERT = "t_" + TABLE + "_insert";
		private static final String TRIG_DELETE = "t_" + TABLE + "_delete";	
		//Column index numbers
		public static final int ID_RECIPE_INDEX = 1;
		public static final int RECIPE_INDEX = 2;
		public static final int SERVES_INDEX = 3;
		public static final int RECIPE_SERVES_INDEX = 4;
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = RECIPE + " ASC";
	}

	public static final class MealIngredientsView implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/mealplanneringredientsview");
		public static final String TABLE = "v_MealPlannerIngredients";
		//Column names
		public static final String ID_INGREDIENT = "c_ID_Ingredient";
		public static final String INGREDIENT = "c_Ingredient";
		public static final String QTY = "c_Qty";
		//Column index numbers
		public static final int ID_INGREDIENT_INDEX = 1;
		public static final int INGREDIENT_INDEX = 2;
		public static final int QTY_INDEX = 3;
		//Default sort order
		public static final String DEFAULT_SORT_ORDER = ID_INGREDIENT + " ASC";
	}

	public static final class Transaction {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/transaction");
		public static final String TYPE = "type";
		private static Boolean locked = false;
		public static Boolean isLocked() {
			return locked;
		}
		//keys
		public static final int BEGIN = 1;
		public static final int ROLLBACK = 2;
		public static final int COMMIT = 3;
	}

	private SQLiteDatabase mDB;
	private static final UriMatcher URL_MATCHER;
	private static final String TAG = "Bites.Provider";
	private static final String DATABASE_NAME = "db_Bites";
	private static final int DATABASE_VERSION = 1;
	private static final int INVENTORY = 1;
	private static final int INVENTORY_ID = 2;
	private static final int RECIPES = 3;
	private static final int RECIPES_ID = 4;
	private static final int RECIPE_INGREDIENTS = 5;
	private static final int RECIPE_INGREDIENTS_ID = 6;
	private static final int SHOPPING_LIST = 7;
	private static final int SHOPPING_LIST_ID = 8;
	private static final int RECIPE_INGREDIENTS_VIEW = 9;
	private static final int RECIPE_INGREDIENTS_VIEW_ID = 10;
	private static final int MEAL_PLANNER = 11;
	private static final int MEAL_PLANNER_ID = 12;
	private static final int RECIPE_METHOD = 13;
	private static final int RECIPE_METHOD_ID = 14;
	private static final int TRANSACTION = 15;
	private static Resources resources;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		@Override
		public void onCreate(SQLiteDatabase db) {
			//SQL query strings for table and trigger creating are stored as resources

			//Create Tables
			db.execSQL(resources.getString(R.string.t_inventory_create));
			db.execSQL(resources.getString(R.string.t_recipes_create));
		    	db.execSQL(resources.getString(R.string.t_recipeingredients_create));
		    	db.execSQL(resources.getString(R.string.t_recipemethod_create));
		    	db.execSQL(resources.getString(R.string.t_mealplanner_create)); 

			//Create Views
			db.execSQL(resources.getString(R.string.v_recipeingredients_create));
			db.execSQL(resources.getString(R.string.v_mealplanner_create));
			db.execSQL(resources.getString(R.string.v_mealplanneringredients_create));
			db.execSQL(resources.getString(R.string.v_shoppinglist_create));
			
			//Create Triggers
			db.execSQL(resources.getString(R.string.trig_t_recipes_delete));
			db.execSQL(resources.getString(R.string.trig_v_recipeingredients_update));
			db.execSQL(resources.getString(R.string.trig_v_recipeingredients_insert));
			db.execSQL(resources.getString(R.string.trig_v_recipeingredients_delete));
			db.execSQL(resources.getString(R.string.trig_v_mealplanner_delete));
			db.execSQL(resources.getString(R.string.trig_v_mealplanner_insert));
			db.execSQL(resources.getString(R.string.trig_v_mealplanner_update));
			db.execSQL(resources.getString(R.string.trig_v_shoppinglist_update));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
			    + newVersion + ", which will destroy all old data");
		    db.execSQL(resources.getString(R.string.drop_t_inventory));
		    db.execSQL(resources.getString(R.string.drop_t_mealplanner));
		    db.execSQL(resources.getString(R.string.drop_t_recipeingredients));
		    db.execSQL(resources.getString(R.string.drop_t_recipemethod));
		    db.execSQL(resources.getString(R.string.drop_t_recipes));
		    db.execSQL(resources.getString(R.string.drop_v_mealplanner));
		    db.execSQL(resources.getString(R.string.drop_v_mealplanneringredients));
		    db.execSQL(resources.getString(R.string.drop_v_recipeingredients));
		    db.execSQL(resources.getString(R.string.drop_v_shoppinglist));
		    onCreate(db);
		}
    	}

	@Override
	public boolean onCreate() {
		//Get a resources handle 
		resources = getContext().getResources();
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		return (mDB == null) ? false : true;
	}

	@Override
	public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
		 SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        	String orderBy;

		switch (URL_MATCHER.match(url)) {
		case INVENTORY:
		    qb.setTables(Inventory.TABLE);
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? Inventory.DEFAULT_SORT_ORDER : sort;
		    break;

		case INVENTORY_ID:
		    qb.setTables(Inventory.TABLE);
		    qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? Inventory.DEFAULT_SORT_ORDER : sort;
		    break;

		case RECIPES:
			qb.setTables(Recipes.TABLE);
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? Recipes.DEFAULT_SORT_ORDER : sort;
			break;
			
		case RECIPES_ID:
			qb.setTables(Recipes.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? Recipes.DEFAULT_SORT_ORDER : sort;
			break;

		case RECIPE_INGREDIENTS:
			qb.setTables(RecipeIngredients.TABLE);
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeIngredients.DEFAULT_SORT_ORDER : sort;
			break;
		
		case RECIPE_INGREDIENTS_ID:
			qb.setTables(RecipeIngredients.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeIngredients.DEFAULT_SORT_ORDER : sort;
			break;

		case RECIPE_METHOD:
			qb.setTables(RecipeMethod.TABLE);
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeMethod.DEFAULT_SORT_ORDER : sort;
			break;
		
		case RECIPE_METHOD_ID:
			qb.setTables(RecipeMethod.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeMethod.DEFAULT_SORT_ORDER : sort;
			break;

		case RECIPE_INGREDIENTS_VIEW:
			qb.setTables(RecipeIngredientsView.TABLE);
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeIngredientsView.DEFAULT_SORT_ORDER : sort;
			break;
		
		case RECIPE_INGREDIENTS_VIEW_ID:
			qb.setTables(RecipeIngredientsView.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? RecipeIngredientsView.DEFAULT_SORT_ORDER : sort;
			break;

		case SHOPPING_LIST:
			qb.setTables(ShoppingList.TABLE);
			orderBy = (TextUtils.isEmpty(sort)) ? ShoppingList.DEFAULT_SORT_ORDER : sort;
			break;

		case SHOPPING_LIST_ID:
			qb.setTables(ShoppingList.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? ShoppingList.DEFAULT_SORT_ORDER : sort;
			break;

		case MEAL_PLANNER:
			qb.setTables(MealPlannerView.TABLE);
			orderBy = (TextUtils.isEmpty(sort)) ? MealPlannerView.DEFAULT_SORT_ORDER : sort;
			break;

		case MEAL_PLANNER_ID:
			qb.setTables(MealPlannerView.TABLE);
		    	qb.appendWhere("_id=" + url.getPathSegments().get(1));
		// If no sort order is specified use the default
			orderBy = (TextUtils.isEmpty(sort)) ? MealPlannerView.DEFAULT_SORT_ORDER : sort;
			break;
		default:
		    throw new IllegalArgumentException("Query: Unknown URL " + url);
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
    	public String getType(Uri url) {
		switch (URL_MATCHER.match(url)) {
		case INVENTORY:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.inventory";

		case INVENTORY_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.inventory";

		case RECIPES:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.recipes";

		case RECIPES_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.recipes";

		case RECIPE_INGREDIENTS:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.recipeingredients";

		case RECIPE_INGREDIENTS_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.recipeingredients";

		case RECIPE_METHOD:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.recipemethod";

		case RECIPE_METHOD_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.recipemethod";

		case RECIPE_INGREDIENTS_VIEW:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.recipeingredientsview";

		case RECIPE_INGREDIENTS_VIEW_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.recipeingredientsview";

		case SHOPPING_LIST:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.shoppinglist";

		case SHOPPING_LIST_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.shoppinglist";

		case MEAL_PLANNER:
		    return "vnd.android.cursor.dir/vnd.captainfanatic.mealplanner";

		case MEAL_PLANNER_ID:
		    return "vnd.android.cursor.item/vnd.captainfanatic.mealplanner";

		default:
		    throw new IllegalArgumentException("Unknown URL " + url);
		}		

	}

	@Override
    	public Uri insert(Uri url, ContentValues initialValues) {
		long rowID;
		ContentValues values;
		if (initialValues != null) {
		    values = new ContentValues(initialValues);
		} else {
		    values = new ContentValues();
		}
		switch (URL_MATCHER.match(url)){
			case INVENTORY:
				//Make sure all fields are set
				//If no ingredient name is entered use "Spam"
				if (values.containsKey(Provider.Inventory.INGREDIENT) == false) {
				    values.put(Provider.Inventory.INGREDIENT, "");
				}

				if (values.containsKey(Provider.Inventory.QTY) == false) {
				    values.put(Provider.Inventory.QTY, 0);
				}

				rowID = mDB.insert(Provider.Inventory.TABLE, Provider.Inventory.INGREDIENT, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.Inventory.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case RECIPES:

				//Make sure all fields are set
				if (values.containsKey(Provider.Recipes.RECIPE) == false) {
				    values.put(Provider.Recipes.RECIPE, "Untitled");
				}

				rowID = mDB.insert(Provider.Recipes.TABLE, Provider.Recipes.RECIPE, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.Recipes.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case RECIPE_INGREDIENTS:
				//Make sure all fields are set
				if (values.containsKey(Provider.RecipeIngredients.ID_RECIPE) == false) {
					throw new IllegalArgumentException(TAG + " No ID_RECIPE given");
				}

				if (values.containsKey(Provider.RecipeIngredients.ID_INGREDIENT) == false) {
					values.put(Provider.RecipeIngredients.ID_INGREDIENT,0);
				}

				if (values.containsKey(Provider.RecipeIngredients.QTY) == false) {
					values.put(Provider.RecipeIngredients.QTY,0);
				}

				rowID = mDB.insert(Provider.RecipeIngredients.TABLE, Provider.RecipeIngredients.ID_RECIPE, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.RecipeIngredients.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case RECIPE_INGREDIENTS_VIEW:
				//Make sure all fields are set
				if (values.containsKey(Provider.RecipeIngredientsView.ID_RECIPE) == false) {
					throw new IllegalArgumentException(TAG + " No ID_RECIPE given");
				}

				if (values.containsKey(Provider.RecipeIngredientsView.ID_INGREDIENT) == false) {
					throw new IllegalArgumentException(TAG + " No ID_INGREDIENT given");
				}

				if (values.containsKey(Provider.RecipeIngredientsView.QTY) == false) {
					values.put(Provider.RecipeIngredients.QTY,0);
				}

				//Insert into RecipeIngredients table directly rather than into the view
				//inserting into the view does not seem to return a rowID
				rowID = mDB.insert(Provider.RecipeIngredients.TABLE, Provider.RecipeIngredients.ID_RECIPE, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.RecipeIngredientsView.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case RECIPE_METHOD:
				//Make sure all fields are set
				if (values.containsKey(Provider.RecipeMethod.ID_RECIPE) == false) {
					throw new IllegalArgumentException(TAG + " No ID_RECIPE given");
				}

				rowID = mDB.insert(Provider.RecipeMethod.TABLE, Provider.RecipeMethod.ID_RECIPE, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.RecipeMethod.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case MEAL_PLANNER:
				//Make sure all fields are set
				if (values.containsKey(Provider.MealPlannerView.ID_RECIPE) == false) {
					throw new IllegalArgumentException(TAG + " No ID_RECIPE given");
				}

				if (values.containsKey(Provider.MealPlannerView.SERVES) == false) {
					values.put(Provider.MealPlannerView.SERVES,2);
				}

				//Insert into table directly rather than into the view
				//inserting into the view does not seem to return a rowID
				rowID = mDB.insert(Provider.MealPlanner.TABLE, Provider.MealPlanner.ID_RECIPE, values);
				if (rowID > 0) {
				    Uri uri = ContentUris.withAppendedId(Provider.MealPlannerView.CONTENT_URI, rowID);
				    getContext().getContentResolver().notifyChange(uri, null);
				    return uri;
				}
				break;

			case SHOPPING_LIST:
				//Make sure all fields are set
				if (values.containsKey(Provider.ShoppingList._ID) == false) {
					throw new IllegalArgumentException(TAG + " No _ID given");
				}
				rowID = mDB.insert(Provider.ShoppingList.TABLE, Provider.ShoppingList._ID, values);
			    	Uri uri = ContentUris.withAppendedId(Provider.ShoppingList.CONTENT_URI, 
									values.getAsLong(Provider.ShoppingList._ID));
			    	getContext().getContentResolver().notifyChange(uri, null);
			    	return uri;
		
			case TRANSACTION:
				int type = values.getAsInteger(Transaction.TYPE);
				switch (type)
				{
					case Transaction.BEGIN:
						Transaction.locked = true;
						mDB.execSQL("BEGIN;");
						return url;
					case Transaction.COMMIT:
						mDB.execSQL("COMMIT;");
						Transaction.locked = false;
						return url;
					case Transaction.ROLLBACK:
						mDB.execSQL("ROLLBACK;");
						Transaction.locked = false;
						return url;
					default:
						throw new IllegalArgumentException(TAG + " No transaction type give");
				}
				
				
							
			default:
			throw new IllegalArgumentException("Insert: Unknown URL " + url);
		
		}	

		throw new SQLException("Failed to insert row into " + url);
	}

	@Override
    	public int delete(Uri url, String where, String[] whereArgs) {
		int count;
		long rowId = 0;
		String segment;
		switch (URL_MATCHER.match(url)) {
			case INVENTORY:
			    	count = mDB.delete(Inventory.TABLE, where, whereArgs);
			    	break;

			case INVENTORY_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(Inventory.TABLE, "_id="
					    + segment + " AND NOT EXISTS (SELECT * FROM " + RecipeIngredients.TABLE
						+ " WHERE " + RecipeIngredients.ID_INGREDIENT + "=" + segment + ") "
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;
			
			case RECIPES:
			    	count = mDB.delete(Recipes.TABLE, where, whereArgs);
			    	break;
			
			case RECIPES_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB.delete(Recipes.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;

			case RECIPE_INGREDIENTS:
			    	count = mDB.delete(RecipeIngredients.TABLE, where, whereArgs);
			    	break;
			
			case RECIPE_INGREDIENTS_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(RecipeIngredients.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;

			case RECIPE_INGREDIENTS_VIEW:
			    	count = mDB.delete(RecipeIngredientsView.TABLE, where, whereArgs);
			    	break;

			case RECIPE_INGREDIENTS_VIEW_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(RecipeIngredientsView.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;
			
			case RECIPE_METHOD:
			    	count = mDB.delete(RecipeMethod.TABLE, where, whereArgs);
			    	break;
			
			case RECIPE_METHOD_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(RecipeMethod.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;

			case MEAL_PLANNER:
			    	count = mDB.delete(MealPlannerView.TABLE, where, whereArgs);
			    	break;

			case MEAL_PLANNER_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(MealPlannerView.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
				break;
			
			case SHOPPING_LIST:
			    	count = mDB.delete(ShoppingList.TABLE, where, whereArgs);
			    	break;
			
			case SHOPPING_LIST_ID:
			    	segment = url.getPathSegments().get(1);
			    	rowId = Long.parseLong(segment);
			    	count = mDB
				    .delete(ShoppingList.TABLE, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    	break;

			default:
		    		throw new IllegalArgumentException("Delete: Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
    	public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
		int count;
		String segment;
		switch (URL_MATCHER.match(url)) {
			case RECIPES:
			    count = mDB.update(Recipes.TABLE, values, where, whereArgs);
			    break;

			case RECIPES_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(Recipes.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case RECIPE_INGREDIENTS:
			    count = mDB.update(RecipeIngredients.TABLE, values, where, whereArgs);
			    break;

			case RECIPE_INGREDIENTS_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(RecipeIngredients.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case RECIPE_INGREDIENTS_VIEW:
			    count = mDB.update(RecipeIngredientsView.TABLE, values, where, whereArgs);
			    break;

			case RECIPE_INGREDIENTS_VIEW_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(RecipeIngredientsView.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case RECIPE_METHOD:
			    count = mDB.update(RecipeMethod.TABLE, values, where, whereArgs);
			    break;

			case RECIPE_METHOD_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(RecipeMethod.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case MEAL_PLANNER:
			    count = mDB.update(MealPlannerView.TABLE, values, where, whereArgs);
			    break;

			case MEAL_PLANNER_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(MealPlannerView.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case SHOPPING_LIST:
			    count = mDB.update(ShoppingList.TABLE, values, where, whereArgs);
			    break;

			case SHOPPING_LIST_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(ShoppingList.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;

			case INVENTORY:
			    count = mDB.update(Inventory.TABLE, values, where, whereArgs);
				break;

			case INVENTORY_ID:
			    segment = url.getPathSegments().get(1);
			    count = mDB.update(Inventory.TABLE, values, "_id="
					    + segment
					    + (!TextUtils.isEmpty(where) ? " AND (" + where
						    + ')' : ""), whereArgs);
			    break;
				
                                
			default:
			    throw new IllegalArgumentException("Update: Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "inventory", INVENTORY);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "inventory/#", INVENTORY_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipes", RECIPES);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipes/#", RECIPES_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipeingredients", RECIPE_INGREDIENTS);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipeingredients/#", RECIPE_INGREDIENTS_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipemethod", RECIPE_METHOD);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipemethod/#", RECIPE_METHOD_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "shoppinglist", SHOPPING_LIST);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "shoppinglist/#", SHOPPING_LIST_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipeingredientsview", RECIPE_INGREDIENTS_VIEW);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "recipeingredientsview/#", RECIPE_INGREDIENTS_VIEW_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "mealplanner", MEAL_PLANNER);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "mealplanner/#", MEAL_PLANNER_ID);
		URL_MATCHER.addURI("captainfanatic.bites.Provider", "transaction", TRANSACTION);
	}

}
