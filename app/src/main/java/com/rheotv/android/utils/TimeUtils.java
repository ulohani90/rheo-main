package com.rheotv.android.utils;

import android.util.Log;

import com.rheotv.android.data.network.models.TimerObj;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TimeUtils {

    public static final String YYYY_MM_DD_T_HH_MM_SS_SSSXXX = "yyyy-MM-dd\'T\'HH:mm:ss.SSS";
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd\'T\'HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MM_YYYY = "dd-MM-yyyy";
    public static final String HH_MM_AA = "hh:mm aa";
    public static final String HH_MM = "hh:mm";
    public static final String DD_MMM_YYYY = "dd-MMM-yyyy";

    private static Map<Integer, String> hm = new HashMap<>();

    private static Map<Integer, String> hd = new HashMap<Integer, String>();

    public static final int MILLIS_IN_DAY = 86400000;

    public static final int MILLIS_IN_A_MIN = 60000;

    private static final int SECS_IN_AN_HOUR = 3600;

    public static final int MILLIS_AN_HOUR = 3600000;

    private static final int SECS_IN_A_DAY = 86400;

    public static String getFormattedHindiDate(String dateString) {
        fillHashMaps();
        int daysBetween;
        daysBetween = getDaysDifference(dateString);
        if (daysBetween == 0) {
            return "Today";
        } else if (daysBetween == 1) {
            return "Yesterday";
        } else {
            String res = dateString;
            if (dateString.contains("+")) {
                res = dateString.substring(0, dateString.indexOf('+'));
            } else if (dateString.contains("Z")) {
                res = dateString.substring(0, dateString.indexOf('Z'));
            } else if (dateString.contains("z")) {
                res = dateString.substring(0, dateString.indexOf('z'));
            } else {
                return "";
            }
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(res);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            long timeInMillis = System.currentTimeMillis();
            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(timeInMillis);
            String dateforrow = sdf.format(cal1.getTime());

            dateforrow = dateforrow.replace('T', ' ');
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int mon = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);

            if (hour > 12) {
                hour = Math.abs(12 - hour);
                String ans = hm.get(mon);
                String upday = hd.get(day);
                Log.d("dare", dateforrow + "");
                Log.d("month", mon + "");
                Log.d("hour", hour + "");
                Log.d("min", min + "");
                return upday + " " + ans;
            } else {
                String ans = hm.get(mon);
                String upday = hd.get(day);
                Log.d("dare", dateforrow + "");
                Log.d("month", mon + "");
                Log.d("hour", hour + "");
                Log.d("min", min + "");
                return upday + " " + ans;
            }

        }
    }

    public static long getTimeInMillis(String timeStamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return format.parse(timeStamp).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getTimeDiffInMs(String startTime, String endTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date startDate = format.parse(startTime);
            Date endDate = format.parse(endTime);
            return endDate.getTime() - startDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getContestDateState(String startDateString, String endDateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            Date startDate = format.parse(startDateString);
            Date endDate = format.parse(endDateString);
            if (endDate.getTime() < System.currentTimeMillis()) {
                return AppConstants.CONTEST_DATE_STATE_END;
            }
            if (startDate.getTime() < System.currentTimeMillis() && endDate.getTime() > System.currentTimeMillis()) {
                return AppConstants.CONTEST_DATE_STATE_LIVE;
            }
            if (startDate.getTime() - System.currentTimeMillis() <= MILLIS_IN_DAY) {
                return AppConstants.CONTEST_DATE_STATE_WITHIN_1_DAY;
            }
            return AppConstants.CONTEST_DATE_STATE_BEFORE_1_DAY;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return AppConstants.CONTEST_DATE_STATE_BEFORE_1_DAY;
    }

    public static String getFormattedDateForContestStart(String fromDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("Starts On ");
        try {
            SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = fromFormat.parse(fromDate);
            SimpleDateFormat toDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            builder.append(toDateFormat.format(date));
            builder.append(" at ");
            SimpleDateFormat toTimeFormat = new SimpleDateFormat("hh:mm a");
            builder.append(toTimeFormat.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static long getContestStartIn(String startDate) {
        try {
            SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = fromFormat.parse(startDate);
            return (date.getTime() - System.currentTimeMillis()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static int getDaysDifference(String input) {
        try {
            DateTime dateTime = new DateTime(input);
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - dateTime.getMillis();
            int daysBetween = (int) (diff / (1000 * 60 * 60 * 24));
            return daysBetween;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return getDaysDifferenceForTimestamp(input);
            }

            return 0;
        }
    }

    public static int getDaysDifferenceForTimestamp(String input) {
        try {
            DateTime dateTime = new DateTime(Double.valueOf(input).longValue());
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - (dateTime.getMillis() * 1000);
            int daysBetween = (int) (diff / (1000 * 60 * 60 * 24));
            return daysBetween;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getHoursDifference(String input) {
        try {
            DateTime dateTime = new DateTime(input);
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - dateTime.getMillis();
            int hourbetween = (int) (diff / (1000 * 60 * 60));
            return hourbetween;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return getHoursDifferenceForTimestamp(input);
            }
            return 0;
        }
    }

    public static int getHoursDifferenceForTimestamp(String input) {
        try {
            DateTime dateTime = new DateTime(Double.valueOf(input).longValue());
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - (dateTime.getMillis() * 1000);
            int hourbetween = (int) (diff / (1000 * 60 * 60));
            return hourbetween;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getMinDifference(String input) {
        try {
            DateTime dateTime = new DateTime(input);
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - dateTime.getMillis();
            int minbetween = (int) (diff / (1000 * 60));
            return minbetween;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return getMinDifferenceForTimestamp(input);
            }
            return 0;
        }
    }

    public static int getMinDifferenceForTimestamp(String input) {
        try {
            DateTime dateTime = new DateTime(Double.valueOf(input).longValue());
            DateTime currentDate = new DateTime();
            long diff = currentDate.getMillis() - (dateTime.getMillis() * 1000);
            int minbetween = (int) (diff / (1000 * 60));
            return minbetween;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void fillHashMaps() {

        if (hm.isEmpty()) {
            hm.put(1, "Jan");
            hm.put(2, "Feb");
            hm.put(3, "Mar");
            hm.put(4, "April");
            hm.put(5, "May");
            hm.put(6, "June");
            hm.put(7, "July");
            hm.put(8, "August");
            hm.put(9, "Sept.");
            hm.put(10, "Oct");
            hm.put(11, "Nov.");
            hm.put(12, "Dec.");
        }

        if (hd.isEmpty()) {

            hd.put(1, "1st");
            hd.put(2, "2nd");
            hd.put(3, "3rd");
            hd.put(4, "4th");
            hd.put(5, "5th");
            hd.put(6, "6th");
            hd.put(7, "7th");
            hd.put(8, "8th");
            hd.put(9, "9th");
            hd.put(10, "10th");
            hd.put(11, "11th");
            hd.put(12, "12th");
            hd.put(13, "13th");
            hd.put(14, "14th");
            hd.put(15, "15th");
            hd.put(16, "16th");
            hd.put(17, "17th");
            hd.put(18, "18th");
            hd.put(19, "19th");
            hd.put(20, "20th");
            hd.put(21, "21st");
            hd.put(22, "22nd");
            hd.put(23, "23rd");
            hd.put(24, "24th");
            hd.put(25, "25th");
            hd.put(26, "26th");
            hd.put(27, "27th");
            hd.put(28, "28th");
            hd.put(29, "29th");
            hd.put(30, "30th");
            hd.put(31, "31st");
        }
    }

    public static String getTimeForLong(long timeInSecs) {
        List<TimerObj> timerObjs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        int days = (int) timeInSecs / SECS_IN_A_DAY;
        if (days > 0) {
            if (days <= 9) {
                timerObjs.add(new TimerObj("Days", days + ""));
            } else {

            }
            builder.append(days);
            builder.append("Days ");
        }
        timeInSecs = timeInSecs % SECS_IN_A_DAY;
        int hours = (int) timeInSecs / SECS_IN_AN_HOUR;
        if (hours > 0) {
            if (hours <= 9) {
                builder.append("0");
            }
            builder.append(hours);
            builder.append("Hrs ");
        }

        timeInSecs = timeInSecs % SECS_IN_AN_HOUR;
        int mins = (int) timeInSecs / 60;
        if (mins > 0) {
            if (mins <= 9) {
                builder.append("0");
            }
            builder.append(mins);
            builder.append("Mins ");
        }

        timeInSecs = timeInSecs % 60;
        if (timeInSecs <= 9) {
            builder.append("0");
        }
        builder.append(timeInSecs);
        builder.append("Secs");
        return builder.toString();
    }

    public static boolean hasStreamNotStarted(long startDateTime) {
        Date streamStartDateTime = new Date(startDateTime);
        if (streamStartDateTime.after(new Date(System.currentTimeMillis()))) {
            return true;
        }
        return false;
    }

    public static String getStreamStartText(long timeInMillis) {
        Date dateToConvert = new Date(timeInMillis);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTimeInMillis(timeInMillis);
        timeCal.set(Calendar.HOUR, 0);
        timeCal.set(Calendar.MINUTE, 0);
        timeCal.set(Calendar.SECOND, 0);
        timeCal.set(Calendar.MILLISECOND, 0);
        StringBuilder builder = new StringBuilder();
        if (timeCal.getTimeInMillis() - cal.getTimeInMillis() < MILLIS_IN_DAY) {
            builder.append("Today");
        } else if (timeCal.getTimeInMillis() - cal.getTimeInMillis() < 2 * MILLIS_IN_DAY) {
            builder.append("Tomorrow");
        } else {
            SimpleDateFormat toDateFormat = new SimpleDateFormat("d");
            String date = toDateFormat.format(dateToConvert);
            if (date.endsWith("1") && !date.endsWith("11"))
                toDateFormat = new SimpleDateFormat("EEE, d'st' MMM");
            else if (date.endsWith("2") && !date.endsWith("12"))
                toDateFormat = new SimpleDateFormat("EEE, d'nd' MMM");
            else if (date.endsWith("3") && !date.endsWith("13"))
                toDateFormat = new SimpleDateFormat("EEE, d'rd' MMM");
            else
                toDateFormat = new SimpleDateFormat("EEE, d'th' MMM");

            builder.append(toDateFormat.format(dateToConvert));
        }
        builder.append(" at ");
        SimpleDateFormat toTimeFormat = new SimpleDateFormat("h a");
        builder.append(toTimeFormat.format(dateToConvert));
        return builder.toString();
    }


    public static List<TimerObj> getTimerObjsList(long timeInSecs) {
        List<TimerObj> timerObjs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        int days = (int) timeInSecs / SECS_IN_A_DAY;
        if (days > 0) {
            timerObjs.add(new TimerObj(days > 1 ? "Days" : "Day", days + ""));
        }
        timeInSecs = timeInSecs % SECS_IN_A_DAY;
        int hours = (int) timeInSecs / SECS_IN_AN_HOUR;
        if (hours > 0) {
            timerObjs.add(new TimerObj(hours > 1 ? "Hrs" : "Hr", hours + ""));
        }

        timeInSecs = timeInSecs % SECS_IN_AN_HOUR;
        int mins = (int) timeInSecs / 60;
        if (mins >= 0) {
            timerObjs.add(new TimerObj(mins > 1 ? "Mins" : "Min", mins > 9 ? mins + "" : "0" + mins));
        }

        timeInSecs = timeInSecs % 60;
        if (timeInSecs >= 0) {
            timerObjs.add(new TimerObj(timeInSecs > 1 ? "Secs" : "Sec", timeInSecs > 9 ? timeInSecs + "" : "0" + timeInSecs));
        }

        return timerObjs;
    }

    public static Date getDateFromString(String dateString, String dateFormat) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            Date date = simpleDateFormat.parse(dateString);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFormattedDate(String dateFormat, Date date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDateTimeFromLongMS(long ms) {
        SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSSXXX);
        return format.format(new Date(ms));
    }
}