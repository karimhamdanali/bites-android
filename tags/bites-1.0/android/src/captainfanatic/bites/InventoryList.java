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
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.net.Uri;
import android.util.Log;

public class InventoryList extends ListActivity
{

	private static final String TAG = "Bites.InventoryList";
	private Cursor mCursor;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int EDIT_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST +2;
	private String action;
	private ContentResolver contentResolver;
	private Uri mUri;
	
	public static final String INGREDIENT_KEY = "ingredientKey";
        public static final String UNIT_KEY = "unitKey";
        public static final String QTY_KEY = "quantityKey";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
	contentResolver = getContentResolver();

	setContentView(R.layout.inventorylist);
	
	final Intent intent = getIntent();
	if (intent.getData() == null) {
		intent.setData(Provider.Inventory.CONTENT_URI);
	}

	mUri = intent.getData();
	action = intent.getAction();	

	mCursor = managedQuery(mUri, null, null, null);

	//map columns from database to columns in the list rows
	ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.inventorylist_row, mCursor,
							new String[] {Provider.Inventory.INGREDIENT, 
									Provider.Inventory.QTY, 
									Provider.Inventory.UNIT},
							new int[] {R.id.ingredient, 
									R.id.quantity, 
									R.id.unit});
	
	View headerView = getViewInflate().inflate(R.layout.inventorylist_row,null,false,null);
	headerView.setClickable(false);
	headerView.setFocusable(false);
	headerView.setFocusableInTouchMode(false);
	TextView v = (TextView)headerView.findViewById(R.id.ingredient);
	int yellow = getResources().getColor(R.color.yellow_header);
	v.setTextColor(yellow);
	v = (TextView)headerView.findViewById(R.id.quantity);
	v.setTextColor(yellow);
	v = (TextView)headerView.findViewById(R.id.unit);
	v.setTextColor(yellow);
	getListView().addHeaderView(headerView,null,false);	
	setListAdapter(adapter);

	if (mCursor.count() > 0 & mCursor != null){
		mCursor.first();
	}
    }

	@Override
	protected void onPause() {
		super.onPause();
	}

	/** Create the options menu **/
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(0, INSERT_ID, "Insert");
		menu.add(0, EDIT_ID, "Edit");
		if (mCursor.count() > 0) {
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
			case EDIT_ID:
				//Only edit if the cursor is off the header row to avoid an error
				if (getSelectedItemPosition() > 0) {
					Uri url = ContentUris.withAppendedId(mUri, getSelectedItemId());
					startActivity(new Intent(Intent.EDIT_ACTION, url));
				}
				return true;	
			case DELETE_ID:
				//use the contentresolver for custom delete method in Provider
				contentResolver.delete(Uri.withAppendedPath(mUri, 
									""+getSelectedItemId()),
									null, null);
 				return true;
		}		
		return super.onOptionsItemSelected(item);
	}	

	
	/** Hande clicking on an item in the list **/
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//Position 0 is the header row, don't respond to clicking on this
		if (position > 0) {
			if (action.equals(Intent.VIEW_ACTION)) {
				Uri url = ContentUris.withAppendedId(mUri, id);
				startActivity(new Intent(Intent.EDIT_ACTION, url));
			}
			if (action.equals(Intent.PICK_ACTION)) {
				Bundle bundle = new Bundle();
				bundle.putLong(RecipeIngredientAdd.INGREDIENT_KEY, id);
				bundle.putString(UNIT_KEY, mCursor.getString(Provider.Inventory.UNIT_INDEX));
				String ingredient = mCursor.getString(Provider.Inventory.INGREDIENT_INDEX);
				bundle.putString(QTY_KEY, mCursor.getString(Provider.Inventory.QTY_MIN_INDEX));
				setResult(RESULT_OK, ingredient, bundle);
				finish();
			}
		}
	}
	
}
