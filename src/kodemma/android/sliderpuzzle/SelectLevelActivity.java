package kodemma.android.sliderpuzzle;

import java.io.FileNotFoundException;
import java.io.InputStream;
import android.app.AlertDialog;
import android.content.ContentResolver;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Spinner;

public class SelectLevelActivity extends SharedMenuActivity {

	private static final int KDMA_SLIDE = 1;
	private static final String DEFAULT_IMAGE_URI = Utils.getResourceUri(R.drawable.ic_launcher);
	private Spinner spnlvl; // Level setting
	private CheckBox cbhint, cbtile, cbsound; // HINT, TILE, SOUND
	private Button btnChoose, btnPlayActCall, btnRankingActCall,btnTitleActCall;
	private ImageView iv;
	private Bitmap bmp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		// 非常用ビットマップの読み込み ---------------------------------------
		getSurvivalBitmap(this);	// OutOfMemoryError発生時に、非常用ビットマップでさえも読めなくなるのを避けるため、最初に空読みしておく。
		// level spinner ---------------------------------------
		spnlvl = (Spinner) this.findViewById(R.id.id_conf_level_spn);
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
		cbhint = (CheckBox) this.findViewById(R.id.id_conf_cbhint);
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
		cbtile = (CheckBox) this.findViewById(R.id.id_conf_cbtile);
		cbtile.setChecked(getSharedPreferences("pref", MODE_PRIVATE).getBoolean("tile", false));
		cbtile.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cbtile, boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				 if (cbtile.isChecked() == true) { e.putBoolean("tile", true);e.commit(); }
				 else if (cbtile.isChecked() == false) { e.putBoolean("tile",false); e.commit(); }
			}
		});
		// tile ------------------------------------------------
		cbsound = (CheckBox) this.findViewById(R.id.id_conf_cbsound);
		cbsound.setChecked(getSharedPreferences("pref", MODE_PRIVATE).getBoolean("sound", true));
		cbsound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cbsound, boolean isChecked) {
				SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
				Editor e = pref.edit();
				 if (cbsound.isChecked() == true) { e.putBoolean("sound", true);e.commit(); }
				 else if (cbsound.isChecked() == false) { e.putBoolean("sound",false); e.commit(); }
			}
		});
		// =================== current screen transitions to Call screen.
		// button picture Choose
		btnChoose = (Button) this.findViewById(R.id.id_btnChoose);
		btnChoose.setOnClickListener(new ChooseClickListner());
		iv = (ImageView) findViewById(R.id.imageview1);
// 以下、削除 by shima
//		iv.setImageURI(Uri.parse(getSharedPreferences("pref", MODE_PRIVATE).getString("uri", DEFAULT_IMAGE_URI)));
// 以下、追加 by shima
		Bitmap bitmap = getBitmapSetting(this);
		iv.setImageBitmap(bitmap);
// 以上。　by shima
		// button Play Activity Call
		btnPlayActCall = (Button) this.findViewById(R.id.id_btnPlayActCall);
		btnPlayActCall.setOnClickListener(new BtnClickListner());
		// button Ranking Activity Call
		btnRankingActCall = (Button) this.findViewById(R.id.id_btnRankingActCall);
		btnRankingActCall.setOnClickListener(new BtnClickListner());
		// button Exit(Title) Activity Call
		btnTitleActCall = (Button) this.findViewById(R.id.id_btnTitleActCall);
		btnTitleActCall.setOnClickListener(new BtnClickListner());
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

// 以下、削除 by shima
//				InputStream is = getContentResolver().openInputStream(u);
//				try {
//					bmp = BitmapFactory.decodeStream(is);
//				} catch (OutOfMemoryError ome) {
//					ome.printStackTrace();
//					// メモリ不足の場合は、ダイアログを表示し、デフォルトのドロイドアイコンの画像を設定する。
//					Resources res = getResources();
//					new AlertDialog.Builder(this)
//							.setTitle(R.string.alart_title_outOfMemoey)
//							.setMessage(R.string.alart_message_outOfMEmory)
//							.setPositiveButton(R.string.str_ok, null).show();
//					bmp = BitmapFactory.decodeResource(res,R.drawable.ic_launcher);
//				}
//				iv.setImageBitmap(bmp);
// 以下、追加 by shima
				Bitmap bitmap = getBitmapSetting(this);
				iv.setImageBitmap(bitmap);
// 以上。　by shima
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// =================== Button Click Listener ================= //
	class BtnClickListner implements OnClickListener {
		public void onClick(View v) {
			Intent it = null;
			if( v == btnPlayActCall ){	// 浜田修正　7/12　プレイ途中のタイルを保持するためにIntentで戻ります。
				// button Play Activity Call"level", res 
				it = getIntent();
				boolean isGame = it.getBooleanExtra("GAME", false);
				
				if(isGame){
					setResult(RESULT_OK, it);
					finish();
				}else{
					it = new Intent(SelectLevelActivity.this, BoardActivity.class);
					startActivity(it);
				}
			}
			else if( v == btnRankingActCall ){ it = new Intent(SelectLevelActivity.this, RankingActivity.class);startActivity(it);}
			else if( v == btnTitleActCall ){ it = new Intent(SelectLevelActivity.this, TitleActivity.class);startActivity(it);}
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
	public static Boolean getSoundSetting(Context context) {
		return context.getSharedPreferences("pref", MODE_PRIVATE).getBoolean("sound", true);
	}
	public static Uri getImgUriSetting(Context context) {	// このメソッドは外部からは使用しない。代わりにBitmap getBitmapSetting()を使用するべし。
		return Uri.parse(context.getSharedPreferences("pref", MODE_PRIVATE).getString("uri", DEFAULT_IMAGE_URI));
	}
	// SharedPreferencesに保存されているURIからビットマップを読み込む。
	// 読み込みに失敗した場合は、非常用ビットマップ（ドロイド君）を返す。（その際は、そのビットマップをSharedPreferencesに書き込む）
	public static Bitmap getBitmapSetting(Context context) {
		Uri uri = SelectLevelActivity.getImgUriSetting(context);	// SharedPreferencesに保存されているURIを取得する。
		Bitmap bitmap = loadBitmap(context, uri);					// ビットマップを読み込む。（失敗した場合はnullが返る）
		if (bitmap == null) {
			SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
			Editor e = pref.edit();
			// 非常用ビットマップのURI文字列を取得し、SharedPreferencesに書き込む。
			String uriString = Utils.getResourceUri(SURVIVAL_BITMAP_RESOURCE_ID);
			e.putString("uri", uriString);
			e.commit();
			// 非常用ビットマップを取得する。	
			bitmap = getSurvivalBitmap(context);
		}
		return bitmap; 
	}
	// =================== Preventing OutOfMemory Error. ================= //
	// 非常用ビットマップのリソースID
	private static int SURVIVAL_BITMAP_RESOURCE_ID = R.drawable.ic_launcher;
	// 非常用ビットマップ
	private static Bitmap SURVIVAL_BITMAP = null;
	// 非常用のビットマップを取得する。
	private static Bitmap getSurvivalBitmap(Context context) {
		if (SURVIVAL_BITMAP == null)
			SURVIVAL_BITMAP = BitmapFactory.decodeResource(context.getResources(), SURVIVAL_BITMAP_RESOURCE_ID);
		return SURVIVAL_BITMAP;
	}
	// 指定されたURIのリソースを読み込み、ビットマップを生成する。　読み込みに失敗した場合はnullを返す。
	private static Bitmap loadBitmap(Context context, Uri uri) {
		int messageId = 0;
		ContentResolver contentResolver = context.getContentResolver();
		InputStream inputStream = null;
		try {
			inputStream = contentResolver.openInputStream(uri);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			messageId = R.string.alart_message_fileNotFound;
		} catch (Exception e) {
			e.printStackTrace();
			messageId = R.string.alart_message_unknownException;
		}
		Bitmap bitmap = null;
		if (messageId == 0) {
			try {
				bitmap = BitmapFactory.decodeStream(inputStream);
			} catch (OutOfMemoryError ome) {
				ome.printStackTrace();
				bitmap = null;
				messageId = R.string.alart_message_outOfMEmory;
			}
		}
		if (messageId != 0) {
			// 何らかのエラーが発生した場合は、アラート・ダイアログを表示する。
			new AlertDialog.Builder(context)
					.setTitle(R.string.alart_title_failureInLoadingBitmap)
					.setMessage(messageId)
					.setPositiveButton(R.string.str_ok, null).show();
		}
		return bitmap;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){ //7.12ハードキーのバックキーを押された場合
		if (keyCode == KeyEvent.KEYCODE_BACK){ //戻りボタンの処理
			Toast.makeText(this, "Please press title button", Toast.LENGTH_SHORT).show();
			return false;
		}else{return super.onKeyDown(keyCode, event);}
	}
}