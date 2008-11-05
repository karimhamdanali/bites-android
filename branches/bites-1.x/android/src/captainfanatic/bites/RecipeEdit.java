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

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ExpandableListAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.util.AndroidException;
import java.util.ArrayList;
import java.util.Arrays;
import android.text.TextWatcher;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

/**
 * Edit, view or make recipes.
 * When viewing, serves updates the ingredient quantities displayed in the ingredients group of the expandable list.
 * When "make recipe" is selected, the quantity of the ingredients used is subtracted from the inventory quantities.
 * When making a recipe a bundle extra using SERVES_KEY can be passed in for the initial number of serves to make.
 *
 * If the activity is called with an "RUN_ACTION" intent action, it will return a result OK when a recipe has been made.
 * This was designed for use with the Meal Planner activity, letting it know when a meal has been made so it can be deleted
 * from the meal planner list.
 *
 * @author Ben Caldwell
 * @version 1.1
 */
public class RecipeEdit extends ExpandableListActivity
{
	private static final String TAG = "Bites.RecipeEdit";

	private int mState;
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;
	private static final int STATE_VIEW = 3;

	private boolean mMethodExpanded;
	private boolean mIngredientsExpanded;
	private boolean mSetResult;
	private boolean mRollback;
	private boolean mGotLock;

	public static final String RECIPE_ID_KEY = "recipeId";
	public static final String SERVES_KEY = "serves";
	public static final String MEAL_PLANNER_ID_KEY = "mealPlannerId";

	private static final int MAKE_MEAL_ID = Menu.FIRST;	
	private static final int ADD_ING_ID = Menu.FIRST + 1;	
	private static final int DEL_ING_ID = Menu.FIRST + 2;
	private static final int DISCARD_ID = Menu.FIRST + 3;
	private static final int ADD_METH_ID = Menu.FIRST + 4;
	private static final int DEL_METH_ID = Menu.FIRST + 5;

	private Uri mUriRecipe;	
	private Uri mUriIngredients;	
	private Uri mUriMethod;	
	private ContentResolver contentResolver;
	private Cursor mCursorRecipe;
	private Cursor mCursorMethod;
	private Cursor mCursorIngredients;

	private TextView mRecipe;
	private EditText mServes;

	private String mOriginalRecipe;
	private String mOriginalMethod;

	private String mRecipeId;
	private String mMealPlannerId;

	private int mServesMultiplier;
	private int mRecipeServes;

	private ExpandableListAdapter adapter;

    	/** Called when the activity is first created. */
    	@Override
    	public void onCreate(Bundle icicle)
    	{
		super.onCreate(icicle);
Log.d(TAG,"onCreate");
		final Intent intent = getIntent();
		String action = intent.getAction();
		contentResolver = getContentResolver();
		
		//Start a transaction with Provider, allows rollback
		if (!Provider.Transaction.isLocked()) {
			ContentValues begin = new ContentValues();
			begin.put(Provider.Transaction.TYPE, Provider.Transaction.BEGIN);
			contentResolver.insert(Provider.Transaction.CONTENT_URI, begin);
			mGotLock = true;
		}
		else {
			mGotLock = false;
		}

		if (intent.getData() == null) {
			intent.setData(Provider.Recipes.CONTENT_URI);
		}

		if (action.equals(Intent.INSERT_ACTION)) {
			mState = STATE_INSERT;
			mUriRecipe = contentResolver.insert(intent.getData(), null);
			mSetResult = false;
		}
		if (action.equals(Intent.EDIT_ACTION)) {
			mState = STATE_EDIT;
			mUriRecipe = intent.getData();
			mSetResult = false;
		}
		if (action.equals(Intent.VIEW_ACTION)) {
			mState = STATE_VIEW;
			mUriRecipe = intent.getData();
			mSetResult = false;
		}
		if (action.equals(Intent.RUN_ACTION)) {
			mState = STATE_VIEW;
			mSetResult = true;
			mUriRecipe = intent.getData();
		}


		if (mState == STATE_VIEW) {
			setContentView(R.layout.recipemake);
		}
		else {
			setContentView(R.layout.recipeedit);
		}
		mUriIngredients = Provider.RecipeIngredientsView.CONTENT_URI;
		mUriMethod = Provider.RecipeMethod.CONTENT_URI;

		mCursorRecipe = managedQuery(mUriRecipe, null, null, null);

		if (mCursorRecipe != null) {
			mCursorRecipe.first();
			mRecipeId = mCursorRecipe.getString(0);
			mRecipeServes = mCursorRecipe.getInt(Provider.Recipes.SERVES_INDEX);
		}

		mCursorIngredients = managedQuery(mUriIngredients, null, 
							Provider.RecipeIngredientsView.ID_RECIPE + "=" + mRecipeId, 
							null, null);

		mCursorMethod = managedQuery(mUriMethod, null, Provider.RecipeMethod.ID_RECIPE + "=" + mRecipeId, null, null);

		refresh();

		if (mState == STATE_VIEW) {
			mRecipe = (TextView)findViewById(R.id.recipe);
		}	
		else{
			mRecipe = (EditText)findViewById(R.id.recipe);
		}
		mServes = (EditText)findViewById(R.id.serves);
		mServes.addTextChangedListener(new ServesWatcher());

		Bundle extras = intent.getExtras();
		String serves = null;
		if (mState == STATE_VIEW && extras != null)  {
			serves = extras.getString(SERVES_KEY);
			mMealPlannerId = extras.getString(MEAL_PLANNER_ID_KEY);
		}
		mServes.setText(serves != null ? serves : Integer.toString(mRecipeServes));
	}
	
	/*
	* Refresh the ExpandableListAdapter when ingredient quantities have changed etc.
	*/
	private void refresh() {
		boolean methodWasExpanded = mMethodExpanded;
		boolean ingredientsWasExpanded = mIngredientsExpanded;
		adapter = new RecipeAdapter(mCursorMethod, mCursorIngredients);
		setListAdapter(adapter);
		if (methodWasExpanded) {
			getExpandableListView().expandGroup(RecipeAdapter.METHOD_ID);
		}
		if (ingredientsWasExpanded) {
			getExpandableListView().expandGroup(RecipeAdapter.INGREDIENTS_ID);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		menu.clear();
		//Only add method/ingredient modification options if these groups are expanded - keeps the screen menu smaller
		if (mState == STATE_EDIT || mState == STATE_INSERT) {
			if (mCursorMethod.count() < 1 || mMethodExpanded) {
				menu.add(0, ADD_METH_ID, "Add Method Step");
			}
			if (mCursorMethod.count() > 0 && mMethodExpanded) {
				menu.add(0, DEL_METH_ID, "Delete Method Step");
			}
			if (mCursorIngredients.count() < 1 || mIngredientsExpanded) {
				menu.add(0, ADD_ING_ID, "Add Ingredient");
			}
			if (mCursorIngredients.count() > 0 && mIngredientsExpanded) {
				menu.add(0, DEL_ING_ID, "Delete Ingredient");
			}
			menu.add(0, DISCARD_ID, "Discard Changes");
		}
		if (mState == STATE_VIEW) {	
			menu.add(0, MAKE_MEAL_ID, "Make Meal");
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
			case MAKE_MEAL_ID:
				Cursor cInventory;
				Uri invUri;
				int newQty;
				String ingId;
				ContentValues invValue = new ContentValues();
				mCursorIngredients.first();

				//Loop through the ingredients list for this recipe and remove ingredients in each row from inventory
				do {
					ingId = mCursorIngredients.getString(Provider.RecipeIngredientsView.ID_INGREDIENT_INDEX);
					invUri = Uri.withAppendedPath(Provider.Inventory.CONTENT_URI, ingId); 
					cInventory = contentResolver.query(invUri,new String[] {Provider.Inventory.QTY},null,null,null);
					cInventory.first();
					newQty = cInventory.getInt(0) - mCursorIngredients.getInt(Provider.RecipeIngredientsView.QTY_INDEX) 
							* mServesMultiplier;
					invValue.put(Provider.Inventory.QTY, newQty);
					contentResolver.update(invUri, invValue, null, null);
					mCursorIngredients.next();
					
				} while (!mCursorIngredients.isAfterLast());
				cInventory.close();				

				//If this was called as a subactivity then return a suitable result
				if (mSetResult) {
					Bundle bundle = new Bundle();
					bundle.putString(MEAL_PLANNER_ID_KEY, mMealPlannerId);
					setResult(RESULT_OK, null, bundle);
					finish();
				}
				finish();
				return true;
			case ADD_ING_ID:
				Intent ingIntent = new Intent(Intent.INSERT_ACTION, Provider.RecipeIngredientsView.CONTENT_URI);
				//Add the row id of the recipe to the intent extras
				ingIntent.putExtra(RecipeIngredientAdd.RECIPE_KEY, mCursorRecipe.getLong(0));
				startActivity(ingIntent); 
				return true;
			case DEL_ING_ID:
				mCursorIngredients.deleteRow();
				refresh();
				return true;
			case ADD_METH_ID:
				Intent methIntent = new Intent(Intent.INSERT_ACTION, Provider.RecipeMethod.CONTENT_URI);
				//Add the row id of the recipe to the intent extras (used to relate the method to this recipe)
				methIntent.putExtra(RecipeMethodEdit.RECIPE_KEY, mCursorRecipe.getLong(0));
				startActivity(methIntent);
				return true;
			case DEL_METH_ID:
				mCursorMethod.deleteRow();
				refresh();
				return true;
			case DISCARD_ID:
				switch (mState) {
					case STATE_INSERT:
						mRollback = true;
						break;
					case STATE_EDIT:
						mRollback = true;
						break;
					case STATE_VIEW:
						if (mSetResult) {
							setResult(RESULT_CANCELED);
						}
						break;
				}
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, v, groupPosition, childPosition, id);
                if (mState == STATE_EDIT   | mState == STATE_INSERT) {
			Uri url;
			switch (groupPosition) {
				case RecipeAdapter.METHOD_ID:
					url = ContentUris.withAppendedId(Provider.RecipeMethod.CONTENT_URI, id);
					startActivity(new Intent(Intent.EDIT_ACTION, url));
				break;
				case RecipeAdapter.INGREDIENTS_ID:
					url = ContentUris.withAppendedId(Provider.RecipeIngredientsView.CONTENT_URI, id);
					startActivity(new Intent(Intent.EDIT_ACTION, url));
				break;
			}
                }
		return result;
        }


	@Override
	protected void onResume() {
		super.onResume();
		mCursorRecipe.first();
		mRecipe.setText(mCursorRecipe.getString(Provider.Recipes.RECIPE_INDEX));
		refresh();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Check recipe string for all whitespace and substitute "Untitled" if it is
		//(Looks much neater in the recipe list to have "Untitled" rather than a blank row)
		ContentValues updates = new ContentValues();
		updates.put(Provider.Recipes.RECIPE, mRecipe.getText().toString());
		if (mState == STATE_EDIT || mState == STATE_INSERT) {
			updates.put(Provider.Recipes.SERVES, mServes.getText().toString());
		}
		contentResolver.update(mUriRecipe, updates, null, null);

		//If this activity is finishing an edit or insert, commit the transactions since onCreate()
		//or rollback the Provider database to as it was before onCreate()
		if (isFinishing() && (mState == STATE_EDIT || mState == STATE_INSERT) && mGotLock) {
			ContentValues transEnd = new ContentValues();
			if (mRollback) {
				transEnd.put(Provider.Transaction.TYPE, Provider.Transaction.ROLLBACK);
			}
			else {
				transEnd.put(Provider.Transaction.TYPE, Provider.Transaction.COMMIT);
			}
			contentResolver.insert(Provider.Transaction.CONTENT_URI, transEnd);
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
	}

	public class RecipeAdapter extends BaseExpandableListAdapter {
		private static final int METHOD_ID = 0;
		private static final int INGREDIENTS_ID = 1;
		private Cursor mCMethod;
		private Cursor mCIngredients;
		private ArrayList mFromMethod;
		private ArrayList mToMethod;
		private ArrayList mFromIngredient;
		private ArrayList mToIngredient;

		private String[] groups = {"Method", "Ingredients"};

		RecipeAdapter(Cursor cMethod, Cursor cIngredients) {
			mCMethod = cMethod;
			mCIngredients = cIngredients;
		}

		public Object getChild(int groupPosition, int childPosition) {
			switch (groupPosition) {
				case METHOD_ID:
				//TODO: add a way to pass back a row if this is required	
					mCMethod.moveTo(childPosition);
					return mCMethod.getString(Provider.RecipeMethod.METHOD_INDEX);
				case INGREDIENTS_ID:
					mCIngredients.moveTo(childPosition);
					return mCIngredients.getString(Provider.RecipeIngredientsView.INGREDIENT_INDEX);
				//TODO: add a way to pass back a row if this is required
				default:
					return 0;
			}
		}

		public long getChildId(int groupPosition, int childPosition) {
			switch (groupPosition) {
				case METHOD_ID:
					//Move to the childposition
					mCMethod.moveTo(childPosition);
					//get the "_id" field from the table
					return mCMethod.getLong(0);
				case INGREDIENTS_ID:
					//Move to the childposition
					mCIngredients.moveTo(childPosition);
					//get the "_id" field from the table
					return mCIngredients.getLong(0);
				default:
					return 0;
			}
		}

		public int getChildrenCount(int groupPosition) {
			switch (groupPosition) {
				case METHOD_ID:
					return mCMethod.count();
				case INGREDIENTS_ID:
					return mCIngredients.count();
				default:
					return 0;
			}
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewInflate viewInflate = getViewInflate();
			LinearLayout layout;
			switch (groupPosition) {
				case METHOD_ID:
					mCMethod.moveTo(childPosition);
					layout = (LinearLayout)viewInflate.inflate(R.layout.recipeedit_method_row, null, false, null);
					TextView stepNumber = (TextView)layout.findViewById(R.id.step);
					stepNumber.setText(mCMethod.getString(Provider.RecipeMethod.STEP_INDEX));
					TextView method = (TextView)layout.findViewById(R.id.methodtext);
					method.setText(mCMethod.getString(Provider.RecipeMethod.METHOD_INDEX));
					return layout;
				case INGREDIENTS_ID:
					mCIngredients.moveTo(childPosition);
					layout = (LinearLayout)viewInflate.inflate(R.layout.recipeedit_ingredient_row, 
												null, false, null);
					TextView ingredient = (TextView)layout.findViewById(R.id.ingredient);
					ingredient.setText(mCIngredients.getString(Provider.RecipeIngredientsView.INGREDIENT_INDEX));
					TextView quantity = (TextView)layout.findViewById(R.id.quantity);
					int qty = mCIngredients.getInt(Provider.RecipeIngredientsView.QTY_INDEX);
					int qtyInv = mCIngredients.getInt(Provider.RecipeIngredientsView.QTY_INV_INDEX);
					qty = qty * mServesMultiplier;
					quantity.setText(Integer.toString(qty));
					TextView unit = (TextView)layout.findViewById(R.id.unit);
					unit.setText(mCIngredients.getString(Provider.RecipeIngredientsView.UNIT_INDEX));

					/*Highlight the ingredient row if we are making a recipe and don't 
 * 					have sufficient quantity of this ingredient */
					if  (qty>qtyInv && mState == STATE_VIEW) {
						int color = android.graphics.Color.RED;
						Spannable str = (Spannable)quantity.getText();
						str.setSpan(new ForegroundColorSpan(color), 0, str.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						str = (Spannable)ingredient.getText();
						str.setSpan(new ForegroundColorSpan(color), 0, str.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						str = (Spannable)unit.getText();
						str.setSpan(new ForegroundColorSpan(color), 0, str.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					return layout;
				default:
					//Invalid view id requested
					throw new ViewInflate.InflateException("Invalid Group View id");
			}
		}

		public Object getGroup(int groupPosition) {
		    return groups[groupPosition];
		}

		public int getGroupCount() {
		    return groups.length;
		}

		public long getGroupId(int groupPosition) {
		    return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,ViewGroup parent) {
			ViewInflate viewInflate = getViewInflate();
			TextView textView;
			switch (groupPosition) {
				case METHOD_ID:
					textView = (TextView)viewInflate.inflate(R.layout.recipeedit_method, null, false, null);
					return textView;
				case INGREDIENTS_ID:
					textView = (TextView)viewInflate.inflate(R.layout.recipeedit_ingredientlist, 
												null, false, null);
					return textView;
				default:
					//Invalid view id requested
					throw new ViewInflate.InflateException("Invalid Group View id");
			}
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
		    return true;
		}

		public boolean stableIds() {
		    return true;
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			super.onGroupExpanded(groupPosition);
			switch (groupPosition) {
				case METHOD_ID:
					mMethodExpanded = true;
					break;
				case INGREDIENTS_ID:
					mIngredientsExpanded = true;
					break; 	
			}
		}

		@Override
		public void onGroupCollapsed(int groupPosition) {
			super.onGroupCollapsed(groupPosition);
			switch (groupPosition) {
				case METHOD_ID:
					mMethodExpanded = false;
					break;
				case INGREDIENTS_ID:
					mIngredientsExpanded = false;
					break; 	
			}
		}
	}


	/*
	 *Listens for text changed events from the "Serves" EditText.
	 *
	 * @author Ben Caldwell
	 * @version 1.1
	 */
	public class ServesWatcher implements TextWatcher {
		/*
		 * Unused, required by TextWatcher interface
		 */
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		/*
		 * Updates mServesMultiplier in the parent class to the correct value when "Serves" EditText has changed
		 */
		public void onTextChanged(CharSequence s, int start, int count, int after) {
			int serves;
			try {
				serves = Integer.parseInt(mServes.getText().toString());
			}
			catch (NumberFormatException e) {
				serves = 1;
			}
			//If mRecipeServes has not been set yet it will be 0, replace with 1 to prevent /0 error
			mServesMultiplier = ( serves + mRecipeServes -1) / (mRecipeServes<=0 ? 1 : mRecipeServes);
			refresh();
		}
	}

}
