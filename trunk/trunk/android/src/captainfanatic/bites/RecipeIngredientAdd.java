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

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListAdapter;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;
import android.content.ContentValues;

public class RecipeIngredientAdd extends Activity
{
	private static final String TAG = "Bites.RecipeIngredientAdd";
	private static final int PICK_INVENTORY = 1;
	public static final String RECIPE_KEY = "recipeKey";
	public static final String INGREDIENT_KEY = "ingredientKey";
	public static final String UNIT_KEY = "unitKey";
	
	private static final int DISCARD_ID = Menu.FIRST;

	private int mState;
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;

	private Uri mUri;	
	private String rowId;
	private ContentResolver contentResolver;

	private TextView mIngredient;
	private TextView mUnit;
	private EditText mQuantity;

	private Button btnConfirm;
	private Button btnDiscard;

	private long mIngredientId;
	private long mRecipeId;
	private Intent intent;

	private boolean waitingForReturn;	
	private ContentValues values;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

	intent = getIntent();
        setContentView(R.layout.recipeingredientadd);
	contentResolver = getContentResolver();
	
	//Set member variables to layout items
	mIngredient = (TextView)findViewById(R.id.txt_ingredient);
	mUnit = (TextView)findViewById(R.id.txt_unit);
	mQuantity = (EditText)findViewById(R.id.quantity);
	
	//Get the recipe id from the bundle extra
	mRecipeId = intent.getLongExtra(RECIPE_KEY, 0L);
	values = new ContentValues();


	String action = intent.getAction();
	if (action.equals(Intent.INSERT_ACTION)) {
		mState = STATE_INSERT;
		values.put(Provider.RecipeIngredientsView.ID_RECIPE, mRecipeId);
		waitingForReturn=true;
	}
	if (action.equals(Intent.EDIT_ACTION)) {
		mState = STATE_EDIT;
		mUri = intent.getData();
		waitingForReturn=false;
	}

	//Go straight to picking an ingredient from inventory if this is an insert action
	if (mState == STATE_INSERT) {
		Intent pickIntent = new Intent(Intent.PICK_ACTION, Provider.Inventory.CONTENT_URI);
		startSubActivity(pickIntent, PICK_INVENTORY);
	}

    }

	@Override
	protected void onActivityResult (int requestCode, int resultCode, String data, Bundle extras) {
		super.onActivityResult(requestCode, resultCode, data, extras);
		
		if (requestCode == PICK_INVENTORY & resultCode != RESULT_CANCELED) {
			mIngredient.setText(data);
			mUnit.setText(extras.getString(UNIT_KEY));
			mIngredientId = extras.getLong(INGREDIENT_KEY);	
			values.put(Provider.RecipeIngredientsView.ID_INGREDIENT, mIngredientId);
			mUri = contentResolver.insert(intent.getData(), values);
			waitingForReturn=false;
		}
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
						contentResolver.delete(mUri, null, null);
						finish();
						break;
					case STATE_EDIT:
						Cursor c = contentResolver.query(mUri, null, null, null, null);
						c.first();
						mQuantity.setText(c.getString(Provider.RecipeIngredientsView.QTY_INDEX));
						break;
				}
				finish();
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (!waitingForReturn) {
			Cursor c = contentResolver.query(mUri, null, null, null, null);
			c.first();
			mQuantity.setText(c.getString(Provider.RecipeIngredientsView.QTY_INDEX));
			mIngredient.setText(c.getString(Provider.RecipeIngredientsView.INGREDIENT_INDEX));
			mUnit.setText(c.getString(Provider.RecipeIngredientsView.UNIT_INDEX));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!waitingForReturn) {
			values.put(Provider.RecipeIngredientsView.QTY, mQuantity.getText().toString());
			contentResolver.update(mUri, values,  null, null);
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
	}
}
