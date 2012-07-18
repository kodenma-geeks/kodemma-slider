package kodemma.android.sliderpuzzle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

/**
 * プレイ画面のカウントタイマーを扱うクラス
 * @author 島田
 *
 */
public class PuzzleTimerTask extends TimerTask{

	private PuzzleTimerTask timerTask;
	private Timer puzzleTimer;
	static long lapTime = 0, startTime = 0, pauseTime = 0;
	TextView displayed;

	final Handler hn = new Handler();
//コンストラクタ	
	public PuzzleTimerTask(TextView disp){
		super();
		displayed = disp;
	}
	public void run() {
		hn.post(new Runnable() {
			public void run() {

				long currentTime = System.currentTimeMillis();
				lapTime = currentTime-startTime+pauseTime;
				
				//秒数を60で割って余りを秒数に表示、以下分と時間も同じ
				long time = lapTime;
				time /= 1000;
		        long second = time % 60;
		        time /= 60;
		        long minute = time % 60;
		        long hour = time / 60;
	
				displayed.setText(String.format("%02d:%02d:%02d", hour, minute, second));	
			}
		});
	}
/**
 * タイマーをスタートさせる（プレイ中のonClickでリスタートになりタイマーが0から再カウント）
 * @author 島田
 * @param なし
 * @return なし
 */
	public void timerStart() {
		startTime = System.currentTimeMillis();
		if (puzzleTimer == null) {
			pauseTime = 0;
			lapTime = 0;
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
		} else if (puzzleTimer != null) {
			puzzleTimer.cancel();
			pauseTime = 0;
			lapTime = 0;
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
		}
	}
/**
 * タイマーをポーズする
 * リジュームする可能性がある場合タイマーをポーズしてLapTimeを保持
 * @author 島田
 * @param なし
 * @return なし
 */
	public void timerPause(){
		if(puzzleTimer != null){
			puzzleTimer.cancel();
			puzzleTimer = null;
		}
	}
/**
 * タイマーをリジュームする
 * ポーズで保持したLapTimeをPauseTimeとして加算してリジューム
 * @author 島田
 * @param なし
 * @return なし
 */
	public void timerResume(){
		if(puzzleTimer == null){
			pauseTime = lapTime;
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
			startTime = System.currentTimeMillis();
		}
	}
/**
 * タイマーをストップする
 * タイマーを停止しLapTime,PauseTimeを初期化
 * @author 島田
 * @param なし
 * @return なし
 */
	public void timerStop(){
		if(puzzleTimer != null){
			puzzleTimer.cancel();
			puzzleTimer = null;
			pauseTime = 0;
			lapTime = 0;
			displayed.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
		}
	}
/**
 * タイマーをリセットする
 * タイマーのスレッドが停止したのち再度LapTime,PauseTimeを初期化し、タイマー表示も初期状態に
 * @author 島田
 * @param なし
 * @return なし
 */
	public void reset(){
		pauseTime = 0;
		lapTime = 0;
		displayed.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
	}
}