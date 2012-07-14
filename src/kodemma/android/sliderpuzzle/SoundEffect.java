package kodemma.android.sliderpuzzle;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundEffect {
	private static SoundPool pool;
	private static Map<Integer, Integer> effects = new HashMap<Integer, Integer>();
	private static boolean isSound;
	
	//サウンドプールをロード
	public static void load(Context cn){
		if (pool == null) {
			pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			TypedArray tArray = cn.getResources().obtainTypedArray(R.array.effects);	

			for (int i=0; i<tArray.length(); i++) {
				int resId = tArray.getResourceId(i, 0);
				int soundId = pool.load(cn, resId, 1);
				effects.put(resId, soundId);
			}
		}
	}
	public static void unload(){
		if (pool != null) {
			for (Integer e : effects.values()) { pool.unload(e); }
			pool.release();
			pool = null;
		}
	}
	//効果音の再生タイミングで呼び出すメソッド
	public static void getSound(int resId){
		if (!isSound) return;
		if (pool != null) {
			int soundId = effects.get(resId);
			pool.play(soundId, 0.3f, 0.3f, 1, 0, 1.0f);
		}
	}
	public static void mute(boolean sound) {
		SoundEffect.isSound = sound;
	}
}

