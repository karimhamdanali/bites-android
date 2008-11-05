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

public class RecipeMethodEdit extends Activity
{
	private static final String TAG = "Bites.RecipeMethodEdit";
	public static final String RECIPE_KEY = "recipeKey";
	
	private static final int DISCARD_ID = Menu.FIRST;

	private int mState;
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;

	private Uri mUri;	
	private String rowId;
	private ContentResolver contentResolver;

	private EditText mStep;
	private EditText mMethod;

	private long mRecipeId;
	private Intent intent;

	private ContentValues values;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
	intent = getIntent();
        setContentView(R.layout.recipemethodedit);
	contentResolver = getContentResolver();
	
	//Set member variables to layout items
	mStep = (EditText)findViewById(R.id.step);
	mMethod = (EditText)findViewById(R.id.method);
	
	//Get the recipe id from the bundle extra
	mRecipeId = intent.getLongExtra(RECIPE_KEY, 0L);
	values = new ContentValues();


	String action = intent.getAction();
	if (action.equals(Intent.INSERT_ACTION)) {
		mState = STATE_INSERT;
		//Get a cursor to highest step number for this recipe
		Cursor cLastStep = contentResolver.query(Provider.RecipeMethod.CONTENT_URI,
                                                                        new String[] {"MAX("+Provider.RecipeMethod.STEP+")"},
                                                                        Provider.RecipeMethod.ID_RECIPE + "=" + mRecipeId,
                                                                        null,null);
		cLastStep.first();
		//Add one to the current highest step number to get the new step number
               	int nextStep = cLastStep.getInt(0) + 1;

		values.put(Provider.RecipeMethod.STEP, nextStep);
		values.put(Provider.RecipeMethod.ID_RECIPE, mRecipeId);
		mUri = contentResolver.insert(intent.getData(), values);
		//TODO: call an sql query to get the highest step number then add 1 for a new step
	}
	if (action.equals(Intent.EDIT_ACTION)) {
		mState = STATE_EDIT;
		mUri = intent.getData();
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
						break;
					case STATE_EDIT:
						Cursor c = contentResolver.query(mUri, null, null, null, null);
						c.first();
						mStep.setText(c.getString(Provider.RecipeMethod.STEP_INDEX));
						mMethod.setText(c.getString(Provider.RecipeMethod.METHOD_INDEX));
						break;
				}
				finish();
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onResume() {
		super.onResume();
			Cursor c = contentResolver.query(mUri, null, null, null, null);
			c.first();
			mStep.setText(c.getString(Provider.RecipeMethod.STEP_INDEX));
			mMethod.setText(c.getString(Provider.RecipeMethod.METHOD_INDEX));
	}

	@Override
	protected void onPause() {
		super.onPause();
			values.put(Provider.RecipeMethod.STEP, mStep.getText().toString());
			values.put(Provider.RecipeMethod.METHOD, mMethod.getText().toString());
			contentResolver.update(mUri, values,  null, null);
	}

	@Override
	protected void onFreeze(Bundle outState) {
	}
}
