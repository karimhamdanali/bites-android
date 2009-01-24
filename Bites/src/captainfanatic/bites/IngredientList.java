package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Ingredients;
import captainfanatic.bites.RecipeBook.Recipes;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class IngredientList extends ListActivity {
	private static final String TAG = "IngredientList";
	// Menu item ids
	public static final int MENU_ITEM_EDIT = Menu.FIRST;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 1;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 2;
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Ingredients._ID, // 0
            Ingredients.RECIPE, // 1
            Ingredients.TEXT, // 2
    };
    
    /**
     * Case selections for the type of dialog box displayed
     */
    private static final int DIALOG_INGREDIENT = 1;
    
    private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Ingredients.CONTENT_URI);
        }
			
		setContentView(R.layout.ingredients);
		
		Cursor cursor = managedQuery(Ingredients.CONTENT_URI, PROJECTION,
									Ingredients.RECIPE + "=" + Bites.mRecipeId, 
									null, Ingredients.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.ingredientlist_item, cursor,
                new String[] { Ingredients.TEXT}, new int[] { R.id.ingredienttext});
        setListAdapter(adapter);
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
                new ComponentName(this, IngredientList.class), null, intent, 0, null);
       
        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Insert a new item
        	ContentValues values = new ContentValues();
        	values.put(Ingredients.RECIPE, Bites.mRecipeId);
        	mUri = getContentResolver().insert(getIntent().getData(), values);
        	showDialog(DIALOG_INGREDIENT);
        	break;
	    case MENU_ITEM_EDIT:
	        // Edit an existing item
			showDialog(DIALOG_INGREDIENT);
			break;
	    }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mUri = ContentUris.withAppendedId(getIntent().getData(), id);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_INGREDIENT:
			LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.dialog_ingredient, null);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_ingredient_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.dialog_ingredient_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                    	ContentValues values = new ContentValues();
                    	EditText ingredient = (EditText)textEntryView.findViewById(R.id.ingredient_edit);
                        values.put(Ingredients.TEXT, ingredient.getText().toString());
                        getContentResolver().update(mUri, values, null, null);
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
}
