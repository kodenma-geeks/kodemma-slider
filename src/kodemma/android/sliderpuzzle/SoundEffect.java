package kodemma.android.sliderpuzzle;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundEffect {
	static SoundPool mSoundPool;
//	static int sound_Touch;
//	static int sound_Move;
	static int sound_Up;
	static int sound_Button_on;
	static int sound_Button_off;
	static int sound_solved;
//サウンドプールをロード
	public static void soundLoad(Context cn){
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
//		sound_Touch = mSoundPool.load(cn, R.raw.switch_ti, 1);
//		sound_Move = mSoundPool.load(this, R.raw., 1);
		sound_Up = mSoundPool.load(cn, R.raw.switch_ti, 1);
		sound_Button_on = mSoundPool.load(cn, R.raw.pipon, 1);
		sound_Button_off = mSoundPool.load(cn, R.raw.popin, 1);
		sound_solved = mSoundPool.load(cn, R.raw.turururu, 1);
	}
	public static void soundStop(){
//		mSoundPool.unload(sound_Touch);
//		mSoundPool.unload(sound_Move);
		mSoundPool.unload(sound_Up);
		mSoundPool.unload(sound_Button_on);
		mSoundPool.unload(sound_Button_off);
		mSoundPool.unload(sound_solved);
		mSoundPool.release();
	}
//効果音の再生タイミングで呼び出すメソッド
	public static void getSound(int sound_source){
		mSoundPool.play(sound_source, 0.3f, 0.3f, 1, 0, 1.0f);
	}
}

