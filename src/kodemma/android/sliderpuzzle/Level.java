package kodemma.android.sliderpuzzle;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ゲームの難易度、及び難易度に関する属性を表すクラス。
 * シングルトン・パターンで実装してあるため、利用者は本クラスのインスタンスを参照するのみ。
 * @author shimatani
 *
 */
public class Level {
	/** 
	 * ゲーム盤の列または行の最小値
	 */
	public static final int MIN = 3;
	/** 
	 * ゲーム盤の列または行の最大値
	 */
	public static final int MAX = 6;
	/** 
	 * 全てのレベルオブジェクトを最小レベルから順に格納してあるツリーマップ
	 */
	private static final SortedMap<Integer, Level> levels = createLevels(MIN, MAX);
	/** 
	 * スコア計算の際、スコアを適切な範囲に収めるために使用する定数。
	 */
	private final int BONUS = 10000;
	/** 
	 * スコア計算の際、パネル１枚ごとに与えられる点数。
	 */
	private final int PANEL_VALUE = 120;
	/** 
	 * ゲーム盤の行数または列数のうち、小さいほうの値
	 */
	private int small;
	/** 
	 * ゲーム盤の行数または列数のうち、大きいほうの値
	 */
	private int large;
	/** 
	 * ゲームのレベル（難易度）。最小レベルの１から始まり１ずつ増加する。
	 */
	private int level;
	/** 
	 * コンストラクタ
	 * @param a ゲーム盤の行数。
	 * @param b ゲーム盤の列数。
	 */
	private Level(int a, int b) {
		small = (a < b)? a : b;
		large = (a < b)? b : a;
	}
	/** 
	 * ゲーム盤の行数、列数のうち小さいほうの値を返す。
	 * @return ゲーム盤の行数、列数のうち小さいほうの値
	 */
	public int small()		{ return small; }
	/** 
	 * ゲーム盤の行数、列数のうち大きいほうの値を返す。
	 * @return ゲーム盤の行数、列数のうち大きいほうの値
	 */
	public int large()		{ return large; }
	/** 
	 * ゲームのレベルを返す。
	 * @return ゲームのレベル
	 */
	public int level()		{ return level; }
	/** 
	 * ゲームのタイル数を返す。
	 * @return ゲームのタイル数
	 */
	public int tiles()		{ return small * large; }
	/** 
	 * ゲームのタイル数を表す文字列を返す。
	 * @return ゲームのタイル数を表す文字列
	 */
	public String text()	{ return small + " x " + large; }
	/** 
	 * ゲームのスコア値を算出し返す。
	 * @return ゲームのスコア値
	 * @param ms ゲームを完了するまでにかかった時間（ミリ秒）
	 * @param count ゲームを完了するまでに移動したタイル数
	 */
	public int score(long ms, int count) {
		int score = 0;
		try{
			int time = (int)ms/1000;	// ミリ秒から秒へ変換　7/13　やめ
			score = (int)((BONUS / count) * (tiles() * PANEL_VALUE) / time);
		} catch (ArithmeticException e) {
//			０除算は処理をせず強制的に０点。
		}
		return score;
	}
	/** 
	 * デバッグ用
	 * @return 自身が保持している全ての値を表す可読文字列
	 */
	@Override public String toString() {
		return "small=" + small + ", large=" + large + ", level=" + level + ", tiles=" + tiles() + ", text=" + text();
	}
	/** 
	 * 全レベルオブジェクトを生成し、ツリーマップに小さいレベル順にセットする。
	 * @return 生成したレベルオブジェクト群を含むツリーマップ
	 * @param min ゲーム盤の列数または行数の最小値
	 * @param min ゲーム盤の列数または行数の最大値
	 */
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
	/** 
	 * 全レベルオブジェクト群を含むツリーマップを取得する。
	 * @return 生成したレベルオブジェクト群を含むツリーマップ
	 */
	public static final SortedMap<Integer, Level> levels() { return Level.levels; }
	/** 
	 * 指定されたレベルのレベルオブジェクトを取得する。
	 * @return 対象のレベルオブジェクト
	 * @param level 対象のレベル
	 */
	public static final Level get(int level) { return Level.levels.get(level); }
	/** 
	 * 最小レベルのレベルオブジェクトを取得する。
	 * @return 対象のレベルオブジェクト
	 */
	public static final Level min() { return Level.levels.get(Level.levels.firstKey()); }
	/** 
	 * 最大レベルのレベルオブジェクトを取得する。
	 * @return 対象のレベルオブジェクト
	 */
	public static final Level max() { return Level.levels.get(Level.levels.lastKey()); }
}
