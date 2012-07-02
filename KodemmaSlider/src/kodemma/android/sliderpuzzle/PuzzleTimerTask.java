package kodemma.android.sliderpuzzle;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;

public class PuzzleTimerTask extends TimerTask{
	
	private PuzzleTimerTask timerTask;
	private Timer puzzleTimer;
	static float lapTime;
	TextView displayed;
	
	Handler hn = new Handler();
	
	public PuzzleTimerTask(TextView displayed){
		super();
	}
	public void run(){
    	hn.post(new Runnable(){
	    	public void run(){
	    		lapTime += 0.1d;
	    		
	    		
	    		BigDecimal bi = new BigDecimal(lapTime);
	    		float outputValue = bi.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
//	    		TimerActivity.timeWindow.setText(Float.toString(outputValue));
	    	}
    	});
	}
	public void timerStart(){
		if(puzzleTimer == null){
			timerTask = new PuzzleTimerTask(displayed);
			lapTime = 0;
			puzzleTimer = new Timer(true);
			puzzleTimer.schedule(timerTask, 100, 100);
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
			puzzleTimer.schedule(timerTask, 100, 100);
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

