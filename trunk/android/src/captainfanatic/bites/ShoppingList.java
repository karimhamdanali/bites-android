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
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.text.style.ForegroundColorSpan;

public class ShoppingList extends ListActivity
{

	//Implement SimpleCursorAdapter.ViewBinder to override setting view item text from the cursor 
	//and allow for striking out items that have been picked
	public class ShoppingListBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			int qty = cursor.getInt(Provider.ShoppingList.QTY_INDEX);
			int basket = cursor.getInt(Provider.ShoppingList.QTY_BASKET_INDEX);
			if (qty <=basket) {
				//Ingredient is in trolley so strikethrough the item on the list
				TextView textView = (TextView)view;
				textView.setText(cursor.getString(columnIndex));
				Spannable str = (Spannable) textView.getText();
				str.setSpan(new StrikethroughSpan(), 0, str.length(), 
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				int color = android.graphics.Color.DKGRAY;				
				str.setSpan(new ForegroundColorSpan(color), 0, str.length(), 
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				//Binding was handled here so return true
				return true;
			}
			//If this method returns false, SimpleCursorAdapter will handle binding itself
			return false;
		}
	}	

	private static final String TAG = "Bites.ShoppingList";
	private Uri mUri;
	private ContentResolver contentResolver;
	private Intent intent;
	private Cursor mCursor;
	private Boolean mGotLock = false;
	private Boolean mRollBack = false;
	
	private static final int DISCARD_ID = Menu.FIRST;
	private static final int FINISH_SHOPPING_ID = Menu.FIRST + 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
	contentResolver = getContentResolver();
	if (!Provider.Transaction.isLocked()) {
		ContentValues begin = new ContentValues();
		begin.put(Provider.Transaction.TYPE, Provider.Transaction.BEGIN);
		contentResolver.insert(Provider.Transaction.CONTENT_URI,begin);
		mGotLock = true;
	}
	else {
		mGotLock = false;
	}
        setContentView(R.layout.shoppinglist);
	View headerView = getViewInflate().inflate(R.layout.shoppinglist_row,null,false,null);
	TextView v = (TextView)headerView.findViewById(R.id.ingredient);
        int yellow = getResources().getColor(R.color.yellow_header);
        v.setTextColor(yellow);
        v = (TextView)headerView.findViewById(R.id.qty);
        v.setTextColor(yellow);
        v = (TextView)headerView.findViewById(R.id.unit);
        v.setTextColor(yellow);	
	getListView().addHeaderView(headerView,null,false);
	
	intent = getIntent();
	if (intent.getData() == null ) {
		intent.setData(Provider.ShoppingList.CONTENT_URI);
	}
	
	mUri = intent.getData();
	mCursor = contentResolver.query(mUri, null, null, null,null);
    }

	@Override
        protected void onResume() {
                super.onResume();
		refreshList();
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

	private void refreshList() {
		mCursor = contentResolver.query(mUri, null,null,null,null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.shoppinglist_row, mCursor,
								new String[] {Provider.ShoppingList.INGREDIENT,
										Provider.ShoppingList.QTY,
										Provider.ShoppingList.UNIT},
								new int[] {R.id.ingredient, 
										R.id.qty,
										R.id.unit});
		//Use a custom view binder to allow for text strikethrough
		adapter.setViewBinder(new ShoppingListBinder());
		setListAdapter(adapter);
		mCursor.first();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		menu.add(0, DISCARD_ID, "Discard Changes");
		menu.add(0, FINISH_SHOPPING_ID, "Finish Shopping");
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch(item.getId()) {
			case DISCARD_ID:
				mRollBack = true;	
				finish();
				return true;
			case FINISH_SHOPPING_ID:
				int qtyBasket;
				int qtyInv;
				Uri invUri;
				Cursor cInv;
				ContentValues update;
				//Loop through the shopping list adding basket qtys to inventory qtys
				mCursor.first();
				do{
					qtyBasket = mCursor.getInt(Provider.ShoppingList.QTY_BASKET_INDEX);
					invUri = Uri.withAppendedPath(Provider.Inventory.CONTENT_URI, mCursor.getString(0));	
					cInv = contentResolver.query(invUri, null, null, null,null);
					cInv.first();
					qtyInv = cInv.getInt(Provider.Inventory.QTY_INDEX);
					qtyInv = qtyInv + qtyBasket;
					update = new ContentValues();
					update.put(Provider.Inventory.QTY, qtyInv);
					update.put(Provider.Inventory.QTY_BASKET,0);
					contentResolver.update(invUri, update, null, null);
					mCursor.next();
					
				} while (!mCursor.isAfterLast());
				cInv.close();
				mCursor.close();
				mRollBack = false;
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position > 0) {
			int basket = mCursor.getInt(Provider.ShoppingList.QTY_BASKET_INDEX);
			int needed = mCursor.getInt(Provider.ShoppingList.QTY_INDEX);
			ContentValues update = new ContentValues();
			if (basket < needed) {
				update.put(Provider.ShoppingList.QTY_BASKET, needed);
			}
			else {
				update.put(Provider.ShoppingList.QTY_BASKET, 0);
			}
			contentResolver.update(Uri.withAppendedPath(Provider.ShoppingList.CONTENT_URI,"" + id),update,null,null);
			refreshList();
		}
        }


}
