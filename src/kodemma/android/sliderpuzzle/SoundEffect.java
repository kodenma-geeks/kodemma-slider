package kodemma.android.sliderpuzzle;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
/**
 * プレイ画面の効果音を扱うクラス
 * @author 島田
 *
 */
public class SoundEffect {
	private static SoundPool pool;
	private static Map<Integer, Integer> effects = new HashMap<Integer, Integer>();
	private static boolean isSound;
	
/**
 * サウンドプールをロードする
 * 効果音すべてをいったんロードする
 * @author 島田
 * @param Context
 * @return なし
 */
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
/**
 * サウンドプールをロードする
 * 効果音すべてをアンロードし、サウンドプールをリリース
 * @author 島田
 * @param なし
 * @return なし
 */
	public static void unload(){
		if (pool != null) {
			for (Integer e : effects.values()) { pool.unload(e); }
			pool.release();
			pool = null;
		}
	}
/**
 * 効果音を再生する
 * 引数に応じて効果音を鳴らす
 * @author 島田
 * @param int resId
 * @return なし
 */
	public static void getSound(int resId){
		if (!isSound) return;
		if (pool != null) {
			int soundId = effects.get(resId);
			pool.play(soundId, 0.3f, 0.3f, 1, 0, 1.0f);
		}
	}
/**
 * 効果音をミュートする
 * 設定画面にて効果音をミュートにする
 * @author 浜田
 * @param boolean sound
 * @return なし
 */
	public static void mute(boolean sound) {
		SoundEffect.isSound = sound;
	}
}

