package captainfanatic.bites;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

//TODO: handle sms message text in intent from SmsReceiver

public class Bites extends Activity {
	ViewFlipper flipper;
	TextView text;
	SmsReceiver sms;
	Button btn; 
	
	ArrayList<Recipe> mRecipeList;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        flipper=(ViewFlipper)findViewById(R.id.details);
        text = (TextView)findViewById(R.id.TextView01);
        mRecipeList = new ArrayList<Recipe>();
        String strXml = new String();
        Recipe recipe = new Recipe();
        try {
			strXml = recipe.Push();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		strXml = strXml.replaceAll("apples", "pears");
		try {
			recipe.Pull(strXml);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		text.setText(strXml);
        
        btn = (Button)findViewById(R.id.flip_me);
        
        btn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		flipper.showNext();
        	}
        });
    }

	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.main);
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
    
}