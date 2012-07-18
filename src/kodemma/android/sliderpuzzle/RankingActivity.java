package kodemma.android.sliderpuzzle;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends SharedMenuActivity{

	private TextView tvRnk;
	private Button ivTitle;
	private ImageButton ivUp, ivDown;
	private RankingOpenHelper dbHelper;
	private SQLiteDatabase db;
	private TableLayout tbLayout;
	private int level, lvlmode;
	private String strlvl;
	private float first = 24;
	private float second = 22;
	private float third = 20;
	private float normal = 18;
	private long insid;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank);
		
		level = SelectLevelActivity.getLevelSetting(this);
		lvlmode = level;
		strlvl = Level.get(level).text();
		
		tvRnk = (TextView) findViewById(R.id.textView_level);
		tvRnk.setText("LEVEL "+strlvl);
		tvRnk.setTextSize(first);
		ivTitle = (Button) findViewById(R.id.ranking_button_title);
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
			String gettime = getFormatTime(timeKey);
			// Ranking Dialog message
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Your Score")
			.setMessage("score : "+score+"\ncount : "+countKey+"\ntime : "+gettime)
			.setPositiveButton("OK",null).show();
		}
		
		db.delete("ranking_table", "_id >= 100", null); // ～位以下削除
		db.setTransactionSuccessful();
		db.endTransaction();

		if(Level.min().level() < lvlmode || Level.max().level()> lvlmode){
			if( Level.max().level() < lvlmode || lvlmode == 10){
				strlvl = Level.get(lvlmode).text();
				tvRnk.setText("LEVEL "+strlvl);
				tvRnk.setTextSize(first);
				read(lvlmode);		// Data Select
				ivDown.setEnabled(true);
				ivUp.setEnabled(false);
			}else if( Level.min().level() > lvlmode || lvlmode == 1){
				strlvl = Level.get(lvlmode).text();
				tvRnk.setText("LEVEL "+strlvl);
				tvRnk.setTextSize(first);
				read(lvlmode);		// Data Select
				ivDown.setEnabled(false);
				ivUp.setEnabled(true);
			}else{
				strlvl = Level.get(lvlmode).text();
				tvRnk.setText("LEVEL "+strlvl);
				tvRnk.setTextSize(first);
				read(lvlmode);		// Data Select
				ivDown.setEnabled(true);
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
			case R.id.ranking_button_title: // Title Activity Call.
				
				titleBack();break;
				
			case R.id.imageButton_leveldown: // Down Level expression

				lvlmode = lvlmode-1;
				tbLayout.removeAllViews();
				
				if(Level.min().level() < lvlmode || Level.max().level()> lvlmode){
					if( Level.min().level() < lvlmode ){
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						tvRnk.setTextSize(first);
						read(lvlmode);
						ivDown.setEnabled(true);
						ivUp.setEnabled(true);
					}else{
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						tvRnk.setTextSize(first);
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
						tvRnk.setTextSize(first);
						
						read(lvlmode);
						ivUp.setEnabled(true);
						ivDown.setEnabled(true);
					}else{
						strlvl = Level.get(lvlmode).text();
						tvRnk.setText("LEVEL "+strlvl);
						tvRnk.setTextSize(first);
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
		Cursor cursor = db.query("ranking_table", new String[] {"_id", "rank",
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
		int txtcolor = Color.WHITE;
		float txtsize = normal;
		
		while (cursor.moveToNext()) {
			long current_id = cursor.getLong(0) ;
			if(current_id == insid){ txtcolor = Color.YELLOW; }else{ txtcolor = Color.WHITE; }
			
			TableRow row = new TableRow(RankingActivity.this);
			
			switch(num){
			case 1:txtsize=first;break;
			case 2:txtsize=second;break;
			case 3:txtsize=third;break;
			}
			
			TextView ranktxt = new TextView(RankingActivity.this);
			ranktxt.setGravity(Gravity.CENTER_HORIZONTAL);
			ranktxt.setText(""+num);
			ranktxt.setTextSize(txtsize);
			ranktxt.setTextColor(txtcolor);

			TextView scoretxt = new TextView(RankingActivity.this);
			scoretxt.setGravity(Gravity.CENTER_HORIZONTAL);
			scoretxt.setText(cursor.getString(2));
			scoretxt.setTextSize(txtsize);
			scoretxt.setTextColor(txtcolor);
				
			TextView counttxt = new TextView(RankingActivity.this);
			counttxt.setGravity(Gravity.CENTER_HORIZONTAL);
			counttxt.setText(cursor.getString(3));
			counttxt.setTextSize(txtsize);
			counttxt.setTextColor(txtcolor);

			TextView timetxt = new TextView(RankingActivity.this);
			timetxt.setGravity(Gravity.CENTER_HORIZONTAL);
			timetxt.setText(cursor.getString(4));
			timetxt.setTextSize(txtsize);
			timetxt.setTextColor(txtcolor);

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
		insid = db.insert("ranking_table", null, val);
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
