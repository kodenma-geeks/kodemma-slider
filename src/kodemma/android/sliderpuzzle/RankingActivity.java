package kodemma.android.sliderpuzzle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends Activity {

	TextView tvRnk;

	private ImageButton ivTitle;
	private ImageButton ivUp;
	private ImageButton ivDown;
	private RankingOpenHelper dbHelper;
	private SQLiteDatabase db;
	private TableLayout tbLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank);

		// データ受取処理

		int timeKey = 999;
		int countKey = 10000;
		int panelKey = 2;
		int level = 0;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			timeKey = extras.getInt("Laptime");
			countKey = extras.getInt("Slidecount");
			panelKey = extras.getInt("panels");
		}
		Toast.makeText(this, "test---->    " + timeKey, Toast.LENGTH_LONG)
				.show();
		for (Level l : Level.levels().values()) {
			if (panelKey == l.tiles()) {
				level = l.level();
			}
		}

		tvRnk = (TextView) findViewById(R.id.textView_level);

		ivTitle = (ImageButton) findViewById(R.id.imageButton_title);
		ivUp = (ImageButton) findViewById(R.id.imageButton_levelup);
		ivDown = (ImageButton) findViewById(R.id.imageButton_leveldown);

		tbLayout = (TableLayout) findViewById(R.id.ranklist);

		// データベースヘルパーのインスタンス生成
		dbHelper = new RankingOpenHelper(this);
		// データベースオブジェクトを取得する
		db = dbHelper.getWritableDatabase();

		// データベースを閉じる
		// db.close();

		db.beginTransaction();

		Insert(timeKey, countKey, panelKey);

		// ～位以下削除
		db.delete("ranking_table", "_id >= 30", null);

		db.setTransactionSuccessful();
		db.endTransaction();

		read();

		// イベントリスナー登録
		ivTitle.setOnClickListener(new ImgButtonsClickListener());
		ivUp.setOnClickListener(new ImgButtonsClickListener());
		ivDown.setOnClickListener(new ImgButtonsClickListener());
	}

	// ボタンクリックイベント
	class ImgButtonsClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO 自動生成されたメソッド・スタブ
			switch (v.getId()) {

			case R.id.imageButton_title:

				Toast.makeText(getApplicationContext(), "タイトルへ",
						Toast.LENGTH_SHORT).show();
				titleBack();
				break;

			case R.id.imageButton_leveldown:

				Toast.makeText(getApplicationContext(), "下レベルへ",
						Toast.LENGTH_SHORT).show();

				/* tbLayout.removeAllViews(); */

				ContentValues values = new ContentValues();
				values.put("count", 900);

				db.update("ranking_table", values, null, null);

				break;

			case R.id.imageButton_levelup:

				tvRnk.setText("4×4LEVEL");

				tbLayout.removeAllViews();

				SQLiteDatabase db = dbHelper.getReadableDatabase();

				Cursor cursor = db.query("ranking_table", new String[] {
						"rank", "score", "count", "time", "panels" },
						"panels == ?", new String[] { "1" }, null, null, null);

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

				while (cursor.moveToNext()) {

					TableRow row = new TableRow(RankingActivity.this);

					TextView ranktxt = new TextView(RankingActivity.this);
					ranktxt.setGravity(Gravity.CENTER_HORIZONTAL);
					ranktxt.setText(cursor.getString(0) + "位");

					TextView scoretxt = new TextView(RankingActivity.this);
					scoretxt.setGravity(Gravity.CENTER_HORIZONTAL);
					scoretxt.setText(cursor.getString(1));

					TextView counttxt = new TextView(RankingActivity.this);
					counttxt.setGravity(Gravity.CENTER_HORIZONTAL);
					counttxt.setText(cursor.getString(2));

					TextView timetxt = new TextView(RankingActivity.this);
					timetxt.setGravity(Gravity.CENTER_HORIZONTAL);
					timetxt.setText(cursor.getString(3));

					row.addView(ranktxt);
					row.addView(scoretxt);
					row.addView(counttxt);
					row.addView(timetxt);

					tbLayout.addView(row);
				}

				cursor.close(); // カーソルクローズ

				Toast.makeText(getApplicationContext(), "上レベルへ",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}

	// ランキング表示
	private void read() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query("ranking_table", new String[] { "rank",
				"score", "count", "time", "panels" }, "panels == ?",
				new String[] { "9" }, null, null, null);

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

		while (cursor.moveToNext()) {

			TableRow row = new TableRow(RankingActivity.this);

			TextView ranktxt = new TextView(RankingActivity.this);
			ranktxt.setGravity(Gravity.CENTER_HORIZONTAL);
			ranktxt.setText(cursor.getString(0) + "位");

			TextView scoretxt = new TextView(RankingActivity.this);
			scoretxt.setGravity(Gravity.CENTER_HORIZONTAL);
			scoretxt.setText(cursor.getString(1));

			TextView counttxt = new TextView(RankingActivity.this);
			counttxt.setGravity(Gravity.CENTER_HORIZONTAL);
			counttxt.setText(cursor.getString(2));

			TextView timetxt = new TextView(RankingActivity.this);
			timetxt.setGravity(Gravity.CENTER_HORIZONTAL);
			timetxt.setText(cursor.getString(3));

			row.addView(ranktxt);
			row.addView(scoretxt);
			row.addView(counttxt);
			row.addView(timetxt);

			tbLayout.addView(row);
		}

		cursor.close(); // カーソルクローズ

	}

	public void titleBack() {

		Intent intent = new Intent();
		intent.setClassName(getPackageName(), getPackageName()
				+ ".TitleActivity");

		startActivity(intent);
	}

	public void Insert(int time, int count, int panel) {

		ContentValues val = new ContentValues();

		val.put("rank", 1);
		val.put("score", 100);
		val.put("count", count);
		val.put("time", time);
		val.put("panels", panel);
		db.insert("ranking_table", null, val);

	}

}
