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

	public static int KDMA_SLIDE = 1;
	private Spinner spnlvl; // Level setting
	private CheckBox cbhint, cbtile; // HINT, TILE
	private Button btnChoose, btnPlayActCall, btnRankingActCall, btnTitleActCall;

	@SuppressWarnings("null")
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// level spinner ---------------------------------------
		spnlvl = (Spinner) this.findViewById(R.id.spinner1);
		
		String[] str = new String[10];
		int i=0;
		for (Level v : Level.levels().values()) { str[i]=v.text(); i++;}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnlvl.setAdapter(adapter);
		spnlvl.setPromptId(R.string.conf_level);
		spnlvl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> v, View view, int pos,long id) {
				Spinner spinner = (Spinner) v;
				Integer res =  spinner.getSelectedItemPosition();
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				e.putInt("level", res+1);e.commit();
			}
			public void onNothingSelected(AdapterView<?> v) {}
		});
		// hint ------------------------------------------------
		cbhint = (CheckBox) this.findViewById(R.id.checkBox1);
		cbhint.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cdhint,boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				if (cbhint.isChecked() == true) { e.putBoolean("hint", true); e.commit(); }
				else if (cbhint.isChecked() == false) { e.putBoolean("hint", false); e.commit(); }
			}
		});
		// tile ------------------------------------------------
		cbtile = (CheckBox) this.findViewById(R.id.checkBox2);
		cbtile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cbtile,boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				if (cbhint.isChecked() == true) { e.putBoolean("tile", true); e.commit(); }
				else if (cbhint.isChecked() == false) { e.putBoolean("tile", false); e.commit(); }
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
	@SuppressWarnings("null")
	public void onActivityResult(int reqcode, int result, Intent it){
		ImageView iv = (ImageView) findViewById(R.id.imageview1);
		if( reqcode == KDMA_SLIDE && result == RESULT_OK ){
			Uri u = it.getData();
			try{
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				Cursor cursor = getContentResolver().query(u, null, null, null, null);
				cursor.moveToFirst();

				String filename = "content://media/external/images/media/" + cursor.getString(0); 
				cursor.close();
				e.putString("uri", filename);e.commit();
				
				Bitmap bmp = null;
				bmp.recycle();
				if(bmp.isRecycled()){ // countermeasure OutOfMemoryError
					InputStream is = getContentResolver().openInputStream(u);
					bmp = BitmapFactory.decodeStream(is);
					iv.setImageBitmap(bmp);
				}
			}catch(Exception e){ e.printStackTrace(); }
		}
	}
	// =================== Button Click Listener ================= // 
	class BtnClickListner implements OnClickListener{
		public void onClick(View v){
			Intent it = null;
			if( v == btnPlayActCall ){ it = new Intent(SelectLevelActivity.this, BoardActivity.class);}
			else if( v == btnRankingActCall ){ it = new Intent(SelectLevelActivity.this, RankingActivity.class);}
			else if( v == btnTitleActCall ){ it = new Intent(SelectLevelActivity.this, TitleActivity.class);}
			startActivity(it);
		}
	}
	// =================== gallery listener class ================= //
	class ChooseClickListner implements OnClickListener{
		public void onClick(View v){
			Intent it = new Intent();
			it.setType("image/*");
			it.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(it, KDMA_SLIDE);
		}
	}
	// =================== preferences getter methods. ================= //
	public static Integer getLevelSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getInt("level", 0); 
	}
	public static Boolean getHintSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getBoolean("hint", false);	
	}
	public static Boolean getTileSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getBoolean("tile", false);	
	}
	public static Uri getImgUriSetting(Context context) {
		return Uri.parse(context.getSharedPreferences("pref", MODE_PRIVATE).getString("uri", null));
	}
}