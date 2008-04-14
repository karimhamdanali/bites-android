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

public class Provider extends ContentProvider
{
	//Convenience classes representing each type of content
	public static final class Inventory implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://captainfanatic.bites.Provider/inventory");
		public static final String TABLE = "t_Inventory";
		//Column names
		public static final String INGREDIENT = "c_Ingredient";
		public static final String QTY = "c_Quantity";
		public static final String UNIT = "c_Unit";
		public static final String QTY_MIN = "c_QuantityMin";
		public static final String QTY_BASKET = "c_QuantityBasket";
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

	private static class DatabaseHelper extends SQLiteOpenHelper {

		@Override
		public void onCreate(SQLiteDatabase db) {
			//Create Tables
		    	db.execSQL("CREATE TABLE " + Inventory.TABLE + " (_id INTEGER PRIMARY KEY,"
			    	 + Inventory.INGREDIENT + " TEXT," + Inventory.QTY + " INTEGER,"
				 + Inventory.UNIT + " TEXT," + Inventory.QTY_MIN + " INTEGER DEFAULT 0,"
				 + Inventory.QTY_BASKET + " INTEGER DEFAULT 0" + ");");

			db.execSQL("CREATE TABLE " + Recipes.TABLE + " (_id INTEGER PRIMARY KEY,"
				+ Recipes.RECIPE + " TEXT," + Recipes.SERVES + " INTEGER" + ");");

		    	db.execSQL("CREATE TABLE " + RecipeIngredients.TABLE + " (_id INTEGER PRIMARY KEY,"
			    	+ RecipeIngredients.ID_RECIPE + " INTEGER," 
			    	+ RecipeIngredients.QTY + " INTEGER," 
				+ RecipeIngredients.ID_INGREDIENT + " INTEGER" + ");");

		    	db.execSQL("CREATE TABLE " + RecipeMethod.TABLE + " (_id INTEGER PRIMARY KEY,"
			    	+ RecipeMethod.ID_RECIPE + " INTEGER," 
			    	+ RecipeMethod.STEP + " INTEGER," 
				+ RecipeMethod.METHOD + " TEXT" + ");");

		    	db.execSQL("CREATE TABLE " + MealPlanner.TABLE + " (_id INTEGER PRIMARY KEY,"
			    	+ MealPlanner.ID_RECIPE  + " INTEGER," 
			    	+ MealPlanner.SERVES + " INTEGER" + ");"); 

			//Create Views
			 db.execSQL("CREATE VIEW " + RecipeIngredientsView.TABLE + " AS SELECT "
				+ RecipeIngredients.TABLE + "._id AS _id," 
				+ RecipeIngredients.TABLE + "." + RecipeIngredients.ID_RECIPE 
				+ " AS " + RecipeIngredientsView.ID_RECIPE + ","
				+ RecipeIngredients.TABLE + "." + RecipeIngredients.ID_INGREDIENT 
				+ " AS " + RecipeIngredientsView.ID_INGREDIENT + ","
				+ Inventory.TABLE + "." + Inventory.INGREDIENT + " AS " + RecipeIngredientsView.INGREDIENT + ","
				+ RecipeIngredients.TABLE + "." + RecipeIngredients.QTY 
				+ " AS " + RecipeIngredientsView.QTY + ","
				+ Inventory.TABLE + "." + Inventory.QTY + " AS " + RecipeIngredientsView.QTY_INV + ","
				+ Inventory.TABLE + "." + Inventory.UNIT + " AS " + RecipeIngredientsView.UNIT
				+ " FROM " + RecipeIngredients.TABLE
				+ " INNER JOIN " + Inventory.TABLE
				+ " ON " + RecipeIngredients.ID_INGREDIENT + "="
				+ Inventory.TABLE + "." + Inventory._ID + ";");

			 db.execSQL("CREATE VIEW " + MealPlannerView.TABLE + " AS SELECT "
				+ MealPlanner.TABLE + "._id AS _id," 
				+ MealPlanner.TABLE + "." + MealPlanner.ID_RECIPE 
				+ " AS " + MealPlannerView.ID_RECIPE + ","
				+ Recipes.TABLE + "." + Recipes.RECIPE 
				+ " AS " + MealPlannerView.RECIPE + ","
				+ MealPlanner.TABLE + "." + MealPlanner.SERVES + " AS " + MealPlannerView.SERVES + ","
				+ Recipes.TABLE + "." + Recipes.SERVES + " AS " + MealPlannerView.RECIPE_SERVES 
				+ " FROM " + MealPlanner.TABLE
				+ " INNER JOIN " + Recipes.TABLE
				+ " ON " + MealPlanner.ID_RECIPE + "="
				+ Recipes.TABLE + "." + Recipes._ID + ";");

			db.execSQL("CREATE VIEW " + MealIngredientsView.TABLE + " AS SELECT "
				+ Inventory.TABLE + "." + Inventory._ID + " AS " + MealIngredientsView.ID_INGREDIENT 
				+ "," + Inventory.TABLE + "." + Inventory.INGREDIENT + " AS " + MealIngredientsView.INGREDIENT
				+ ",SUM((" + MealPlanner.TABLE + "." + MealPlanner.SERVES + "+" + Recipes.TABLE +"."+Recipes.SERVES+"-1)/"
				+ Recipes.TABLE + "." + Recipes.SERVES + "*" + RecipeIngredients.TABLE + "." + RecipeIngredients.QTY 
				+ ") AS " + MealIngredientsView.QTY + " FROM " + Inventory.TABLE + " JOIN " + RecipeIngredients.TABLE + " ON " 
				+ Inventory.TABLE + "." + Inventory._ID + "=" + RecipeIngredients.TABLE + "." 
				+ RecipeIngredients.ID_INGREDIENT + " JOIN " + Recipes.TABLE + " ON " + RecipeIngredients.TABLE + "."
				+ RecipeIngredients.ID_RECIPE + "=" + Recipes.TABLE + "." + Recipes._ID + " JOIN " 
				+ MealPlanner.TABLE + " ON " + Recipes.TABLE + "." + Recipes._ID + "=" 
				+ MealPlanner.TABLE + "." + MealPlanner.ID_RECIPE + " GROUP BY " 
				+ Inventory.TABLE + "." + Inventory._ID + ";");

			db.execSQL("CREATE VIEW " + ShoppingList.TABLE + " AS SELECT "
				+ Inventory.TABLE + "." + Inventory._ID + " AS " + ShoppingList._ID + ","
				+ Inventory.TABLE + "." + Inventory.INGREDIENT + " AS " + ShoppingList.INGREDIENT + "," 
				+ "CASE WHEN " + MealIngredientsView.TABLE + "." + MealIngredientsView.QTY + " IS NULL THEN "
				+ Inventory.TABLE + "." + Inventory.QTY_MIN + "-" + Inventory.TABLE + "." + Inventory.QTY
				+ " ELSE " + MealIngredientsView.TABLE + "." + MealIngredientsView.QTY + "-" 
				+ Inventory.TABLE + "." + Inventory.QTY + "+" + Inventory.TABLE + "." + Inventory.QTY_MIN
				+ " END AS " + ShoppingList.QTY + "," 
				+ Inventory.TABLE + "." + Inventory.QTY_BASKET
				+ " AS " + ShoppingList.QTY_BASKET + "," + Inventory.TABLE + "." + Inventory.UNIT 
				+ " AS " + ShoppingList.UNIT + " FROM " + Inventory.TABLE + " LEFT JOIN "
				+ MealIngredientsView.TABLE + " ON " + Inventory.TABLE + "." + Inventory._ID
				+ "=" + MealIngredientsView.TABLE + "." + MealIngredientsView.ID_INGREDIENT 
				+ " WHERE " + Inventory.TABLE + "." + Inventory.QTY_MIN + ">" 
				+ Inventory.TABLE + "." + Inventory.QTY + " OR " + MealIngredientsView.TABLE + "."
				+ MealIngredientsView.QTY + " IS NOT NULL AND " + MealIngredientsView.TABLE + "." 
				+ MealIngredientsView.QTY + "> (" + Inventory.TABLE + "." + Inventory.QTY + "-"
				+ Inventory.TABLE + "." + Inventory.QTY_MIN + ");");
			
			//Create Triggers
			//A trigger to delete the associations between a recipe and its ingredients after it is deleted
			db.execSQL("CREATE TRIGGER t_" + Recipes.TABLE + "_delete "
				+ "AFTER DELETE ON " + Recipes.TABLE + " "
				+ "BEGIN "
				+ "DELETE FROM " + RecipeIngredients.TABLE + " "
				+ "WHERE " + RecipeIngredients.ID_RECIPE + "=old._id; "
				+ "DELETE FROM " + RecipeMethod.TABLE + " "
				+ "WHERE " + RecipeMethod.ID_RECIPE + "=old._id; "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + RecipeIngredientsView.TRIG_UPDATE + " "
				+ "INSTEAD OF UPDATE ON " + RecipeIngredientsView.TABLE + " "
				+ "BEGIN "
				+ "UPDATE " + RecipeIngredients.TABLE + " "
				+ "SET " + RecipeIngredients.ID_RECIPE + "=new." + RecipeIngredientsView.ID_RECIPE + ","
				+ RecipeIngredients.ID_INGREDIENT + "=new." + RecipeIngredientsView.ID_INGREDIENT + ","
				+ RecipeIngredients.QTY + "=new." + RecipeIngredientsView.QTY + " "
				+ "WHERE _id=old._id; "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + RecipeIngredientsView.TRIG_INSERT + " "
				+ "INSTEAD OF INSERT ON " + RecipeIngredientsView.TABLE + " "
				+ "BEGIN "
				+ "INSERT INTO " + RecipeIngredients.TABLE + " "
				+ "(" + RecipeIngredients.ID_RECIPE + "," + RecipeIngredients.QTY + "," 
				+ RecipeIngredients.ID_INGREDIENT + ") "
				+ "VALUES (new." + RecipeIngredientsView.ID_RECIPE + ","
				+ "new." + RecipeIngredientsView.QTY + ","
				+ "new." + RecipeIngredientsView.ID_INGREDIENT + "); "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + RecipeIngredientsView.TRIG_DELETE +" "
				+ "INSTEAD OF DELETE ON " + RecipeIngredientsView.TABLE + " "
				+ "BEGIN "
				+ "DELETE FROM " + RecipeIngredients.TABLE + " "
				+ "WHERE _id=old._id; "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + MealPlannerView.TRIG_DELETE +" "
				+ "INSTEAD OF DELETE ON " + MealPlannerView.TABLE + " "
				+ "BEGIN "
				+ "DELETE FROM " + MealPlanner.TABLE + " "
				+ "WHERE _id=old._id; "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + MealPlannerView.TRIG_INSERT + " "
				+ "INSTEAD OF INSERT ON " + MealPlannerView.TABLE + " "
				+ "BEGIN "
				+ "INSERT INTO " + MealPlanner.TABLE + " "
				+ "(" + MealPlanner.ID_RECIPE + "," + MealPlanner.SERVES + ") " 
				+ "VALUES (new." + MealPlannerView.ID_RECIPE + ","
				+ "new." + MealPlannerView.SERVES + "); "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + MealPlannerView.TRIG_UPDATE + " "
				+ "INSTEAD OF UPDATE ON " + MealPlannerView.TABLE + " "
				+ "BEGIN "
				+ "UPDATE " + MealPlanner.TABLE + " "
				+ "SET " + MealPlanner.ID_RECIPE + "=new." + MealPlannerView.ID_RECIPE + ","
				+ MealPlanner.SERVES + "=new." + MealPlannerView.SERVES + " "
				+ "WHERE _id=old._id; "
				+ "END;");

			db.execSQL("CREATE TRIGGER " + ShoppingList.TRIG_UPDATE + " "
				+ "INSTEAD OF UPDATE ON " + ShoppingList.TABLE + " "
				+ "BEGIN "
				+ "UPDATE " + Inventory.TABLE + " "
				+ "SET " + Inventory.QTY_BASKET + "=new." + ShoppingList.QTY_BASKET + " "
				+ "WHERE _id=old._id; "
				+ "END;");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
			    + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + Inventory.TABLE + ";");
		    db.execSQL("DROP TABLE IF EXISTS " + Recipes.TABLE+ ";");
		    db.execSQL("DROP TABLE IF EXISTS " + RecipeIngredients.TABLE + ";");
		    db.execSQL("DROP TABLE IF EXISTS " + RecipeMethod.TABLE + ";");
		    db.execSQL("DROP TABLE IF EXISTS " + MealPlanner.TABLE + ";");
		    db.execSQL("DROP VIEW IF EXISTS " + RecipeIngredientsView.TABLE + ";");
		    db.execSQL("DROP VIEW IF EXISTS " + MealPlannerView.TABLE + ";");
		    db.execSQL("DROP VIEW IF EXISTS " + MealIngredientsView.TABLE + ";");
		    db.execSQL("DROP VIEW IF EXISTS " + ShoppingList.TABLE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + RecipeIngredientsView.TRIG_UPDATE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + RecipeIngredientsView.TRIG_DELETE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + RecipeIngredientsView.TRIG_INSERT + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + Recipes.TRIG_DELETE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + MealPlannerView.TRIG_INSERT + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + MealPlannerView.TRIG_UPDATE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + MealPlannerView.TRIG_DELETE + ";");
		    db.execSQL("DROP TRIGGER IF EXISTS " + ShoppingList.TRIG_UPDATE + ";");
		    onCreate(db);
		}
    	}

	@Override
	public boolean onCreate() {
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
			
			//TODO: add entry for more providers as they are needed
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
	}

}
