package kodemma.android.sliderpuzzle;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

// こうすればすっきりするのでは？　このクラスは未使用。　試しに書いてみた。
public class Prefs {
	private static final String DEFAULT_IMAGE_URI = Utils.getResourceUri(R.drawable.ic_launcher);
	private Context context;
	public Prefs(Context c) { context = c; }
	// getters
	public int getLevel()		{ return sp().getInt("level", 1); }
	public Boolean getHint()	{ return sp().getBoolean("hint", false); }
	public Boolean getTile()	{ return sp().getBoolean("tile", false); }
	public Boolean getSound()	{ return sp().getBoolean("sound", false); }
	public Uri getImgUri()		{ return Uri.parse(sp().getString("uri", DEFAULT_IMAGE_URI)); }
	// setters
	public void setLevel(int v)		{ sp().edit().putInt("level", v).commit(); }
	public void setHint(boolean v)	{ sp().edit().putBoolean("hint", v).commit(); }
	public void setTile(boolean v)	{ sp().edit().putBoolean("tile", v).commit(); }
	public void setSound(boolean v)	{ sp().edit().putBoolean("sound", v).commit(); }
	public void setImgUri(String v)	{ sp().edit().putString("uri", v).commit(); }
	//
	private SharedPreferences sp() { return context.getSharedPreferences("pref", context.MODE_PRIVATE); }
}
