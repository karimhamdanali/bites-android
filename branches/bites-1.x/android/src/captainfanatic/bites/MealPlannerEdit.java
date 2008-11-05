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
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;

public class MealPlannerEdit extends Activity
{
	private static final String TAG = "Bites.MealPlannerEdit";
        private static final int PICK_RECIPE = 1;

	private int mState;
	private static final int STATE_INSERT = 1;
	private static final int STATE_EDIT = 2;

	private static final int DISCARD_ID = Menu.FIRST;

	private Uri mUri;	
	private Intent intent;
	private ContentResolver contentResolver;
	private ContentValues values;
	private long mRecipeId;

	private TextView mRecipe;
	private EditText mServes;
	private boolean waitingForReturn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.mealplanneredit);
	contentResolver = getContentResolver();
	intent = getIntent();
	values = new ContentValues();

	mRecipe = (TextView)findViewById(R.id.txt_recipe);
	mServes = (EditText)findViewById(R.id.serves);
	final String action = intent.getAction();

	if (action.equals(Intent.INSERT_ACTION)) {
		mState = STATE_INSERT;
		waitingForReturn = true;
	}
	if (action.equals(Intent.EDIT_ACTION)) {
		mState = STATE_EDIT;
		mUri = intent.getData();
		waitingForReturn = false;
	}

	//Go straight to picking an ingredient from inventory if this is an insert action
        if (mState == STATE_INSERT) {
		Intent pickIntent = new Intent(Intent.PICK_ACTION, Provider.Recipes.CONTENT_URI);
		startSubActivity(pickIntent, PICK_RECIPE);
	}
    }
	
	@Override
        protected void onActivityResult (int requestCode, int resultCode, String data, Bundle extras) {
                super.onActivityResult(requestCode, resultCode, data, extras);

                if (requestCode == PICK_RECIPE & resultCode != RESULT_CANCELED) {
                        mRecipe.setText(data);
                        mRecipeId = extras.getLong(RecipeList.RECIPE_KEY);
                        values.put(Provider.MealPlannerView.ID_RECIPE, mRecipeId);
                        mUri = contentResolver.insert(intent.getData(), values);
                        waitingForReturn=false;
                }
        }

	@Override
	protected void onResume() {
		super.onResume();
		if (!waitingForReturn) {
			Cursor c = contentResolver.query(mUri, null, null, null, null);
			c.first();
			mRecipe.setText(c.getString(Provider.MealPlannerView.RECIPE_INDEX));
			mServes.setText(c.getString(Provider.MealPlannerView.SERVES_INDEX));
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
						Cursor c = managedQuery(mUri, null, null, null);
						mRecipe.setText(c.getString(Provider.MealPlannerView.RECIPE_INDEX));
						mServes.setText(c.getString(Provider.MealPlannerView.SERVES_INDEX));
                                                break;
                                }
                                finish();
                }
                return super.onOptionsItemSelected(item);
        }


	@Override
	protected void onPause() {
		super.onPause();
		if (!waitingForReturn) {
			values.put(Provider.MealPlannerView.SERVES, mServes.getText().toString());
			contentResolver.update(mUri, values, null, null);
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
	}
}
