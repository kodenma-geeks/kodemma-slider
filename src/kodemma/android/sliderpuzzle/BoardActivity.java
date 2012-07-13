package kodemma.android.sliderpuzzle;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BoardActivity extends SharedMenuActivity implements BoardViewListener {
	private static final int INTENT_FOR_SELECT_LEVEL = 1;
	private static final int INTENT_FOR_RANKING = 3;
	private BoardView boardView;
	private BackgroundView backgroundView;
	private TextView slideCounterView;
	private TextView chronometerView;	
	private PuzzleTimerTask chronometer;
	private Map<Integer, Button> buttonMap;
	

	GameStatus stat;	// 浜田　追記7/5
	int oldLevel;	// 浜田　追記7/12
	Uri oldUri;	// 浜田　追記7/12
	boolean isChange;	// 浜田　追記7/12
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
		SoundEffect.soundLoad(this);
		if(!isChange){
			chronometer.timerResume();
		}
	}
	public void onActivityResult(int reqcode, int result, Intent it) {
		switch(reqcode) {
		case INTENT_FOR_SELECT_LEVEL:
			if (result == RESULT_OK) {

				//設定画面からの戻り修正　浜田　7/12				
				isChange = false;
				
				int lv = (SelectLevelActivity.getLevelSetting(this)==0)? 1: SelectLevelActivity.getLevelSetting(this);
				if(lv != oldLevel)isChange = true;
				boardView.level = Level.levels().get(lv);
				Uri u = SelectLevelActivity.getImgUriSetting(this);
				if(!u.equals(oldUri))isChange = true;
// 以下、削除 by shima				
//				boardView.bitmap = boardView.setImgUriSetting(u, this);
// 以下、追加 by shima
				boardView.bitmap = SelectLevelActivity.getBitmapSetting(this);
// 以上。　by shima			
				
				boardView.board.showId = SelectLevelActivity.getHintSetting(this);
				boardView.board.isGrid = SelectLevelActivity.getTileSetting(this);
				boardView.board.border = boardView.board.getGrid(SelectLevelActivity.getTileSetting(this));
//				boardView.invalidate();

				SoundEffect.setSound_mute(SelectLevelActivity.getSoundSetting(this));// 7/13、追加 by 浜田
				
				if(isChange){//レベルか画像が変更された場合の処理
					chronometer.timerStop();
					buttonMap.get(R.id.board_button_start).setText(R.string.board_button_start);
					boardView.onSizeChanged(boardView.board.width,boardView.board.height,boardView.board.width,boardView.board.height);
					boardView.gameStatus = GameStatus.WAITING;
				}else{
					chronometer.timerResume();					
				}
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
//		Toast.makeText(this,(R.string.congratulation),Toast.LENGTH_LONG).show();	// クリア時に表示
		try {
		    Thread.sleep(2000); //2000ミリ秒Sleepする
		} catch (InterruptedException e) {}

		Intent it_for_ranking = new Intent(BoardActivity.this, RankingActivity.class);
		it_for_ranking.putExtra("Laptime", PuzzleTimerTask.lapTime);
		it_for_ranking.putExtra("Slidecount", slides);
		startActivity(it_for_ranking);
		finish();
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

				oldLevel = boardView.level.level();	// 変更前の値を保存
				oldUri = SelectLevelActivity.getImgUriSetting(BoardActivity.this);	// 変更前の値を保存

				it_for_setting.putExtra("GAME", true);
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
	 public boolean onKeyDown(int keyCode, KeyEvent event)
	 {
		 //戻りボタンの処理
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        	Toast.makeText(this, "Can not back", Toast.LENGTH_SHORT).show();
       	 	return false;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
	 }
}