package kodemma.android.sliderpuzzle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

public class PuzzleTimerTask extends TimerTask{

	private PuzzleTimerTask timerTask;
	private Timer puzzleTimer;
	static long lapTime;
	TextView displayed;

	final Handler hn = new Handler();
//コンストラクタ	
	public PuzzleTimerTask(TextView disp){
		super();
		displayed = disp;
	}
//1/10秒ごとに100ミリ秒インクリメント
	public void run() {
		hn.post(new Runnable() {
			public void run() {

				lapTime += 100;
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
		if (puzzleTimer == null) {
			lapTime = 0;
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
		} else if (puzzleTimer != null) {
			puzzleTimer.cancel();
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
		}
	}
	public void timerResume(){
		if(puzzleTimer == null){
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
		}
	}
	public void timerStop(){
		if(puzzleTimer != null){
			puzzleTimer.cancel();
			puzzleTimer = null;
			lapTime = 0;
		}
	}
}

