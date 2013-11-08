package com.xiaozhi.blog;


/**
 * 字符相似度比较
 * @author xiaozhi
 *
 */
public class Levenshtein {

	private static int compare(String str, String target) {
		int d[][]; // 矩阵
		int n = str.length();
		int m = target.length();
		int i; // 遍历str的
		int j; // 遍历target的
		char ch1; // str的
		char ch2; // target的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}
		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}
		for (i = 1; i <= n; i++) { // 遍历str
			ch1 = str.charAt(i - 1);
			// 去匹配target
			for (j = 1; j <= m; j++) {
				ch2 = target.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]+ temp);
			}
		}
		return d[n][m];
	}

	private static int  min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;
	}

	/**
	 *
	 * 获取两字符串的相似度
	 * @param str
	 * @param target
	 * @return
	 */

	public static float getSimilarityRatio(String str, String target) {
		return 1 - (float) compare(str, target)/ Math.max(str.length(), target.length());
	}




	/**
	 * 短距离算法加权平均获得用户相似度
	 * @param me
	 * @param u
	 * @return
	 */
//	public static float getUserSimilarityRatio(TianJiUser me,TianJiUser u){
//		float name = Levenshtein.getSimilarityRatio(me.getName(), u.getName());
//		float headline = Levenshtein.getSimilarityRatio(me.getHeadline(), u.getHeadline());
//		float country = Levenshtein.getSimilarityRatio(me.getLocation().getCountry(), u.getLocation().getCountry());
//		float city = Levenshtein.getSimilarityRatio(me.getLocation().getCity(), u.getLocation().getCity());
//
//		return (name*10+country*5+city*10+headline*20)/(10+5+10+20);
//	}


	public static void main(String[] args) {
		String str = "北京";
		String target = "北京地区的人";
		System.out.println("similarityRatio="+ Levenshtein.getSimilarityRatio(str, target));

	}

}