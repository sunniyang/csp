package com.suomee.csp.lib.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {
	public static long getNowSeconds() {
		return getNowMilliSeconds() / 1000L;
	}
	
	public static long getNowMilliSeconds() {
		return System.currentTimeMillis();
	}
	
	public static long floorToDate(long seconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(seconds * 1000L);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis() / 1000L;
	}
	
	public static String secondsToString(long seconds, String pattern) {
		return millisecondsToString(seconds * 1000L, pattern);
	}
	
	public static String millisecondsToString(long milliseconds, String pattern) {
		return new SimpleDateFormat(pattern).format(new Date(milliseconds));
	}
	
	public static int secondsToAge(long seconds) {
		Calendar calendar = Calendar.getInstance();
		int y2 = calendar.get(Calendar.YEAR);
		calendar.setTimeInMillis(seconds * 1000L);
		int y1 = calendar.get(Calendar.YEAR);
		return y2 - y1;
	}
}
