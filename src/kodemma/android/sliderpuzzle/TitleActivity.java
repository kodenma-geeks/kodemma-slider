package kodemma.android.sliderpuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TitleActivity extends SharedMenuActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title);
		setMoveActivity();
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
				 Intent intent = new Intent(TitleActivity.this,
				 BoardActivity.class);
				 startActivity(intent);
			}
		});
		image2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				 Intent intent = new Intent(TitleActivity.this,
//				 RankingActivity.class);
//				 startActivity(intent);

			}
		});
		image3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 Intent intent = new Intent(TitleActivity.this,
				 SelectLevelActivity.class);
				 startActivity(intent);

			}
		});
		image4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//finish();
				killAllActivities();	// 自身も含めたすべてのアクティビティを終了する
			}
		});

	}
}