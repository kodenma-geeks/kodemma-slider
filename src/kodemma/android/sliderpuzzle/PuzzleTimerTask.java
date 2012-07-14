package kodemma.android.sliderpuzzle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

public class PuzzleTimerTask extends TimerTask{

	private PuzzleTimerTask timerTask;
	private Timer puzzleTimer;
	static long lapTime, startTime, pauseTime;
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
//スタート、リスタートごとにTimerTaskを生成（一度キャンセルして止めると再利用できない為）
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
	public void timerPause(){
		if(puzzleTimer != null){
			puzzleTimer.cancel();
			puzzleTimer = null;
			pauseTime = lapTime;
			lapTime = 0;
		}
	}
	public void timerResume(){
		if(puzzleTimer == null){
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
			startTime = System.currentTimeMillis();
		}
	}
	public void timerStop(){
		if(puzzleTimer != null){
			puzzleTimer.cancel();
			puzzleTimer = null;
			pauseTime = 0;
			lapTime = 0;
			displayed.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
		}
	}
	public void reset(){
		pauseTime = 0;
		lapTime = 0;
		displayed.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
	}
}