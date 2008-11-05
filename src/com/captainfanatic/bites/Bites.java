package com.captainfanatic.bites;

import com.captainfanatic.bites.RecipeBook.Ingredients;
import com.captainfanatic.bites.RecipeBook.Recipe;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/***
 * A recipe activity. 
 * <p>
 * A tabbed view show ingredients and methods. Features [will] include:
 * import/export of recipes using xml;
 * using the intents system to interact with a shopping list application;
 * @author Ben Caldwell
 *
 */
public class Bites extends Activity {
    
	private TextView currentRecipe;
	private ListView recipeList;
	private ListView ingredientList;
	private ListView methodList;
	
	private Uri mUri;
	private Intent intent;
	
    // Menu item ids
    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;

	
	/**
	 * Columns that will be used from the database
	 */
	private static final String[] RECIPE_PROJECTION = new String[] {
		Recipe._ID,
		Recipe.TITLE,
		Recipe.RATING,		
	};
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Recipe.CONTENT_URI);
        }


        final TabHost tabs = (TabHost) findViewById(R.id.tabhost);
        
        tabs.setup();
        
        TabSpec recipeSpec = tabs.newTabSpec("recipes");
        recipeSpec.setContent(R.id.recipes);
        recipeSpec.setIndicator("Recipe");
        tabs.addTab(recipeSpec);
        
        TabSpec ingredientSpec = tabs.newTabSpec("ingredients");
        ingredientSpec.setContent(R.id.ingredients);
        ingredientSpec.setIndicator("Ingredients");
        tabs.addTab(ingredientSpec);
        
        TabSpec methodSpec = tabs.newTabSpec("method");
        methodSpec.setContent(R.id.method);
        methodSpec.setIndicator("Method");
        tabs.addTab(methodSpec);
        
        tabs.setCurrentTab(0);
     
        loadRecipes();
    
        //Perform a managed query on the recipes database
        Cursor cursor = managedQuery(getIntent().getData(), RECIPE_PROJECTION, null, null, Recipe.DEFAULT_SORT_ORDER);
        
        //Map entries from the recipes database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.recipe_row, cursor, 
        														new String[] {Recipe.TITLE},
        														new int[] {R.id.recipe_name});
        
        recipeList = (ListView) findViewById(R.id.recipe_list);
        recipeList.setAdapter(adapter);
        
/*        
        currentRecipe = (TextView) findViewById(R.id.current_recipe);
        currentRecipe.setText(recipes.get(0).getName());
                
        
           
        ingredientList = (ListView) findViewById(R.id.ingredient_list);
        ingredientList.setAdapter(new SimpleAdapter(this, recipes.get(0).getIngredients(), R.layout.ingredient_row,
        							new String[] {Recipe.ING_NAME_KEY, Recipe.ING_QTY_KEY},
        							new int[] {R.id.ingredient_name, R.id.ingredient_qty}));
        
        methodList = (ListView) findViewById(R.id.method_list);
        methodList.setAdapter(new SimpleAdapter(this, recipes.get(0).getMethod(), R.layout.method_row,
        							new String[] {Recipe.METHOD_STEP_KEY},
        							new int[] {R.id.method_step}));
  
*/         
    }
    
    

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, Bites.class), null, intent, 0, null);

        return true;
        
    }

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // insert a new row in the database
        	mUri = getContentResolver().insert(intent.getData(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
	}
	

	/***
     * Test function to create a recipe in the absence of recipe importing code.
     */
    private void loadRecipes() {
    	    	
    }
        
 }