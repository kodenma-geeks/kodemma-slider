package kodemma.android.sliderpuzzle;


import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TitleActivity extends SharedMenuActivity {
//	GifView gifView;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title);
		setMoveActivity();
		
//		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout1);
//		gifView = new GifView(this);
//		gifView.setGif(R.drawable.logo);
//		ll.addView(gifView);
	}

	private void setMoveActivity() {
		ImageView image1 = (ImageView) findViewById(R.id.imageView2);
		ImageView image2 = (ImageView) findViewById(R.id.imageView3);
		ImageView image3 = (ImageView) findViewById(R.id.imageView4);
		ImageView image4 = (ImageView) findViewById(R.id.imageView5);

		image1.setClickable(true);
		image2.setClickable(true);
		image3.setClickable(true);
		image4.setClickable(true);

		image1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 Intent intent = new Intent(TitleActivity.this, BoardActivity.class);
				 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 startActivity(intent);
				 finish();
			}
		});
		image2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 Intent intent = new Intent(TitleActivity.this, RankingActivity.class);
				 startActivity(intent);
				 finish();
			}
		});
		image3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 Intent intent = new Intent(TitleActivity.this, SelectLevelActivity.class);
				 startActivity(intent);
				 finish();
			}
		});
		image4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//finish();
				killAllActivities();	// 自身も含めたすべてのアク????ビティを終??????
			}
		});

	}
	public boolean onKeyDown(int keyCode, KeyEvent event){ //7.12ハードキーのバックキーを押された場合
		if (keyCode == KeyEvent.KEYCODE_BACK){ //戻りボタンの処理
			Toast.makeText(this, "Please press somthing button", Toast.LENGTH_SHORT).show();
			return false;
		}else{return super.onKeyDown(keyCode, event);}
	}
}