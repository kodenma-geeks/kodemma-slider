package kodemma.android.sliderpuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;

public class SharedMenuActivity extends Activity{

	@Override public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.str_confirm);
		final String msg;
		switch(item.getItemId()){
			case R.id.menu_dialog: msg = "Quit Game?"; break;
			default: msg = "Error!"; break;
		}
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.str_ok, new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setResult(RESULT_OK);
				finish();
			}
		});
		builder.setNegativeButton(R.string.str_cancel, null).create().show();
		return true;
	}
}