package kodemma.android.sliderpuzzle;

import java.io.InputStream;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
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
	private Button btnChoose, btnPlayActCall, btnRankingActCall,btnTitleActCall;
	private ImageView iv;
	private Bitmap bmp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// level spinner ---------------------------------------
		spnlvl = (Spinner) this.findViewById(R.id.spinner1);
		String[] str = new String[Level.levels().size()];
		int i=0;
		for (Level v : Level.levels().values()) { str[i]=v.text(); i++;}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, str);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnlvl.setAdapter(adapter);
		spnlvl.setPromptId(R.string.conf_level);
		spnlvl.setSelection(getSharedPreferences("pref", MODE_PRIVATE).getInt("level", 1)-1);
		spnlvl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> v, View view, int pos, long id) {
				Spinner spinner = (Spinner) v;
				int res = spinner.getSelectedItemPosition();
				SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
				Editor e = pref.edit();
				e.putInt("level", res+1);e.commit();
			}
			public void onNothingSelected(AdapterView<?> v) {}
		});
		// hint ------------------------------------------------
		cbhint = (CheckBox) this.findViewById(R.id.checkBox1);
		cbhint.setChecked(getSharedPreferences("pref", MODE_PRIVATE).getBoolean("hint", false));
		cbhint.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cdhint, boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				 if (cbhint.isChecked() == true) { e.putBoolean("hint", true);e.commit(); }
				 else if (cbhint.isChecked() == false) { e.putBoolean("hint",false); e.commit(); }
			}
		});
		// tile ------------------------------------------------
		cbtile = (CheckBox) this.findViewById(R.id.checkBox2);
		cbtile.setChecked(getSharedPreferences("pref", MODE_PRIVATE).getBoolean("tile", false));
		cbtile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cbtile, boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				 if (cbtile.isChecked() == true) { e.putBoolean("tile", true);e.commit(); }
				 else if (cbtile.isChecked() == false) { e.putBoolean("tile",false); e.commit(); }
			}
		});
		// =================== current screen transitions to Call screen.
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
		iv = (ImageView) findViewById(R.id.imageview1);
		iv.setImageURI(Uri.parse(getSharedPreferences("pref", MODE_PRIVATE).getString("uri", DEFAULT_IMAGE_URI)));
	}

	// =================== gallery result ================= //
	public void onActivityResult(int reqcode, int result, Intent it) {
		if (reqcode == KDMA_SLIDE && result == RESULT_OK) {
			Uri u = it.getData();
			try {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				Cursor cursor = getContentResolver().query(u, null, null, null,null);
				cursor.moveToFirst();

				String filename = "content://media/external/images/media/"+ cursor.getString(0);
						
				cursor.close();
				e.putString("uri", filename);e.commit();

				InputStream is = getContentResolver().openInputStream(u);

				try {
					bmp = BitmapFactory.decodeStream(is);
				} catch (OutOfMemoryError ome) {
					ome.printStackTrace();
					// メモリ不足の場合は、ダイアログを表示し、デフォルトのドロイドアイコンの画像を設定する。
					Resources res = getResources();
					new AlertDialog.Builder(this)
							.setTitle(R.string.alart_title_outOfMemoey)
							.setMessage(R.string.alart_message_outOfMEmory)
							.setPositiveButton(R.string.str_ok, null).show();
					bmp = BitmapFactory.decodeResource(res,R.drawable.ic_launcher);
				}
				iv.setImageBitmap(bmp);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// =================== Button Click Listener ================= //
	class BtnClickListner implements OnClickListener {
		public void onClick(View v) {
			Intent it = null;
			if( v == btnPlayActCall ){ it = new Intent(SelectLevelActivity.this, BoardActivity.class);}
//			else if( v == btnRankingActCall ){ it = new Intent(SelectLevelActivity.this, RankingActivity.class);}
//			else if( v == btnTitleActCall ){ it = new Intent(SelectLevelActivity.this, TitleActivity.class);}
			startActivity(it);
		}
	}

	// =================== gallery listener class ================= //
	class ChooseClickListner implements OnClickListener {
		public void onClick(View v) {
			Intent it = new Intent();
			it.setType("image/*");
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