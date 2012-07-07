package kodemma.android.sliderpuzzle;

import java.io.InputStream;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;

public class SelectLevelActivity extends SharedMenuActivity {

	private static final int KDMA_SLIDE = 1;
	private static final String DEFAULT_IMAGE_URI = Utils.getResourceUri(R.drawable.ega);
	
	private Spinner spnlvl; // Level setting
	private CheckBox cbhint, cbtile; // HINT, TILE
	private Button btnChoose, btnPlayActCall, btnRankingActCall, btnTitleActCall;
	
	Intent it = null;	// 浜田　追記7/5
	Integer res;		// 浜田　追記7/5
	boolean isHint;		// 浜田　追記7/5
	boolean isGrid;		// 浜田　追記7/5
	String filename;	// 浜田　追記7/5
	Bitmap bmp;			// 浜田　追記7/5
	GameStatus status;	// 浜田　追記7/5
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// level spinner ---------------------------------------
		spnlvl = (Spinner) this.findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ary_lv, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		it = getIntent();	// 浜田　追記7/5
		isHint = it.getBooleanExtra("HINT", true);	// 浜田　追記7/5
		isGrid = it.getBooleanExtra("TILE", true);	// 浜田　追記7/6
		Bundle b = it.getExtras();	// 浜田　追記7/5
		status = (GameStatus)b.get("STATUS");	// 浜田　追記7/5

		spnlvl.setAdapter(adapter);
		spnlvl.setPromptId(R.string.conf_level);
		spnlvl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> v, View view, int pos,long id) {
				Spinner spinner = (Spinner) v;
				res =  spinner.getSelectedItemPosition();
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				e.putInt("level", res);e.commit();
			}

			public void onNothingSelected(AdapterView<?> v) {}
		});
		if(status == GameStatus.PLAYING) spnlvl.setEnabled(false);	// 浜田　追記7/5

		// hint ------------------------------------------------
		cbhint = (CheckBox) this.findViewById(R.id.checkBox1);
		cbhint.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cdhint,boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				isHint = cbhint.isChecked()? true : false; // 浜田　 書き方を修正 7/5
				e.putBoolean("hint", isHint); e.commit();
//				if (cbhint.isChecked() == true) { e.putBoolean("hint", true); e.commit(); }
//				else if (cbhint.isChecked() == false) { e.putBoolean("hint", false); e.commit(); }
			}
		});
		// tile ------------------------------------------------
		cbtile = (CheckBox) this.findViewById(R.id.checkBox2);
		cbtile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cbtile,boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				isGrid = cbtile.isChecked()? true : false; // 浜田　 書き方を修正 7/5
				e.putBoolean("hint", isGrid); e.commit();
//				if (cbhint.isChecked() == true) { e.putBoolean("tile", true); e.commit(); }
//				else if (cbhint.isChecked() == false) { e.putBoolean("tile", false); e.commit(); }
			}
		});
		// =================== current screen transitions to Call screen. ================= //
		// button Play Activity Call
		btnPlayActCall = (Button) this.findViewById(R.id.button2);
		btnPlayActCall.setOnClickListener(new BtnClickListner());
		// button Ranking Activity Call
		btnRankingActCall = (Button) this.findViewById(R.id.button3);
		btnRankingActCall.setOnClickListener(new BtnClickListner());
		// button Exit(Title) Activity Call
		btnTitleActCall = (Button) this.findViewById(R.id.button4);
		btnTitleActCall.setOnClickListener(new BtnClickListner());
		// button picture Choose 
		btnChoose = (Button) this.findViewById(R.id.button1);
		btnChoose.setOnClickListener(new ChooseClickListner());
	}
	// =================== gallery result  ================= //
	public void onActivityResult(int reqcode, int result, Intent it){
		ImageView iv = (ImageView) findViewById(R.id.imageview1);
		if( reqcode == KDMA_SLIDE && result == RESULT_OK ){
			Uri u = it.getData();
			try{
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				Cursor cursor = getContentResolver().query(u, null, null, null, null);
				cursor.moveToFirst();

				filename = "content://media/external/images/media/" + cursor.getString(0); 
				cursor.close();
				e.putString("uri", filename);e.commit();

				InputStream is = getContentResolver().openInputStream(u);
				bmp = BitmapFactory.decodeStream(is);
				iv.setImageBitmap(bmp);
			}catch(Exception e){}
		}
	}
	// =================== Button Click Listener ================= // 
	class BtnClickListner implements OnClickListener{
		public void onClick(View v){
			switch (v.getId()) {	// 浜田　switch{}追記7/5
			case R.id.button2:
				// button Play Activity Call"level", res
//				it.putExtra("LEVEL", res);
				it.putExtra("HINT", isHint);
				it.putExtra("TILE", isGrid);
//				it.putExtra("PICTURE", bmp);
				setResult(RESULT_OK, it);
				finish();
				break;
			case R.id.button3:
				// button Ranking Activity Call
				break;
			case R.id.button4:
				// button Exit(Title) Activity Call
				break;
			}
//			Intent it = null;
//
//			startActivity(it);
		}
	}
	// =================== gallery listener class ================= //
	class ChooseClickListner implements OnClickListener{
		public void onClick(View v){
			Intent it = new Intent();
			it.setType("image/*");
//			it.setAction(Intent.ACTION_GET_CONTENT);
			it.setAction(Intent.ACTION_PICK);
			startActivityForResult(it, KDMA_SLIDE); 
		}
	}
	// =================== preferences getter methods. ================= //
	public static Integer getLevelSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getInt("level", 1); 
	}
	public static Boolean getHintSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getBoolean("hint", false);	
	}
	public static Boolean getTileSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getBoolean("tile", false);	
	}
	public static Uri getImgUriSetting(Context context) {
		return Uri.parse(context.getSharedPreferences("pref", MODE_PRIVATE).getString("uri", DEFAULT_IMAGE_URI));
	}
}