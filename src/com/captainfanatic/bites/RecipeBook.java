package com.captainfanatic.bites;

import android.net.Uri;
import android.provider.BaseColumns;

public class RecipeBook {
	
	public static final String AUTHORITY = "com.captainfanatic.bites.RecipeBook";
	
	// This class cannot be instantiated
    private RecipeBook() {}
    
    /**
     * Recipes table
     */
    public static final class Recipe implements BaseColumns {
        // This class cannot be instantiated
        private Recipe() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipes");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.captainfanatic.recipe";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.captainfanatic.recipe";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * The title of the recipe
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";
        
        /**
         * The rating of the recipe
         * <P>Type: TEXT</P>
         */
        public static final String RATING = "rating";

        /**
         * The timestamp for when the recipe was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the recipe was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
    
    /**
     * Ingredients table
     */
    public static final class Ingredients implements BaseColumns {
        // This class cannot be instantiated
        private Ingredients() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/ingredients");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of ingredients.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.captainfanatic.ingredient";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single ingredient.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.captainfanatic.ingredient";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /**
         * The ingredients of the recipe
         * <P>Type: TEXT</P>
         */
        public static final String INGREDIENT = "ingredient";

        /**
         * The quantity of the ingredient
         * <P>Type: TEXT</P>
         */
        public static final String QUANTITY = "quantity";

        /**
         * The timestamp for when the recipe was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the recipe was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * Methods table
     */
    public static final class Methods implements BaseColumns {
        // This class cannot be instantiated
        private Methods() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/methods");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of method steps.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.captainfanatic.method";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single method step.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.captainfanatic.method";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
      
        /**
         * The method text
         * <P>Type: TEXT</P>
         */
        public static final String METHOD = "method";

        /**
         * The step number of the method step
         * <P>Type: TEXT</P>
         */
        public static final String STEP = "step";

        
        /**
         * The timestamp for when the recipe was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the recipe was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }

}
