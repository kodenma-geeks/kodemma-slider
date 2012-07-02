package kodemma.android.sliderpuzzle;


import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BoardActivity extends Activity implements TileListener {
	private BoardView boardView;
	private TextView slideCounterView;
	private TextView chronometerView;
	
	private PuzzleTimerTask chronometer;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		boardView = (BoardView)findViewById(R.id.boardView);
		boardView.tileListener = this;
		slideCounterView = (TextView)findViewById(R.id.slideCounter);
		chronometerView = (TextView)findViewById(R.id.chronometer);
		chronometer = new PuzzleTimerTask(chronometerView);
		new ButtonClickListener();
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
				break;
			case R.id.board_button_pause:
				GameStatus stat = boardView.pauseButtonPressed();
				if (stat == GameStatus.PAUSED) {
					chronometer.timerPause();
				} else if (stat == GameStatus.PLAYING) {
					chronometer.timerResume();
				}
				break;
			case R.id.board_button_setting:
				break;
			case R.id.board_button_answer:
				break;
			case R.id.board_button_suspend:
				break;
			}
		}
	}
	public void onTileSlided(int count) {
		slideCounterView.setText(String.valueOf(count));
	}
}