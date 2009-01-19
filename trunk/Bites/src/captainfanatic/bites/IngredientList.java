package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Ingredients;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class IngredientList extends ListActivity {
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Ingredients._ID, // 0
            Ingredients.TEXT, // 1
    };
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ingredients);
		
		Cursor cursor = managedQuery(Ingredients.CONTENT_URI, PROJECTION, null, null,
				Ingredients.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.recipelist_item, cursor,
                new String[] { Ingredients.TEXT}, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
	}
}
