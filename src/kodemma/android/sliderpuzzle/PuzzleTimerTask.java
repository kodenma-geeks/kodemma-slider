package kodemma.android.sliderpuzzle;

import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	public PuzzleTimerTask(TextView disp){
		super();
		displayed = disp;
	}
	public void run(){
   	hn.post(new Runnable(){
	    	public void run(){

	    		lapTime += 100; 		
	    		Date date = new Date(lapTime-32400000); 
	    		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	    		displayed.setText(format.format(date)); 

	    	}
    	});
	}
	public void timerStart(){
		if(puzzleTimer == null){
			SoundEffect.getSound(SoundEffect.sound_Button_on);
			lapTime = 0;
			timerTask = new PuzzleTimerTask(displayed);
			puzzleTimer = new Timer(true);
			puzzleTimer.scheduleAtFixedRate(timerTask, 100, 100);
		}
		else if(puzzleTimer != null){
			SoundEffect.getSound(SoundEffect.sound_Button_off);
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
