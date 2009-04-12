package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Ingredients;
import captainfanatic.bites.RecipeBook.Methods;
import captainfanatic.bites.RecipeBook.Recipes;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * The main activity, operates as a top level tabhost that contains list activities for
 * recipes, ingredients and method steps.
 * 
 * Recipe received notifications send an intent to Bites to add the new recipe when clicked.
 *  
 * @author Ben Caldwell
 *
 */
public class Bites extends TabActivity {
	SmsReceiver sms;
	
	static long mRecipeId;
	static String mRecipeName;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /**
         * If Bites was started by clicking on a notification that a recipe was received
         * load the new recipe into the database and cancel the notification 
         */
        if (getIntent().getAction() != null)
        {
        	AddReceivedRecipe();
		}
               
        final TabHost tabHost = getTabHost();
               
        tabHost.addTab(tabHost.newTabSpec("tab_recipes")
                .setIndicator(getResources().getText(R.string.tab_recipes))
                .setContent(new Intent(this, RecipeList.class)));
        tabHost.addTab(tabHost.newTabSpec("tab_ingredients")
                .setIndicator(getResources().getText(R.string.tab_ingredients))
                .setContent(new Intent(this, IngredientList.class)));
        tabHost.addTab(tabHost.newTabSpec("tab_method")
                .setIndicator(getResources().getText(R.string.tab_method))
                .setContent(new Intent(this, MethodList.class)));  
    
    }

    /**
     * Add a recipe received via sms to the content provider
     */
	private void AddReceivedRecipe() {
		if(getIntent().getAction().contentEquals("com.captainfanatic.bites.RECEIVED_RECIPE")) 
		{
			//Cancel the notification using the id extra in the intent
			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(getIntent().getIntExtra(SmsReceiver.KEY_NOTIFY_ID,0));
			
			ContentValues values = new ContentValues();
			values.put(Recipes.TITLE, getIntent().getStringExtra(SmsReceiver.KEY_RECIPE));
			Uri recipeUri = getContentResolver().insert(Recipes.CONTENT_URI, values);
			long recipeId = Long.parseLong(recipeUri.getLastPathSegment());

			//get ingredients from the intent extras and load into the content provider
			String ingredients[] = getIntent().getStringArrayExtra(SmsReceiver.KEY_ING_ARRAY);
				for (int i =0; i<ingredients.length; i++)
				{
					values = new ContentValues();
					values.put(Ingredients.RECIPE, recipeId);
					values.put(Ingredients.TEXT,ingredients[i]);
					getContentResolver().insert(Ingredients.CONTENT_URI, values);
				}
			
			//get methods from the intent extras and load into the content provider
			String methods[] = getIntent().getStringArrayExtra(SmsReceiver.KEY_METH_ARRAY);
			int methodSteps[] = getIntent().getIntArrayExtra(SmsReceiver.KEY_METH_STEP_ARRAY);
			
			for (int i = 0; i<methods.length; i++)
			{
				values = new ContentValues();
				values.put(Methods.RECIPE, recipeId);
				values.put(Methods.TEXT, methods[i]);
				values.put(Methods.STEP, (i<methodSteps.length) ? methodSteps[i] : i);
				getContentResolver().insert(Methods.CONTENT_URI, values);
			}
			
			//TODO: select (highlight?) the new recipe in the RecipeList activity
		}
	}
    
}