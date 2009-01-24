package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Ingredients;
import captainfanatic.bites.RecipeBook.Methods;
import captainfanatic.bites.RecipeBook.Recipes;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;

//TODO: handle sms message text in intent from SmsReceiver

public class Bites extends TabActivity {
	SmsReceiver sms;
	
	private ListView mRecipeList;
	private ListView mIngredientList;
	private ListView mMethodList;
	
	private Cursor mCurRecipe;
	private Cursor mCurIngredient;
	private Cursor mCurMethod;
	
	private Uri mUriRecipe;
	private Uri mUriIngredient;
	private Uri mUriMethod;
	
	private long mRecipeId;
	
	private static final String[] PROJECTION_RECIPES = new String[] {
        Recipes._ID, // 0
        Recipes.TITLE, // 1
	};
	
	private static final String[] PROJECTION_INGREDIENTS = new String[] {
        Ingredients._ID, // 0
        Ingredients.RECIPE, // 1
        Ingredients.TEXT, // 2
	};
	
	private static final String[] PROJECTION_METHODS = new String[] {
        Methods._ID, // 0
        Methods.TEXT, // 1
	};
	
	/** Options menu items */
	public static final int MENU_ITEM_EDIT = Menu.FIRST;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 1;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 2;
    
    /** Dialog boxes to display */
    private static final int DIALOG_RECIPE_NAME = 1;
    private static final int DIALOG_INGREDIENT = 2;
    private static final int DIALOG_METHOD = 3;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        final Context context = this;
        final TabHost tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.bites, tabHost.getTabContentView(), true);
                
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("Recipes")
                .setContent(R.id.recipes));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("Ingredients")
                .setContent(R.id.ingredients));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("Method")
                .setContent(R.id.methods));  
    
        
        mRecipeList = (ListView)findViewById(R.id.recipelist);
        mIngredientList = (ListView)findViewById(R.id.ingredientlist);
        mMethodList = (ListView)findViewById(R.id.methodlist);
        
        mRecipeList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mRecipeId = id;
				mUriRecipe = Uri.withAppendedPath(Recipes.CONTENT_URI, Long.toString(mRecipeList.getSelectedItemId()));
				mCurIngredient = managedQuery(Ingredients.CONTENT_URI, 
						PROJECTION_INGREDIENTS, 
						Ingredients.RECIPE + "=" + mRecipeId, 
						null,
						Ingredients.DEFAULT_SORT_ORDER);
				
				SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(context, R.layout.ingredientlist_item, mCurIngredient,
		                new String[] { Ingredients.TEXT}, new int[] { R.id.ingredienttext});
		        mIngredientList.setAdapter(ingredientAdapter);
		        
		        mCurMethod = managedQuery(Methods.CONTENT_URI, 
						PROJECTION_METHODS, 
						Methods.RECIPE + "=" + mRecipeId, 
						null,
						Methods.DEFAULT_SORT_ORDER);

		        SimpleCursorAdapter methodAdapter = new SimpleCursorAdapter(context, R.layout.methodlist_item, mCurMethod,
		        										new String[] { Methods.TEXT}, new int[] { R.id.methodtext});
		        mMethodList.setAdapter(methodAdapter);
			}
        });
        
        mIngredientList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		mUriIngredient = Uri.withAppendedPath(Ingredients.CONTENT_URI, Long.toString(mIngredientList.getSelectedItemId()));
        	}
        });
        
        mMethodList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		mUriMethod = Uri.withAppendedPath(Methods.CONTENT_URI, Long.toString(mMethodList.getSelectedItemId()));
        	}
        });
        
        mCurRecipe = managedQuery(Recipes.CONTENT_URI, 
        							PROJECTION_RECIPES, 
        							null, 
        							null,
        							Recipes.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter recipeAdapter = new SimpleCursorAdapter(this, R.layout.recipelist_item, mCurRecipe,
                new String[] { Recipes.TITLE }, new int[] { R.id.recipetitle});
        mRecipeList.setAdapter(recipeAdapter);
        mRecipeId = mCurRecipe.getLong(0);
        
        mCurIngredient = managedQuery(Ingredients.CONTENT_URI, 
        								PROJECTION_INGREDIENTS, 
        								Ingredients.RECIPE + "=" + mRecipeId, 
        								null,
        								Ingredients.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(this, R.layout.ingredientlist_item, mCurIngredient,
                new String[] { Ingredients.TEXT}, new int[] { R.id.ingredienttext});
        mIngredientList.setAdapter(ingredientAdapter);
        
        mCurMethod = managedQuery(Methods.CONTENT_URI, 
				PROJECTION_METHODS, 
				Methods.RECIPE + "=" + mRecipeId, 
				null,
				Methods.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter methodAdapter = new SimpleCursorAdapter(this, R.layout.methodlist_item, mCurMethod,
        										new String[] { Methods.TEXT}, new int[] { R.id.methodtext});
        mMethodList.setAdapter(methodAdapter);
        
        
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Insert a new recipe into the list
        menu.add(0, MENU_ITEM_INSERT, 0, "insert")
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_ITEM_EDIT, 0, "edit")
        .setIcon(android.R.drawable.ic_menu_edit);
        
     // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, Bites.class), null, intent, 0, null);
       
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int selTab = getTabHost().getCurrentTab();
		switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Insert a new item
        	switch (selTab) {
        	//Recipes tab
        	case 0:
        		mUriRecipe = getContentResolver().insert(Recipes.CONTENT_URI,null);
	        	showDialog(DIALOG_RECIPE_NAME);
	        	break;
        	//Ingredients tab
        	case 1:
        		ContentValues values = new ContentValues();
        		values.put(Ingredients.RECIPE, mRecipeId);
        		mUriIngredient = getContentResolver().insert(Ingredients.CONTENT_URI,values);
	        	showDialog(DIALOG_INGREDIENT);
	        	break;
        	//Methods tab
        	case 2:
        		mUriMethod = getContentResolver().insert(Methods.CONTENT_URI,null);
	        	showDialog(DIALOG_METHOD);
	        	break;
        	}
	    case MENU_ITEM_EDIT:
	        // Edit an existing item
	    	switch (selTab) {
	    	//Recipe tab
	    	case 0:
	    		showDialog(DIALOG_RECIPE_NAME);
				break;
			//Ingredients tab
	    	case 1:
				showDialog(DIALOG_INGREDIENT);
				break;
			//Methods tab
	    	case 2:
				showDialog(DIALOG_METHOD);
				break;
	    	}
	    }
        return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView;
		switch (id) {
		case DIALOG_RECIPE_NAME:
            textEntryView = factory.inflate(R.layout.dialog_recipename, null);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_recipename_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.dialog_recipename_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                    	EditText recipeName = (EditText)textEntryView.findViewById(R.id.recipename_edit);
                        values.put(Recipes.TITLE, recipeName.getText().toString());
                        getContentResolver().update(mUriRecipe, values, null, null);
                    }
                })
                .setNegativeButton(R.string.dialog_recipename_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_INGREDIENT:
            textEntryView = factory.inflate(R.layout.dialog_ingredient, null);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_ingredient_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.dialog_ingredient_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                    	EditText ingredientName = (EditText)textEntryView.findViewById(R.id.ingredient_edit);
                        values.put(Ingredients.TEXT, ingredientName.getText().toString());
                        values.put(Ingredients.RECIPE, mRecipeId);
                        getContentResolver().update(mUriIngredient, values, null, null);
                    }
                })
                .setNegativeButton(R.string.dialog_ingredient_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_METHOD:
			break;
		}		
		return null;
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