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
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.net.Uri;
import android.util.Log;

public class RecipeList extends ListActivity
{

	private static final String TAG = "Bites.RecipeList";
	public static final String RECIPE_KEY = "recipeKey";
	private Cursor mCursor;
	private static final int EDIT_ID = Menu.FIRST;
	private static final int INSERT_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private String action;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

	setContentView(R.layout.recipelist);
	
	final Intent intent = getIntent();
	if (intent.getData() == null) {
		intent.setData(Provider.Recipes.CONTENT_URI);
	}

	action = intent.getAction();	

	mCursor = managedQuery(getIntent().getData(), null, null, null);
	//map columns from database to columns in the list rows
	ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.recipelist_row, mCursor,
							new String[] {Provider.Recipes.RECIPE}, 
							new int[] {R.id.recipe}); 
	setListAdapter(adapter);

	mCursor.first();
    }

	@Override
	protected void onResume(){
		super.onResume();
	}


	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		
		menu.clear();
		menu.add(0, INSERT_ID, "Insert");
		if (mCursor.count() > 0) {
			menu.add(0, EDIT_ID, "Edit");
			menu.add(0, DELETE_ID, "Delete");
		}

		return result;
	}

	/** Handle a selection from the options menu **/
	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()){
			case INSERT_ID:
				startActivity(new Intent(Intent.INSERT_ACTION, getIntent().getData()));
				return true;
			case DELETE_ID:
				mCursor.deleteRow();
				return true;
			case EDIT_ID:
				Uri url = ContentUris.withAppendedId(getIntent().getData(), mCursor.getLong(0));
				startActivity(new Intent(Intent.EDIT_ACTION, url));
				return true;
		}		
		return super.onOptionsItemSelected(item);
	}	
	
	/** Hande clicking on an item in the list **/
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (action.equals(Intent.VIEW_ACTION)) {
			Uri url = ContentUris.withAppendedId(getIntent().getData(), id);
			startActivity(new Intent(Intent.VIEW_ACTION, url));
		}
		if (action.equals(Intent.PICK_ACTION)) {
                        Bundle bundle = new Bundle();
                        bundle.putLong(RecipeList.RECIPE_KEY, id);
                        String recipe = mCursor.getString(Provider.Recipes.RECIPE_INDEX);
                        setResult(RESULT_OK, recipe, bundle);
                        finish();
                }
	}
}
