package com.ssh.rfidprint.server;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
/**
 * 处理日期基类
 * 
 * @author sunzx
 */
public class DateUtil {
    public final static String            DEFAULT_ZONE_ID     = "Asia/Hong_Kong";
    public final static String            DEFAULT_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    private final static SimpleDateFormat sfDate              = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static DateFormat       dateFormat          = new SimpleDateFormat("yyyyMMdd");

    /**
     * 将一个String 类型字段转成 Timestamp
     * 
     * @param strDate
     *            日期字符�?
     * @return
     */
    public static Date formatDate(String strDate) {
        try {
            if (strDate == null || strDate.length() < 1) return null;
            try {
                java.util.Date date = sfDate.parse(strDate);
                return date;
            } catch (ParseException ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Date formatDate(String strDate, String dateFormat) {
        try {
            SimpleDateFormat sdf = null;
            if (dateFormat != null) {
                sdf = new SimpleDateFormat(dateFormat);
            } else {
                sdf = new SimpleDateFormat("yyyy/MM/dd");
            }
            if (strDate == null || strDate.length() < 1) return null;
                java.util.Date date = sdf.parse(strDate);
                return date;
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将一个字符串日期转换为Timestamp类型
     * 
     * @param strDate
     *            符串日期
     * @return Timestamp 类型日期
     */
    public static Timestamp formatString(String strDate) {
        try {
            if (strDate == null || strDate.length() < 1) return null;
            try {
                java.util.Date date = sfDate.parse(strDate);
                return new java.sql.Timestamp(date.getTime());
            } catch (ParseException ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 把字符格式转换成日期格式
    public static String DateToString(java.sql.Timestamp date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
        String dt = null;
        try {
            dt = simpledateformat.format(date);
        } catch (Exception e) {
            dt = "";
            e.printStackTrace();
        }
        return dt;
    }

    /**
     * 将一个字符串格式化为�?个java.util.Date对象�?
     * 
     * @param obj
     *            Object
     * @return Date
     */
    public static Date parse(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            String dateString = obj.toString().trim();
            if (dateString.length() == 0) {
                return null;
            }
            if (dateString.length() == 10) {
                dateString += " 00:00:00";
            }
            java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.parse(dateString);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public final static java.sql.Timestamp nowTime(String zoneId) {
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        long l = System.currentTimeMillis();
        return new java.sql.Timestamp(l);
    }

    public final static java.sql.Timestamp nowTimestamp() {
        return nowTime(DEFAULT_ZONE_ID);
    }

    public final static String formatDate(java.util.Date date, String dateFormat, String zoneId) {
        SimpleDateFormat sdf = null;
        TimeZone zone = TimeZone.getTimeZone(zoneId);
        if (dateFormat != null) {
            sdf = new SimpleDateFormat(dateFormat);
        } else {
            sdf = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
        }
        if (zone != null) {
            sdf.setTimeZone(zone);
        }
        return sdf.format(date);
    }

    public final static String timestampToString(java.sql.Timestamp ts, String dateFormat) {
        if (ts == null) return "";
        return formatDate(ts, dateFormat, DEFAULT_ZONE_ID);
    }

    /**
     * 获取以当前年月日为文件夹名称字符�?
     * 
     * @return 当前年月日为文件夹名称字符串
     */
    public static String getDatefolder() {
        String strDate = "";
        strDate = dateFormat.format(new Date());
        return strDate;
    }

    /**
     * 获取当前时间的字符串。长度为8。格式为20051223
     * 
     * @author dugang
     * @update date 2005.12.22
     * @return String
     */
    public static String getNowYMD() {
        Date date = new Date();
        return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(date);
    }

    /**
     * 获取当前年份
     * 
     * @return 当前年份
     */
    public static String getYear() {
        String strYear = "";
        Calendar calendar = Calendar.getInstance();
        strYear = calendar.get(Calendar.YEAR) + "";
        return strYear;
    }

    /**
     * 获取当前月分
     * 
     * @return 当前月分
     */
    public static String getMonth() {
        String strMonth = "";
        Calendar calendar = Calendar.getInstance();
        strMonth = calendar.get(Calendar.MONTH) + 1 + "";
        return strMonth;
    }

    /**
     * 在用户查询的日期后面加上时分�?,以便能更精确查询到结�?
     * 
     * @param strDate
     *            要构造的日期,�?:2007-10-10
     * @param HHmmss
     *            在传过来的日期后加上时分�?,�?:00:00:00
     * @return 2007-10-10 00:00:00
     */
    public static Timestamp constructDate(String strDate, String HHmmss) {
        try {
            if (strDate == null || strDate.length() < 1) return null;
            try {
                String dateTemp = strDate + " " + HHmmss;
                java.util.Date date = sfDate.parse(dateTemp);
                return new java.sql.Timestamp(date.getTime());
            } catch (ParseException ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取N分钟前的时间
     * 
     * @param nowDate
     * @param n
     * @return
     */
    public static String getAfterMinuteTime(Date nowDate, int n) {
        Calendar c = Calendar.getInstance();
        // DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        c.setTime(nowDate);
        c.add(Calendar.MINUTE, n);
        Date d2 = c.getTime();
        String s = sfDate.format(d2);
        return s;
    }

    // 获得周一的日�?
    public static String getMonday(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    // 获得周五的日�?
    public static String getFriday(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    // 给定日期前几天或者后几天的日�?
    public static String afterNDay(Date nowDate, int n) {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setTime(nowDate);
        c.add(Calendar.DATE, n);
        Date d2 = c.getTime();
        String s = df.format(d2);
        return s;
    }
    // 判断两个日期是否在同�?�?
    public class ManageWeek {
        boolean isSameWeekDates(Date date1, Date date2) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);
            int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
            if (0 == subYear) {
                if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) return true;
            } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
                // 如果12月的�?后一周横跨来年第�?周的话则�?后一周即算做来年的第�?�?
                if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) return true;
            } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
                if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) return true;
            }
            return false;
        }
    }

    // 产生周序�?
    public static String getSeqWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
        if (week.length() == 1) week = "0" + week;
        String year = Integer.toString(c.get(Calendar.YEAR));
        return year + "-" + week;
    }

    /**
     * 判断两个日期是否为同�?�?
     * 
     * @param date
     * @param otherDate
     * @return
     */
    public static boolean isSameDate(java.sql.Timestamp date, java.sql.Timestamp otherDate) {
        boolean isSame;
        String strDateOne = DateToString(date);
        String strOtherDate = DateToString(otherDate);
        if (strDateOne.equals(strOtherDate)) {
            isSame = true;
        } else {
            isSame = false;
        }
        return isSame;
    }

    /**
     * 判断两个日期相差多少�?
     * 
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int betweenDays(String beginDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = new Date();
        Date d2 = new Date();
        long between = 0;
        int betweenDays = 0;
        try {
            d1 = sdf.parse(beginDate);
            d2 = sdf.parse(endDate);
            if (d1.getTime() > d2.getTime()) {
                between = d1.getTime() - d2.getTime();
            } else {
                between = d2.getTime() - d1.getTime();
            }
            betweenDays = (int) (between / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return betweenDays;
    }

    public static List<String> betweenDayList(String beginTime, String endTime) {
        List<String> betweenDayList = new ArrayList<String>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        try {
            start.setTime(format.parse(beginTime));
            end.setTime(format.parse(endTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (start.before(end) || start.equals(end)) {
            betweenDayList.add(format.format(start.getTime()));
            start.add(Calendar.DAY_OF_MONTH, 1);
        }
        return betweenDayList;
    }
    
    /**
     * 判断日期是星期几
     * @Title: getWeekOfDate
     * @param dt
     * @return
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
        w = 0;
        return weekDays[w];
        }

    public static void main(String[] args) {
        System.out.println("" + getNowYMD());
        System.out.println(DateUtil.nowTimestamp());
        System.out.println(DateUtil.getAfterMinuteTime(DateUtil.nowTimestamp(), -5));
    }
}
