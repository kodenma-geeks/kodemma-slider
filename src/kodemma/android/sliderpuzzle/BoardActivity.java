package kodemma.android.sliderpuzzle;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BoardActivity extends SharedMenuActivity implements BoardViewListener {
	private static final int INTENT_FOR_SELECT_LEVEL = 1;
	private BoardView boardView;
	private BackgroundView backgroundView;
	private TextView slideCounterView;
	private TextView chronometerView;	
	private PuzzleTimerTask chronometer;
	private Map<Integer, Button> buttonMap;
	
//	private Board board;	// 浜田　追記7/5
//	private BoardViewListener boardViewListener;	// 浜田　追記7/5
	GameStatus stat;	// 浜田　追記7/5

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.board);
		boardView = (BoardView)findViewById(R.id.boardView);
		backgroundView = (BackgroundView)findViewById(R.id.backgroundView);
		boardView.boardViewListener = this;
		slideCounterView = (TextView)findViewById(R.id.slideCounter);
		chronometerView = (TextView)findViewById(R.id.chronometer);
		chronometer = new PuzzleTimerTask(chronometerView);
		new ButtonClickListener();
	}
	public void onResume(){
		super.onResume();
		SoundEffect.soundLoad(this);
	}
	public void onStop(){
		super.onStop();
		chronometer.timerPause();
		SoundEffect.soundStop();
	}
	public void onRestart(){
		super.onRestart();
		chronometer.timerResume();
		stat = GameStatus.WAITING;
	}
	public void onActivityResult(int reqcode, int result, Intent it) {
		switch(reqcode) {
		case INTENT_FOR_SELECT_LEVEL:
			if (result == RESULT_OK) {
				int lv = (SelectLevelActivity.getLevelSetting(this)==0)? 1: SelectLevelActivity.getLevelSetting(this);
//				int lv = SelectLevelActivity.getLevelSetting(context);
				boardView.level = Level.levels().get(lv);
				
				Uri u = SelectLevelActivity.getImgUriSetting(this);
				boardView.bitmap = boardView.setImgUriSetting(u, this);
				
				boardView.showId = SelectLevelActivity.getHintSetting(this);
				boardView.isGrid = SelectLevelActivity.getTileSetting(this);
				
				boardView.onSizeChanged(boardView.board.width,boardView.board.height,boardView.board.width,boardView.board.height);
			}
			break;
		}
	}
	public void onTileSlided(int count) {
		slideCounterView.setText(String.valueOf(count));
		backgroundView.slided();
	}
	public void onChronometerSwitched(boolean on) {
		if (on) {
			chronometer.timerStart();
		} else {
			chronometer.timerPause();
		}
	}
	public void onGameSolved(int rows, int cols, int slides) {
		// ここでランキング画面へ遷移する
//		Intent it_for_ranking = new Intent(BoardActivity.this, RankingActivity.class);
//		it_for_ranking.putExtra("Laptime", PuzzleTimerTask.lapTime);
//		it_for_ranking.putExtra("Slidecount", slides);
//		it_for_ranking.putExtra("panels", rows*cols);
//		startActivity(it_for_ranking);
	}
	private class ButtonClickListener implements View.OnClickListener {
		private ButtonClickListener() {
			buttonMap = new HashMap<Integer, Button>();
			TypedArray tArray = getResources().obtainTypedArray(R.array.boardButtons);
			for (int i=0; i<tArray.length(); i++) {
				int resourceId = tArray.getResourceId(i, 0);
				Button button = (Button)findViewById(resourceId);
				button.setOnClickListener(this);
				buttonMap.put(resourceId, button);
			}
		}		
		public void onClick(View v) {		
			switch (v.getId()) {
			case R.id.board_button_start:
				boardView.startButtonPressed();
				buttonMap.get(R.id.board_button_start).setText(R.string.board_button_restart);
				chronometer.timerStart();
				backgroundView.shuffled(boardView.bitmap);
				SoundEffect.getSound(SoundEffect.sound_Button_on);
				break;
			case R.id.board_button_pause:
				stat = boardView.pauseButtonPressed();
				if (stat == GameStatus.PAUSED) {
					buttonMap.get(R.id.board_button_pause).setText(R.string.board_button_resume);
					SoundEffect.getSound(SoundEffect.sound_Button_on);
					chronometer.timerPause();
					buttonMap.get(R.id.board_button_start).setEnabled(false);
					buttonMap.get(R.id.board_button_setting).setEnabled(false);
					buttonMap.get(R.id.board_button_title).setEnabled(false);
				} else if (stat == GameStatus.PLAYING) {
					buttonMap.get(R.id.board_button_pause).setText(R.string.board_button_pause);
					SoundEffect.getSound(SoundEffect.sound_Button_off);
					chronometer.timerResume();
					buttonMap.get(R.id.board_button_start).setEnabled(true);
					buttonMap.get(R.id.board_button_setting).setEnabled(true);
					buttonMap.get(R.id.board_button_title).setEnabled(true);
				}
				break;
			case R.id.board_button_setting:
				SoundEffect.getSound(SoundEffect.sound_Button_on);
				Intent it_for_setting = new Intent(BoardActivity.this, SelectLevelActivity.class);
				it_for_setting.putExtra("STATUS", stat);
//				it_for_setting.putExtra("HINT", boardView.board.showId);
//				it_for_setting.putExtra("PICTURE", boardView.board.bitmap);
				startActivityForResult(it_for_setting, INTENT_FOR_SELECT_LEVEL);
				break;
			case R.id.board_button_title:
				SoundEffect.getSound(SoundEffect.sound_Button_on);
				Intent it_for_title = new Intent(BoardActivity.this, TitleActivity.class);
				startActivity(it_for_title);
				
//				stat = boardView.undoButtonPressed();
				break;
			}
		}
	}
}