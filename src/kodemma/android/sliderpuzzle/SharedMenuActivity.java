package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;

public class SharedMenuActivity extends Activity{

	// 本クラスを継承するすべてのアクティビティ・インスタンスを保持するリスト
	private static List<Activity> activities = new ArrayList<Activity>();
	
	protected SharedMenuActivity() {
		super();
		activities.add(this);	// このコンストラクタで、自身のインスタンスをリストに登録する。
	}
	// 自身も含めたすべてのアクティビティを終了する
	protected void killAllActivities() {
		activities.remove(this);						// 全アクティビティリストから自身を除去する。
		for (Activity a : activities) { a.finish(); }	// 自身以外の全アクティビティを終了する
		SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
		Editor e = pref.edit();
		e.remove("uri");e.commit();
		finish();
	}
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
				killAllActivities();	// 自身も含めたすべてのアクティビティを終了する
//				activities.remove(this);						// 全アクティビティリストから自身を除去する。
//				for (Activity a : activities) { a.finish(); }	// 自身以外の全アクティビティを終了する
//				finish();										// 自身のアクティビティを終了する
			}
		});
		builder.setNegativeButton(R.string.str_cancel, null).create().show();
		return true;
	}
}