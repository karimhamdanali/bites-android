package captainfanatic.bites;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Bites extends Activity {
	ViewFlipper flipper;
	TextView text;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        flipper=(ViewFlipper)findViewById(R.id.details);
        text = (TextView)findViewById(R.id.TextView01);
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
        
        Button btn = (Button)findViewById(R.id.flip_me);
        
        btn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		flipper.showNext();
        	}
        });
    }
}