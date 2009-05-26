package captainfanatic.bites;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import captainfanatic.bites.RecipeBook.Ingredients;
import captainfanatic.bites.RecipeBook.Methods;
import captainfanatic.bites.RecipeBook.Recipes;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

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
        
        checkForReceived();
        
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
     * Check the intent to see if Bites was started on receiving a recipe
     * from either an sms or a downloaded file and add to the content provider if it was.
     */
	private void checkForReceived() {
		if (getIntent().getAction() != null)
        {
	        /**
	         * If Bites was started by clicking on a notification that a recipe was received
	         * load the new recipe into the database and cancel the notification 
	         */
	        if (getIntent().getAction().contentEquals("com.captainfanatic.bites.RECEIVED_RECIPE"))
	        {
	        	addSmsRecipe();
			}
        }
	        /**
	         * If Bites was started by clicking on a downloaded recipe xml file,
	         * parse the xml file and add the new recipe to the database
	         */
	    if (getIntent().getType() != null)
	    {
	    	if (getIntent().getType().contentEquals("text/plain"))
	        {
	        	try {
					addXmlRecipe();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
        }
	}

    /**
     * Add a recipe received via sms to the content provider
     */
	private void addSmsRecipe() {
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
	
	/**
	 * addXmlRecipe
	 * Parse a recipe xml file for recipe name, ingredients and methods and 
	 * add to recipe content provider.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void addXmlRecipe() throws XmlPullParserException, IOException {
		
		String path = getIntent().getData().getPath();
		File file = new File(path);

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
														.newDocumentBuilder();
			Document doc = builder.parse(file);
			Element recipe = doc.getDocumentElement();
			NodeList ingredients = recipe.getElementsByTagName("ingredient");
			NodeList methods = recipe.getElementsByTagName("method");
			
			String recipeName=recipe.getAttribute("name");
			//TODO: insert recipe with ingredients into database - use for each for multi recipe files? 
			String ingredient1=ingredients.item(0).getFirstChild().getNodeValue();

			//TODO: Delete the file?
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
	}  
}