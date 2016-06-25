package com.android.wolflz.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
	/**
	 * yyyy-MM-dd HH:mm:ss格式
	 */
	public static final String FORMAT_COMMON = "yyyy-MM-dd HH:mm:ss";
	/**
	 * yyyy-MM-dd格式
	 */
	public static final String FORMAT_YMD = "yyyy-MM-dd";
	/**
	 * MM-dd HH:mm格式
	 */
	public static final String FORMAT_MMDDHHMM = "MM-dd HH:mm";
	/**
	 * HH:mm:ss格式
	 */
	public static final String FORMAT_HMS = "HH:mm:ss";

	/**
	 * 将Date类型时间转换成String格式
	 */
	public static String parseDateToString(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
		return sdf.format(date);
	}
}
