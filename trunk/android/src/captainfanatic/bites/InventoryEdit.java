/*Bites - an organiser for cooks
Copyright (C) 2008 Ben Caldwell <benny.caldwell@gmail.com>
The Bites project lives at http://code.google.com/p/bites-android/

Bites is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Bites is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Bites.  If not, see <http://www.gnu.org/licenses/>.
*/

package captainfanatic.bites;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;

public class InventoryEdit extends Activity
{
	private static final String TAG = "Bites.InventoryEdit";

	private int mState;
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;

	private static final int DISCARD_ID = Menu.FIRST;

	private Uri mUri;	
	private Cursor mCursor;
	private Boolean mRollback;
	private Boolean mGotLock;
	private ContentResolver contentResolver;

	private EditText mIngredient;
	private EditText mQty;
	private EditText mUnit;
	private EditText mQtyMin;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.inventoryedit);

	final Intent intent = getIntent();
	final String action = intent.getAction();

	contentResolver = getContentResolver();
	
	//Begin a transaction in the Provider database - allows rollback
	if (!Provider.Transaction.isLocked()) {
		ContentValues transaction = new ContentValues();
		transaction.put(Provider.Transaction.TYPE, Provider.Transaction.BEGIN);
		contentResolver.insert(Provider.Transaction.CONTENT_URI, transaction);
		mGotLock = true;
	}
	else {
		mGotLock = false;
	}

	if (action.equals(Intent.INSERT_ACTION)) {
		mState = STATE_INSERT;
		mUri = getContentResolver().insert(intent.getData(), null);
	}
	if (action.equals(Intent.EDIT_ACTION)) {
		mState = STATE_EDIT;
		mUri = intent.getData();
	}

	mCursor = managedQuery(mUri, null, null, null);
	
	mIngredient = (EditText)findViewById(R.id.ingredient);
	if (mState == STATE_EDIT) {
		mIngredient.setClickable(false);
	}
	mQty = (EditText)findViewById(R.id.quantity);
	mUnit = (EditText)findViewById(R.id.unit);
	mQtyMin = (EditText)findViewById(R.id.quantitymin);

    }

	@Override
	protected void onResume() {
		super.onResume();
		
		mCursor.first();
		mRollback = false;

		mIngredient.setText(mCursor.getString(Provider.Inventory.INGREDIENT_INDEX));
		mQty.setText(mCursor.getString(Provider.Inventory.QTY_INDEX));
		mUnit.setText(mCursor.getString(Provider.Inventory.UNIT_INDEX));
		mQtyMin.setText(mCursor.getString(Provider.Inventory.QTY_MIN_INDEX));

	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, DISCARD_ID, "Discard");
		return result;
	}


	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
                switch(item.getId()) {
                        case DISCARD_ID:
                                switch (mState) {
                                        case STATE_INSERT:
						mRollback = true;
                                                break;
                                        case STATE_EDIT:
						mRollback = true;
                                                break;
                                }
                                finish();
                }
                return super.onOptionsItemSelected(item);
        }


	@Override
	protected void onPause() {
		super.onPause();
		if (!mCursor.isBeforeFirst()) {
			ContentValues updates = new ContentValues();
			if (mState == STATE_INSERT) {
				updates.put(Provider.Inventory.INGREDIENT, mIngredient.getText().toString());
			}
			updates.put(Provider.Inventory.QTY, mQty.getText().toString());
			updates.put(Provider.Inventory.UNIT, mUnit.getText().toString());
			updates.put(Provider.Inventory.QTY_MIN, mQtyMin.getText().toString());

			contentResolver.update(mUri,updates,null,null);
		}
		if (isFinishing() && mGotLock){
			ContentValues transaction = new ContentValues();
			if (mRollback){
				transaction.put(Provider.Transaction.TYPE, Provider.Transaction.ROLLBACK);
			}
			else{
				transaction.put(Provider.Transaction.TYPE, Provider.Transaction.COMMIT);
			}
			contentResolver.insert(Provider.Transaction.CONTENT_URI, transaction);
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
	}
}
