package kodemma.android.sliderpuzzle;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends SharedMenuActivity{

	private TextView tvRnk;
	private ImageButton ivTitle, ivUp, ivDown;
	private RankingOpenHelper dbHelper;
	private SQLiteDatabase db;
	private TableLayout tbLayout;
	private int level, lvlmode;
	private String strlvl;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank);
		
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		level = SelectLevelActivity.getLevelSetting(this);
		lvlmode = level;
		strlvl = Level.get(level).text();
		
		tvRnk = (TextView) findViewById(R.id.textView_level);
		tvRnk.setText("LEVEL "+strlvl);
		ivTitle = (ImageButton) findViewById(R.id.imageButton_title);
		ivUp = (ImageButton) findViewById(R.id.imageButton_levelup);
		ivDown = (ImageButton) findViewById(R.id.imageButton_leveldown);
		tbLayout = (TableLayout) findViewById(R.id.ranklist);

		dbHelper = new RankingOpenHelper(this);
		db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) { // データ受取処理
			long timeKey = extras.getLong("Laptime");
			int countKey = extras.getInt("Slidecount");
			Insert(timeKey, countKey, level); // Data Insert
			int score = Level.get(level).score(timeKey, countKey);
			String aaaa = getFormatTime(timeKey);
//			alertDialogBuilder.setMessage("score:"+score+"\n"+"time:"+aaaa+"count:"+countKey);
//			alertDialogBuilder.create().show();
			Toast.makeText(getApplicationContext(), "score:"+score+"\n"+"time:"+aaaa, Toast.LENGTH_LONG).show();
		}
		
		db.delete("ranking_table", "_id >= 100", null); // ～位以下削除
		db.setTransactionSuccessful();
		db.endTransaction();

		if(Level.min().level() < lvlmode || Level.max().level()> lvlmode){
			if( Level.min().level() < lvlmode ){
				strlvl = Level.get(lvlmode).text();
				tvRnk.setText("LEVEL "+strlvl);
				read(lvlmode);		// Data Select
				ivDown.setEnabled(true);
				ivUp.setEnabled(true);
			}else{
				strlvl = Level.get(lvlmode).text();
				tvRnk.setText("LEVEL "+strlvl);
				read(lvlmode);		// Data Select
				ivDown.setEnabled(false);
				ivUp.setEnabled(true);
			}
		}
		
		ivTitle.setOnClickListener(new ImgButtonsClickListener());
		ivUp.setOnClickListener(new ImgButtonsClickListener());
		ivDown.setOnClickListener(new ImgButtonsClickListener());
	}

	class ImgButtonsClickListener implements OnClickListener { // ボタンクリックイベント
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imageButton_title: // Title Activity Call.
				
				titleBack();break;
				
			case R.id.imageButton_leveldown: // Down Level expression

				lvlmode = lvlmode-1;
				tbLayout.removeAllViews();
				
				if(Level.min().level() < lvlmode || Level.max().level()> lvlmode){
					if( Level.min().level() < lvlmode ){
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						read(lvlmode);
						ivDown.setEnabled(true);
						ivUp.setEnabled(true);
					}else{
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						read(lvlmode);
						ivDown.setEnabled(false);
						ivUp.setEnabled(true);
					}
				}
				break;

			case R.id.imageButton_levelup: // Up Level expression

				lvlmode = lvlmode+1;
				tbLayout.removeAllViews();

				if(Level.min().level()< lvlmode || Level.max().level()> lvlmode){
					if( Level.max().level() > lvlmode ){
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						read(lvlmode);
						ivUp.setEnabled(true);
						ivDown.setEnabled(true);
					}else{
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						read(lvlmode);
						ivUp.setEnabled(false);
						ivDown.setEnabled(true);
					}
				}
				break;
			}
		}
	}

	private void read( Integer lvl ) { // Ranking Expression.
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("ranking_table", new String[] { "rank",
				"score", "count", "time", "panels" }, "panels == ?",
				new String[] { Integer.toString(lvl) }, null, null, "score desc");

		tbLayout.setStretchAllColumns(true);

		TableRow headrow = new TableRow(RankingActivity.this);
		
		TextView rankHead = new TextView(RankingActivity.this);
		rankHead.setGravity(Gravity.CENTER_HORIZONTAL);
		rankHead.setText(R.string.rankID);

		TextView scoreHead = new TextView(RankingActivity.this);
		scoreHead.setGravity(Gravity.CENTER_HORIZONTAL);
		scoreHead.setText(R.string.scoreID);

		TextView cntHead = new TextView(RankingActivity.this);
		cntHead.setGravity(Gravity.CENTER_HORIZONTAL);
		cntHead.setText(R.string.countID);

		TextView timeHead = new TextView(RankingActivity.this);
		timeHead.setGravity(Gravity.CENTER_HORIZONTAL);
		timeHead.setText(R.string.timeID);

		headrow.addView(rankHead);
		headrow.addView(scoreHead);
		headrow.addView(cntHead);
		headrow.addView(timeHead);

		tbLayout.addView(headrow);

		int num = 1;
		int opnRnk = 10;
		while (cursor.moveToNext()) {
			TableRow row = new TableRow(RankingActivity.this);
			
			TextView ranktxt = new TextView(RankingActivity.this);
			ranktxt.setGravity(Gravity.CENTER_HORIZONTAL);
//			ranktxt.setText(cursor.getString(0) + R.string.ranking_place);
			ranktxt.setText(""+num);
			if(num == 1){ranktxt.setTextSize(24.0f);}
			else if(num == 2){ranktxt.setTextSize(22.0f);}
			else if(num == 3){ranktxt.setTextSize(20.0f);}

			TextView scoretxt = new TextView(RankingActivity.this);
			scoretxt.setGravity(Gravity.CENTER_HORIZONTAL);
			scoretxt.setText(cursor.getString(1));
			if(num == 1){scoretxt.setTextSize(24.0f);}
			else if(num == 2){scoretxt.setTextSize(22.0f);}
			else if(num == 3){scoretxt.setTextSize(20.0f);}
				
			TextView counttxt = new TextView(RankingActivity.this);
			counttxt.setGravity(Gravity.CENTER_HORIZONTAL);
			counttxt.setText(cursor.getString(2));
			if(num == 1){counttxt.setTextSize(24.0f);}
			else if(num == 2){counttxt.setTextSize(22.0f);}
			else if(num == 3){counttxt.setTextSize(20.0f);}

			TextView timetxt = new TextView(RankingActivity.this);
			timetxt.setGravity(Gravity.CENTER_HORIZONTAL);
			timetxt.setText(cursor.getString(3));
			if(num == 1){timetxt.setTextSize(24.0f);}
			else if(num == 2){timetxt.setTextSize(22.0f);}
			else if(num == 3){timetxt.setTextSize(20.0f);}

			row.addView(ranktxt);
			row.addView(scoretxt);
			row.addView(counttxt);
			row.addView(timetxt);

			tbLayout.addView(row);
			num++;
			if(num > opnRnk){break;};
		}
		cursor.close();
	}

	public void Insert(long time, int count, int level) {
		ContentValues val = new ContentValues();
		int score = Level.get(level).score(time, count);
		
		val.put("rank", 1); // Unused
		val.put("score", score);
		val.put("count", count);
		val.put("time", getFormatTime(time));
		val.put("panels", level);
		db.insert("ranking_table", null, val);
	}
	
	public void titleBack() {
		Intent intent = new Intent();
		intent.setClassName(getPackageName(), getPackageName() + ".TitleActivity");
		startActivity(intent);
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event){ //7.12ハードキーのバックキーを押された場合
		if (keyCode == KeyEvent.KEYCODE_BACK){ //戻りボタンの処理
			Toast.makeText(this, "Please press title button", Toast.LENGTH_SHORT).show();
			return false;
		}else{return super.onKeyDown(keyCode, event);}
	}
	
	public String getFormatTime(long milliseconds){
		long time = milliseconds;
		time /= 1000;
		long second = time % 60;
		time /= 60;
		long minute = time % 60;
		long hour = time / 60;

		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
}
