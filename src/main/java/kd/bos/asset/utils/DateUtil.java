package kd.bos.asset.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * @ClassName:  DateUtil   
 * @Description:TODO(日期转换)   
 * @author: sam
 * @date:   2021-3-1 16:04:30      
 * @Copyright:
 */
public class DateUtil {
	private  SimpleDateFormat sfEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private  SimpleDateFormat sfStart = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",java.util.Locale.ENGLISH) ;
 
    //Date转String
    public  String dateToString(Date date) {
        String dateString = sfEnd.format(date);
        return dateString;
    }
 
 
    //String转Date
    public Date StringToDate(String dateString) {
        Date date = null;
        try {
            date = sfEnd.parse(dateString);
        } catch (ParseException e) {
            //sdf的格式要与dateString的格式相同，否者会报错
            e.printStackTrace();
        }
        return date;
    }
 
    //字符串时间转字符串时间
    public String stringToString(String string){
        String format=null;
        try {
             format = sfEnd.format(sfStart.parse(string));
        } catch (ParseException e) {
            //sdf的格式要与dateString的格式相同，否者会报错
            e.printStackTrace();
        }
      return format;
    }

}
