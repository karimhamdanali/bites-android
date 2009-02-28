package captainfanatic.bites;

import captainfanatic.bites.RecipeBook.Methods;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MethodList extends ListActivity {
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Methods._ID, // 0
            Methods.TEXT, // 1
    };
	private Cursor mCursor;
	
	private TextView mHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mCursor = managedQuery(Methods.CONTENT_URI, PROJECTION, null, null,
				Methods.DEFAULT_SORT_ORDER);
		
		setContentView(R.layout.methods);
		
		mHeader = (TextView)findViewById(R.id.methodheader);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.recipelist_item, mCursor,
                new String[] { Methods.TEXT}, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
	}
}
