package kodemma.android.sliderpuzzle;

import java.util.SortedMap;
import java.util.TreeMap;

import android.widget.Toast;

public class Level {
	public static final int MIN = 3;
	public static final int MAX = 6;
	private static final SortedMap<Integer, Level> levels = createLevels(MIN, MAX);
	private final int BONUS = 10000000;		// スコアが適度な値になるためのボーナス値
	private final int PANEL_VALUE = 120;	// パネル１枚ごとに与えられる点数

	private int small;
	private int large;
	private int level;
	private Level(int a, int b) {
		small = (a < b)? a : b;
		large = (a < b)? b : a;
	}
	public int small()		{ return small; }
	public int large()		{ return large; }
	public int level()		{ return level; }
	public int tiles()		{ return small * large; }
	public String text()	{ return small + " x " + large; }
	public int score(long ms, int count) {
		int score = 0;
		try{
//			int time = (int)ms/1000;	// ミリ秒から秒へ変換　7/13　やめ
			score = (int)((BONUS / count) * (tiles() * PANEL_VALUE) / ms);// ミリ秒のまま計算
		} catch (ArithmeticException e) {
//			０除算は処理をせず強制的に０点。
		}
		return score;
	}
	@Override public String toString() {
		return "small=" + small + ", large=" + large + ", level=" + level + ", tiles=" + tiles() + ", text=" + text();
	}
	private static SortedMap<Integer, Level> createLevels(int min, int max) {
		SortedMap<Integer, Level> map = new TreeMap<Integer, Level>();
		for (int i=min; i<=max; i++) {
			for (int j=min; j<=max; j++) {
				if (i > j) continue;
				map.put(i*j, new Level(i, j));
			}
		}
		SortedMap<Integer, Level> sorted = new TreeMap<Integer, Level>();
		int level = 1;
		for (Level v : map.values()) {
			v.level = level;
			sorted.put(level++, v);
		}
		return sorted;
	}
	public static final SortedMap<Integer, Level> levels() { return Level.levels; }
	public static final Level get(int level) { return Level.levels.get(level); }
	public static final Level min() { return Level.levels.get(Level.levels.firstKey()); }
	public static final Level max() { return Level.levels.get(Level.levels.lastKey()); }
}
