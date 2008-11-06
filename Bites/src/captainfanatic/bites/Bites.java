package captainfanatic.bites;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class Bites extends Activity {
	ViewFlipper flipper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        flipper=(ViewFlipper)findViewById(R.id.details);
        
        Button btn = (Button)findViewById(R.id.flip_me);
        
        btn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		flipper.showNext();
        	}
        });
    }
}