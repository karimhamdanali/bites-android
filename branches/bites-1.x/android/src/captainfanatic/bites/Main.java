/*Bites - an organiser for cooks
 * Copyright (C) 2008 Ben Caldwell <benny.caldwell@gmail.com>
 * The Bites project lives at http://code.google.com/p/bites-android/
 *
 * Bites is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bites is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bites.  If not, see <http://www.gnu.org/licenses/>.
 * */


package captainfanatic.bites;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

import captainfanatic.bites.InventoryList;

public class Main extends Activity
{

	private Button btnInventory;
	private Button btnRecipes;
	private Button btnShoppingList;
	private Button btnMealPlanner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);

	btnInventory = (Button)findViewById(R.id.btn_Inventory);
	btnRecipes = (Button)findViewById(R.id.btn_Recipes);
	btnShoppingList = (Button)findViewById(R.id.btn_ShoppingList);
	btnMealPlanner = (Button)findViewById(R.id.btn_MealPlanner);

	btnInventory.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(Intent.VIEW_ACTION, captainfanatic.bites.Provider.Inventory.CONTENT_URI);	
			startActivity(intent);	
		}
	});
	btnRecipes.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(Intent.VIEW_ACTION, captainfanatic.bites.Provider.Recipes.CONTENT_URI);	
			startActivity(intent);	
		}
	});
	btnShoppingList.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(Intent.VIEW_ACTION, captainfanatic.bites.Provider.ShoppingList.CONTENT_URI);	
			startActivity(intent);	
		}
	});
	btnMealPlanner.setOnClickListener(new Button.OnClickListener(){
		public void onClick(View v) {
			Intent intent = new Intent(Intent.VIEW_ACTION, captainfanatic.bites.Provider.MealPlannerView.CONTENT_URI);	
			startActivity(intent);	
		}
	});
    }
}
