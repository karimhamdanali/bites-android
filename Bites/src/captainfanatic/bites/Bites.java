package captainfanatic.bites;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

//TODO: handle sms message text in intent from SmsReceiver

public class Bites extends Activity {
	ViewFlipper flipper;
	TextView text;
	SmsReceiver sms;
	Button btn;
	ListView mRecipeListView;
	ListView mRecipeIngredientsView;
	ListView mRecipeMethodView;

	ArrayList<Recipe> mRecipeList;
	Recipe selRecipe;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Context context = this;
        
        mRecipeList = new ArrayList<Recipe>();
        Recipe recipe = new Recipe();
        selRecipe = new Recipe();
        mRecipeList.add(recipe);
        mRecipeList.add(recipe);       
        mRecipeListView = (ListView)findViewById(R.id.recipeListView);
        mRecipeListView.setAdapter(new ArrayAdapter<Recipe>(this,
                android.R.layout.simple_list_item_1,mRecipeList));
        
        mRecipeIngredientsView = (ListView)findViewById(R.id.recipeIngredientsView);
        mRecipeMethodView = (ListView)findViewById(R.id.recipeMethodView);
        
		selRecipe = (Recipe)mRecipeListView.getItemAtPosition(0);
        mRecipeIngredientsView.setAdapter(new ArrayAdapter<String>(context, 
																	android.R.layout.simple_list_item_1, 
																	selRecipe.ingredients));
        mRecipeMethodView.setAdapter(new ArrayAdapter<String>(context, 
        														android.R.layout.simple_list_item_1, 
        														selRecipe.methods));
        
        flipper=(ViewFlipper)findViewById(R.id.details);
        
        mRecipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
											int arg2, long arg3) {
				selRecipe = (Recipe)mRecipeListView.getSelectedItem();
				ArrayAdapter<?> adapter = (ArrayAdapter<?>)mRecipeIngredientsView.getAdapter();
				adapter.notifyDataSetChanged();
				adapter = (ArrayAdapter<?>)mRecipeMethodView.getAdapter();
				adapter.notifyDataSetChanged();
				flipper.showNext();
			}
        });
    }

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		
		//Check for new xml recipe from a text message etc.
		if (!intent.hasExtra(SmsReceiver.KEY_MSG_TEXT)) {
			return;
		}

		String strRecipe = intent.getStringExtra(SmsReceiver.KEY_MSG_TEXT);
		Recipe newRecipe = new Recipe();
		try {
			newRecipe.Pull(strRecipe);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//If the xml pullparser was successful and we have a new recipe object
		mRecipeList.add(newRecipe);
	}
    
}