package caldwell.ben.bites;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParserException;

import caldwell.ben.bites.RecipeBook.Ingredients;
import caldwell.ben.bites.RecipeBook.Methods;
import caldwell.ben.bites.RecipeBook.Recipes;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

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
	private File mFile;
	
	private static final int DIALOG_DELETE = 1;

	private String mPath;
	
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
					e.printStackTrace();
				} catch (IOException e) {
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
		mRecipeName = getIntent().getStringExtra(SmsReceiver.KEY_RECIPE);
		values.put(Recipes.TITLE, mRecipeName);
		Uri recipeUri = getContentResolver().insert(Recipes.CONTENT_URI, values);
		mRecipeId = Long.parseLong(recipeUri.getLastPathSegment());

		//get ingredients from the intent extras and load into the content provider
		String ingredients[] = getIntent().getStringArrayExtra(SmsReceiver.KEY_ING_ARRAY);
			for (int i =0; i<ingredients.length; i++)
			{
				values = new ContentValues();
				values.put(Ingredients.RECIPE, mRecipeId);
				values.put(Ingredients.TEXT,ingredients[i]);
				getContentResolver().insert(Ingredients.CONTENT_URI, values);
			}
		
		//get methods from the intent extras and load into the content provider
		String methods[] = getIntent().getStringArrayExtra(SmsReceiver.KEY_METH_ARRAY);
		int methodSteps[] = getIntent().getIntArrayExtra(SmsReceiver.KEY_METH_STEP_ARRAY);
		
		for (int i = 0; i<methods.length; i++)
		{
			values = new ContentValues();
			values.put(Methods.RECIPE, mRecipeId);
			values.put(Methods.TEXT, methods[i]);
			values.put(Methods.STEP, (i<methodSteps.length) ? methodSteps[i] : i);
			getContentResolver().insert(Methods.CONTENT_URI, values);
		}
	}
	
	/**
	 * addXmlRecipe
	 * Parse a recipe xml file for recipe name, ingredients and methods and 
	 * add to recipe content provider.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void addXmlRecipe() throws XmlPullParserException, IOException {
		
		mPath = getIntent().getData().getPath();
		mFile = new File(mPath);

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
														.newDocumentBuilder();
			Document doc = builder.parse(mFile);
			Element recipe = doc.getDocumentElement();
			NodeList ingredients = recipe.getElementsByTagName("ingredient");
			NodeList methods = recipe.getElementsByTagName("method");
			

			//Insert new recipe title
			ContentValues values = new ContentValues();
			mRecipeName = recipe.getAttribute("name");
			values.put(Recipes.TITLE, mRecipeName);
			Uri recipeUri = getContentResolver().insert(Recipes.CONTENT_URI, values);
			mRecipeId = Long.parseLong(recipeUri.getLastPathSegment());
			
			//insert ingredients for the new recipe
			values = new ContentValues();
			for (int i =0; i<ingredients.getLength(); i++)
			{
				values.put(Ingredients.RECIPE, mRecipeId);
				values.put(Ingredients.TEXT,ingredients.item(i).getFirstChild().getNodeValue());
				getContentResolver().insert(Ingredients.CONTENT_URI, values);
			}
			
			//insert methods for the new recipe
			values = new ContentValues();
			for (int i =0; i<methods.getLength(); i++)
			{
				values.put(Methods.RECIPE, mRecipeId);
				values.put(Methods.STEP,((Element)methods.item(i)).getAttribute("step"));
				values.put(Methods.TEXT,methods.item(i).getFirstChild().getNodeValue());
				getContentResolver().insert(Methods.CONTENT_URI, values);
			}
			
			//Delete the downloaded file?
			showDialog(DIALOG_DELETE);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		switch (id)	{
		case DIALOG_DELETE:
			View mDialogView = factory.inflate(R.layout.dialog_confirm, null);
			TextView mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
			mDialogText.setText(mPath);
			return new AlertDialog.Builder(this)
            .setTitle(R.string.delete_file)
            .setView(mDialogView)
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                	/* User clicked OK so do some stuff */
            		mFile.delete();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked cancel so do some stuff */
                }
            })
            .create();
		}
		return null;
	}  
	
	
}