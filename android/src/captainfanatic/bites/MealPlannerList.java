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

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.net.Uri;
import android.util.Log;

public class MealPlannerList extends ListActivity
{

	private static final String TAG = "Bites.MealPlannerList";
	private static final int REQ_MAKE_MEAL = 0;
	private Cursor mCursor;
	private ContentResolver contentResolver;
	private Intent intent;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int EDIT_ID = Menu.FIRST + 1;
	private static final int MAKE_MEAL_ID = Menu.FIRST +2;
	private static final int DELETE_ID = Menu.FIRST +3;
	private static final int DISCARD_ID = Menu.FIRST +4;

	private Boolean mGotLock = false;
	private Boolean mRollBack = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

	setContentView(R.layout.mealplannerlist);
	contentResolver = getContentResolver();
	//Begin a transaction if one has not been started already (allows rollback)
	if (!Provider.Transaction.isLocked()) {
		ContentValues begin = new ContentValues();
		begin.put(Provider.Transaction.TYPE, Provider.Transaction.BEGIN);
		contentResolver.insert(Provider.Transaction.CONTENT_URI, begin);	
		mGotLock = true;
	}
	else {
		mGotLock = false;
	}

	intent = getIntent();
	if (intent.getData() == null) {
		intent.setData(Provider.Recipes.CONTENT_URI);
	}

	String action = intent.getAction();	
	refresh();
    }

	@Override
	protected void onResume(){
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mGotLock && isFinishing()) {
			ContentValues transaction = new ContentValues();
			if (mRollBack) {
				transaction.put(Provider.Transaction.TYPE, Provider.Transaction.ROLLBACK);
			}
			else {
				transaction.put(Provider.Transaction.TYPE, Provider.Transaction.COMMIT);
			}
			contentResolver.insert(Provider.Transaction.CONTENT_URI, transaction);
		}
	}


	private void refresh() {
		mCursor = managedQuery(getIntent().getData(), null, null, null);
		//map columns from database to columns in the list rows
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.mealplannerlist_row, mCursor,
								new String[] {Provider.MealPlannerView.RECIPE,
										Provider.MealPlannerView.SERVES}, 
								new int[] {R.id.recipe, R.id.serves}); 
		setListAdapter(adapter);
		mCursor.first();
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		
		menu.clear();
		menu.add(0, INSERT_ID, "Insert");
		menu.add(0, DISCARD_ID, "Discard");
		if (mCursor.count() > 0) {
			menu.add(0, DELETE_ID, "Delete");
			menu.add(0, EDIT_ID, "Edit");
			menu.add(0, MAKE_MEAL_ID, "Make Meal");
		}

		return result;
	}

	/** Handle results returned from subActivities */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
		super.onActivityResult(requestCode, resultCode, data, extras);
		if (resultCode == RESULT_OK) {
			String rowId = extras.getString(RecipeEdit.MEAL_PLANNER_ID_KEY);	
			Uri uriDelete = Uri.withAppendedPath(intent.getData(), rowId);
			contentResolver.delete(uriDelete, null, null);
			refresh();
		}		
	}

	/** Handle a selection from the options menu **/
	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()){
			case DISCARD_ID:
				mRollBack = true;
				finish();
				return true;
			case INSERT_ID:
				startActivity(new Intent(Intent.INSERT_ACTION, getIntent().getData()));
				return true;
			case EDIT_ID:
				Uri editUri = Uri.withAppendedPath(getIntent().getData(), mCursor.getString(0));
				startActivity(new Intent(Intent.EDIT_ACTION, editUri));
				return true;
			case DELETE_ID:
				if (mCursor.deleteRow()) {
				}
				return true;
			case MAKE_MEAL_ID:
				makeMeal();
				return true;
		}		
		return super.onOptionsItemSelected(item);
	}	
	
	private void makeMeal() {
		//Call RecipeEdit for this recipe, passing the number of serves we will make
		//RecipeEdit will return RESULT_OK and the number of serves made when the meal has been made
		Uri uri = Uri.withAppendedPath(Provider.Recipes.CONTENT_URI, 
						mCursor.getString(Provider.MealPlannerView.ID_RECIPE_INDEX));
		Intent makeMealIntent = new Intent(Intent.RUN_ACTION, uri);
		makeMealIntent.putExtra(RecipeEdit.SERVES_KEY, mCursor.getString(Provider.MealPlannerView.SERVES_INDEX));
		makeMealIntent.putExtra(RecipeEdit.MEAL_PLANNER_ID_KEY, mCursor.getString(0));
		startSubActivity(makeMealIntent, REQ_MAKE_MEAL);
	}
	
	/** Hande clicking on an item in the list **/
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		makeMeal();
	}
	
}
