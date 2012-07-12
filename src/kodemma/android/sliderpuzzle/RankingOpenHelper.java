package kodemma.android.sliderpuzzle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RankingOpenHelper extends SQLiteOpenHelper {

	final static private int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "slider_db";

	public RankingOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {

			String sql = "";
			sql += "CREATE TABLE ranking_table (";
			sql += "_id INTEGER PRIMARY KEY AUTOINCREMENT";
			sql += ",rank INTEGER";  //integer
			sql += ",score INTEGER";
			sql += ",count INTEGER";
			sql += ",time INTEGER";
			sql += ",panels INTEGER";

			sql += ")";
			db.execSQL(sql);

		} catch (Exception e) {
			Log.e("ERROR", e.toString());
	}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ

	}
	

}
