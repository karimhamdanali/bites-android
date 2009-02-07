package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Ingredients;
import captainfanatic.bites.RecipeBook.Methods;
import captainfanatic.bites.RecipeBook.Recipes;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

//TODO: handle sms message text in intent from SmsReceiver

public class Bites extends TabActivity {
	SmsReceiver sms;
	
	private ListView mRecipeList;
	private ListView mIngredientList;
	private ListView mMethodList;
	
	private TextView mRecipeHeader;
	private TextView mIngredientHeader;
	private TextView mMethodHeader;
	
	private Cursor mCurRecipe;
	private Cursor mCurIngredient;
	private Cursor mCurMethod;
	
	private Uri mUriRecipe;
	private Uri mUriIngredient;
	private Uri mUriMethod;
	
	//Use private members for dialog textview to prevent weird persistence problem
	private EditText mDialogEdit;
	private View mDialogView;
	private TextView mDialogText;	
	
	private long mRecipeId;
	
	private Context context;
	
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
    private static final int DIALOG_RECIPE_EDIT = 1;
    private static final int DIALOG_INGREDIENT_EDIT = 2;
    private static final int DIALOG_METHOD_EDIT = 3;
    private static final int DIALOG_DELETE_RECIPE = 4;
    private static final int DIALOG_DELETE_INGREDIENT = 5;
    private static final int DIALOG_DELETE_METHOD = 6;
    private static final int DIALOG_RECIPE_INSERT = 7;
    private static final int DIALOG_INGREDIENT_INSERT = 8;
    private static final int DIALOG_METHOD_INSERT = 9;

		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        context = this;
        final TabHost tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.bites, tabHost.getTabContentView(), true);
                
        //Create the tabs and set their layouts
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("Recipes")
                .setContent(R.id.recipes));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("Ingredients")
                .setContent(R.id.ingredients));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("Method")
                .setContent(R.id.methods));  
    
        //Set the listviews to their views in bites.xml
        mRecipeList = (ListView)findViewById(R.id.recipelist);
        mIngredientList = (ListView)findViewById(R.id.ingredientlist);
        mMethodList = (ListView)findViewById(R.id.methodlist);
        
        //Set the header text on each tab to its view
        mRecipeHeader = (TextView)findViewById(R.id.recipeheader);
        mIngredientHeader = (TextView)findViewById(R.id.ingredientheader);
        mMethodHeader = (TextView)findViewById(R.id.methodheader);      
                
        //The action to perform when an item in the recipe list is clicked
        mRecipeList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mRecipeId = id;
				mUriRecipe = Uri.withAppendedPath(Recipes.CONTENT_URI, mCurRecipe.getString(0));
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
		        
		        String recipeName = mCurRecipe.getString(1);
		        mRecipeHeader.setText(recipeName);
		        mIngredientHeader.setText(recipeName);
		        mMethodHeader.setText(recipeName);
			}
        });
        
        //The action to perform when an item in the ingredient list is clicked
        mIngredientList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		mUriIngredient = Uri.withAppendedPath(Ingredients.CONTENT_URI, mCurIngredient.getString(0));
        	}
        });
        
        //The action to perform when an item in the method list is clicked
        mMethodList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		mUriMethod = Uri.withAppendedPath(Methods.CONTENT_URI, mCurMethod.getString(0));
        	}
        });
        
        //Create the managed cursor for the recipe list objects
        mCurRecipe = managedQuery(Recipes.CONTENT_URI, 
        							PROJECTION_RECIPES, 
        							null, 
        							null,
        							Recipes.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter recipeAdapter = new SimpleCursorAdapter(this, R.layout.recipelist_item, mCurRecipe,
                new String[] { Recipes.TITLE }, new int[] { R.id.recipetitle});
        mRecipeList.setAdapter(recipeAdapter);
        
        //Create the managed cursor for the ingredient list objects
        mCurIngredient = managedQuery(Ingredients.CONTENT_URI, 
        								PROJECTION_INGREDIENTS, 
        								Ingredients.RECIPE + "=" + mRecipeId, 
        								null,
        								Ingredients.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter ingredientAdapter = new SimpleCursorAdapter(this, R.layout.ingredientlist_item, mCurIngredient,
                new String[] { Ingredients.TEXT}, new int[] { R.id.ingredienttext});
        mIngredientList.setAdapter(ingredientAdapter);
        
        //Create the managed cursor for the method list objects
        mCurMethod = managedQuery(Methods.CONTENT_URI, 
				PROJECTION_METHODS, 
				Methods.RECIPE + "=" + mRecipeId, 
				null,
				Methods.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter methodAdapter = new SimpleCursorAdapter(this, R.layout.methodlist_item, mCurMethod,
        										new String[] { Methods.TEXT}, new int[] { R.id.methodtext});
        mMethodList.setAdapter(methodAdapter);
        
        //By default select the first row and perform a click on it to initialise the header text and recipe id
        if (mCurRecipe.moveToFirst())
        	mRecipeList.performItemClick(null, mCurRecipe.getPosition(), mCurRecipe.getLong(0));
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
        
        menu.add(0, MENU_ITEM_DELETE, 0, "delete")
        .setIcon(android.R.drawable.ic_menu_delete);
       
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
	        	showDialog(DIALOG_RECIPE_INSERT);
	        	//Have to set text from here to get around weird 
	        	//dialog persistence issue...
	        	mDialogEdit.setText("");
	        	break;
        	//Ingredients tab
        	case 1:
	        	showDialog(DIALOG_INGREDIENT_INSERT);
	        	mDialogEdit.setText("");
	        	break;
        	//Methods tab
        	case 2:
	        	showDialog(DIALOG_METHOD_INSERT);
	        	mDialogEdit.setText("");
	        	break;
        	}
        	break;
	    case MENU_ITEM_EDIT:
	        // Edit an existing item
	    	switch (selTab) {
	    	//Recipe tab
	    	case 0:
	    		showDialog(DIALOG_RECIPE_EDIT);
	            mDialogEdit.setText(mCurRecipe.getString(1));
				break;
			//Ingredients tab
	    	case 1:
				showDialog(DIALOG_INGREDIENT_EDIT);
	            mDialogEdit.setText(mCurIngredient.getString(2));
				break;
			//Methods tab
	    	case 2:
				showDialog(DIALOG_METHOD_EDIT);
	            mDialogEdit.setText(mCurMethod.getString(2));
				break;
	    	}
	    	break;
	    case MENU_ITEM_DELETE:
	    	switch (selTab) {
	    	//Delete an item
	    	//Delete called from Recipe Tab
	    	case 0:
	    		showDialog(DIALOG_DELETE_RECIPE);
	    		mDialogText.setText(mCurRecipe.getString(1));
		    	break;
		    //Delete called from Ingredient Tab
	    	case 1:
	    		showDialog(DIALOG_DELETE_INGREDIENT);
	    		mDialogText.setText(mCurIngredient.getString(2));
	    		break;
	    	//Delete called from Method Tab
	    	case 2:
	    		showDialog(DIALOG_DELETE_METHOD);
	    		mDialogText.setText(mCurMethod.getString(2));
	    		break;
	    	}
	    	break;
	    }
        return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		switch (id) {
		case DIALOG_RECIPE_EDIT:
            mDialogView = factory.inflate(R.layout.dialog_recipename, null);
            mDialogEdit = (EditText)mDialogView.findViewById(R.id.recipename_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_recipename_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_recipename_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                        values.put(Recipes.TITLE, mDialogEdit.getText().toString());
                        getContentResolver().update(mUriRecipe, values, null, null);
                    }
                })
                .setNegativeButton(R.string.dialog_recipename_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_INGREDIENT_EDIT:
            mDialogView = factory.inflate(R.layout.dialog_ingredient, null);
            mDialogEdit = (EditText)mDialogView.findViewById(R.id.ingredient_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_ingredient_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_ingredient_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                        values.put(Ingredients.TEXT, mDialogEdit.getText().toString());
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
		case DIALOG_METHOD_EDIT:
			mDialogView = factory.inflate(R.layout.dialog_method, null);
			mDialogEdit = (EditText)mDialogView.findViewById(R.id.method_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_method_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_method_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                        values.put(Methods.TEXT, mDialogEdit.getText().toString());
                        values.put(Methods.RECIPE, mRecipeId);
                        getContentResolver().update(mUriMethod, values, null, null);
                	}
                })
                .setNegativeButton(R.string.dialog_ingredient_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_DELETE_RECIPE:
			mDialogView = factory.inflate(R.layout.dialog_confirm, null);
			mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_recipe_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_confirm_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		getContentResolver().delete(Ingredients.CONTENT_URI, 
								Ingredients._ID + "=" + mRecipeId, 
								null);
                		getContentResolver().delete(Methods.CONTENT_URI, 
								Methods._ID + "=" + mRecipeId, null);
                		getContentResolver().delete(mUriRecipe, null, null);
                		mCurRecipe.requery();
                		mCurRecipe.moveToFirst();
                		mRecipeList.performItemClick(mRecipeList, mCurRecipe.getPosition(), mCurRecipe.getLong(0));
                	}
                })
                .setNegativeButton(R.string.dialog_confirm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
		case DIALOG_DELETE_INGREDIENT:
			mDialogView = factory.inflate(R.layout.dialog_confirm, null);
			mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_ingredient_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_confirm_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		getContentResolver().delete(mUriIngredient, null, null);
                		mCurIngredient.moveToFirst();
                	}
                })
                .setNegativeButton(R.string.dialog_confirm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
		case DIALOG_DELETE_METHOD:
			mDialogView = factory.inflate(R.layout.dialog_confirm, null);
			mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_method_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_confirm_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		getContentResolver().delete(mUriMethod, null, null);
                		mCurMethod.moveToFirst();
                	}
                })
                .setNegativeButton(R.string.dialog_confirm_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
		case DIALOG_RECIPE_INSERT:
            mDialogView = factory.inflate(R.layout.dialog_recipename, null);
            mDialogEdit = (EditText)mDialogView.findViewById(R.id.recipename_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_recipename_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_recipename_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		mUriRecipe = getContentResolver().insert(Recipes.CONTENT_URI,null);
                    	ContentValues values = new ContentValues();
                        values.put(Recipes.TITLE, mDialogEdit.getText().toString());
                        getContentResolver().update(mUriRecipe, values, null, null);
                    }
                })
                .setNegativeButton(R.string.dialog_recipename_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_INGREDIENT_INSERT:
            mDialogView = factory.inflate(R.layout.dialog_ingredient, null);
            mDialogEdit = (EditText)mDialogView.findViewById(R.id.ingredient_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_ingredient_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_ingredient_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		ContentValues values = new ContentValues();
                		values.put(Ingredients.RECIPE, mRecipeId);
                		mUriIngredient = getContentResolver().insert(Ingredients.CONTENT_URI,values);
                        values.put(Ingredients.TEXT, mDialogEdit.getText().toString());
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
		case DIALOG_METHOD_INSERT:
			mDialogView = factory.inflate(R.layout.dialog_method, null);
			mDialogEdit = (EditText)mDialogView.findViewById(R.id.method_edit);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_method_title)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_method_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		ContentValues values = new ContentValues();
                		values.put(Methods.RECIPE, mRecipeId);
                		mUriMethod = getContentResolver().insert(Methods.CONTENT_URI,values);
                        values.put(Methods.TEXT, mDialogEdit.getText().toString());
                        values.put(Methods.RECIPE, mRecipeId);
                        getContentResolver().update(mUriMethod, values, null, null);
                	}
                })
                .setNegativeButton(R.string.dialog_ingredient_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();


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