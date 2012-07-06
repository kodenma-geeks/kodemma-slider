package kodemma.android.sliderpuzzle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BoardActivity extends SharedMenuActivity implements BoardViewListener {
	private static final int INTENT_FOR_SELECT_LEVEL = 1;
	private BoardView boardView;
	private TextView slideCounterView;
	private TextView chronometerView;
	
	private PuzzleTimerTask chronometer;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		boardView = (BoardView)findViewById(R.id.boardView);
		boardView.boardViewListener = this;
		slideCounterView = (TextView)findViewById(R.id.slideCounter);
		chronometerView = (TextView)findViewById(R.id.chronometer);
		chronometer = new PuzzleTimerTask(chronometerView);
		new ButtonClickListener();
	}
	public void onResume(){
		super.onResume();
//		SoundEffect.soundLoad(this);
	}
	public void onStop(){
		super.onStop();
//		SoundEffect.soundStop();
	}
	public void onActivityResult(int reqcode, int result, Intent it) {
		switch(reqcode) {
		case INTENT_FOR_SELECT_LEVEL:
			if (result == RESULT_OK) {
				// ここに書く
			}
			break;
		}
	}
	public void onTileSlided(int count) {
		slideCounterView.setText(String.valueOf(count));
	}
	public void onGameSolved(int rows, int cols, int slides) {
		// ここでランキング画面へ遷移する
//		Intent it = new Intent(BoardActivity.this, RankingActivity.class);
//		it.putExtra("Laptime", PuzzleTimerTask.lapTime);
//		it.putExtra("Slidecount", slides);
//		it.putExtra("panels", rows*cols);
//		startActivity(it);
	}
	private class ButtonClickListener implements View.OnClickListener {
		private ButtonClickListener() {
			TypedArray tArray = getResources().obtainTypedArray(R.array.boardButtons);
			for (int i=0; i<tArray.length(); i++) {
				int resourceId = tArray.getResourceId(i, 0);
				Button button = (Button)findViewById(resourceId);
				button.setOnClickListener(this);
			}
		}
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.board_button_start:
				boardView.startButtonPressed();
				chronometer.timerStart();
//				SoundEffect.getSound(SoundEffect.sound_Button);
				break;
			case R.id.board_button_pause:
				GameStatus stat = boardView.pauseButtonPressed();
//				SoundEffect.getSound(SoundEffect.sound_Button);
				if (stat == GameStatus.PAUSED) {
					chronometer.timerPause();
				} else if (stat == GameStatus.PLAYING) {
					chronometer.timerResume();
				}
				break;
			case R.id.board_button_setting:
//				SoundEffect.getSound(SoundEffect.sound_Button);
				Intent it = new Intent(BoardActivity.this, SelectLevelActivity.class);
				startActivityForResult(it, INTENT_FOR_SELECT_LEVEL);
				break;
			case R.id.board_button_answer:
				break;
			case R.id.board_button_suspend:
				break;
			}
		}
	}
}