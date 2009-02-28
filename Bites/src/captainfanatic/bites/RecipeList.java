package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Recipes;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RecipeList extends ListActivity {
	
	private Cursor mCurRecipe;

	private static final String[] PROJECTION_RECIPES = new String[] {
        Recipes._ID, // 0
        Recipes.TITLE, // 1
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//Create the managed cursor for the recipe list objects
        mCurRecipe = managedQuery(Recipes.CONTENT_URI, 
        							PROJECTION_RECIPES, 
        							null, 
        							null,
        							Recipes.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter recipeAdapter = new SimpleCursorAdapter(this, R.layout.recipelist_item, mCurRecipe,
                new String[] { Recipes.TITLE }, new int[] { R.id.recipetitle});
        
        setListAdapter(recipeAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
