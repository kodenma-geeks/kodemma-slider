package kodemma.android.sliderpuzzle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public class TitleActivity extends Activity {
	Button bt;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		bt = new Button(this);
		bt.setText("Title Activity");
		
		ll.addView(bt);
	}
}