package captainfanatic.bites;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

//TODO: handle sms message text in intent from SmsReceiver

public class Bites extends TabActivity {
	SmsReceiver sms;
	
	static long mRecipeId;
	static String mRecipeName;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
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

	/*@Override
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
    */

}