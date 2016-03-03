package com.chenzao.utils;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Map;
import com.chenzao.alarm.AlarmHelper;
import com.chenzao.db.ChenzaoDBAdapter;
import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.TaskScheduler;
import com.chenzao.net.Reflection;
import com.xiaosajie.chenzao.CustomToast;
import com.xiaosajie.chenzao.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class Utils {
	public static int [] dayPics = {
		R.drawable.day1, R.drawable.day2, R.drawable.day3, R.drawable.day4,
		R.drawable.day5, R.drawable.day6, R.drawable.day7, R.drawable.day8,
		R.drawable.day9, R.drawable.day10, R.drawable.day11, R.drawable.day12,
		R.drawable.day13, R.drawable.day14, R.drawable.day15, R.drawable.day16,
		R.drawable.day17, R.drawable.day18, R.drawable.day19, R.drawable.day20,
		R.drawable.day21, R.drawable.day22, R.drawable.day23, R.drawable.day24,
		R.drawable.day25, R.drawable.day26, R.drawable.day27, R.drawable.day28,
		R.drawable.day29, R.drawable.day30, R.drawable.day31, R.drawable.day32,
		R.drawable.day33, R.drawable.day34, R.drawable.day35, R.drawable.day36,
		R.drawable.day37, R.drawable.day38, R.drawable.day39, R.drawable.day40,
		R.drawable.day41, R.drawable.day42, R.drawable.day43, R.drawable.day44,
		R.drawable.day45, R.drawable.day46, R.drawable.day47, R.drawable.day48,
		R.drawable.day49, R.drawable.day50, R.drawable.day51, R.drawable.day52,
		R.drawable.day53, R.drawable.day54, R.drawable.day55, R.drawable.day56,
		R.drawable.day57, R.drawable.day58, R.drawable.day59, R.drawable.day60,
		R.drawable.day61, R.drawable.day62, R.drawable.day63, R.drawable.day64,
		R.drawable.day65, R.drawable.day66, R.drawable.day67, R.drawable.day68,
		R.drawable.day69, R.drawable.day70, R.drawable.day71, R.drawable.day72,
		R.drawable.day73, R.drawable.day74, R.drawable.day75, R.drawable.day76,
		R.drawable.day77, R.drawable.day78, R.drawable.day79, R.drawable.day80,
		R.drawable.day81, R.drawable.day82, R.drawable.day83, R.drawable.day84,
		R.drawable.day85, R.drawable.day86, R.drawable.day87, R.drawable.day88,
		R.drawable.day89, R.drawable.day90, R.drawable.day91, R.drawable.day92,
		R.drawable.day93, R.drawable.day94, R.drawable.day95, R.drawable.day96,
		R.drawable.day97, R.drawable.day98, R.drawable.day99, R.drawable.day100,
		R.drawable.day_start};
	
	//把更新的index转化为对应图片在图片数组中的index
	public static int getDayPicIndex(int dayIndex){
		if (dayIndex > 0 && dayIndex < 101){
			dayIndex = 100-dayIndex+1;
		}else {
			//index异常，统一显示第100天
			dayIndex = 100;
		}
		return dayIndex;
	}
	
	public static void logd(CharSequence msg) {
		if (!TextUtils.isEmpty(msg) && Constants.DEBUG) {
			Log.d(Constants.TAG, msg.toString());
		}
	}

	public static void loge(CharSequence msg) {
		if (!TextUtils.isEmpty(msg) && Constants.DEBUG) {
			Log.e(Constants.TAG, msg.toString());
		}
	}

	public static void loge(Throwable e) {
		if (e != null && Constants.DEBUG) {
			Log.e(Constants.TAG, "", e);
		}
	}
	
	public static void log_d(String tag, String msg){
		if (Constants.DEBUG){
			Log.d(tag, msg);
		}
	}
	
	public static void exitApp(Context context){
		((Activity)context).finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
    public static void showToast(final Context context, int resId, int duration){
        Context appContext = context.getApplicationContext();
        Toast.makeText( appContext, resId, duration).show();
    }
    
    
    public static void showToast(final Context context, CharSequence text, int duration){
        Context appContext = context.getApplicationContext();
        if(TextUtils.isEmpty(text)){
        	return;
        }
        Toast.makeText( appContext, text, duration).show();
    }
	
	public static LinearLayout createLoadingLayout(int res, Context a) {
		LinearLayout pgLayout = new LinearLayout(a);
		ProgressBar mProgressBar = null;

		pgLayout.setGravity(Gravity.CENTER);
		mProgressBar = new ProgressBar(a);
		mProgressBar.setIndeterminate(false);
		mProgressBar.setIndeterminateDrawable(a.getResources().getDrawable(
				R.drawable.progressbar));
		pgLayout.addView(
				mProgressBar,
				new LinearLayout.LayoutParams(
						a.getResources().getDimensionPixelSize(
								R.dimen.baselayout_title_height), a
								.getResources().getDimensionPixelSize(
										R.dimen.baselayout_title_height)));
		TextView tv = new TextView(a);
		tv.setText(res);
		tv.setTextSize(13);
		tv.setTextColor(R.color.card_title_text_color);
		pgLayout.addView(tv);

		return pgLayout;
	}
	
	public static CustomToast createProgressCustomToast(int res, Context a) {
		CustomToast ct = new CustomToast(a.getApplicationContext(), res, true);
		return ct;
	}
	
	public static Toast createProgressToast(int res, Context a) {
		Toast toast = new Toast(a);
		LinearLayout pgLayout = createLoadingLayout(res, a);
		toast.setView(pgLayout);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}

	public static Toast createToast(int res, Context a) {
		Toast toast = null;
		toast = new Toast(a);
		LinearLayout pgLayout = new LinearLayout(a);
		TextView v = new TextView(a);
		((TextView) v).setText(res);
		((TextView) v).setGravity(Gravity.CENTER);
		((TextView) v).setTextSize(13);
		((TextView) v).setPadding(15, 0, 15, 0);
		v.setTextColor(R.color.toast_text);
		pgLayout.addView(v);

		toast.setView(pgLayout);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}

	public static void setWindowHardWareAccelerated(Activity activity) {// 开启Window级别硬件加速
		int flagHardWareAccelerated = getFlagHardWareAccelerated();
		if (flagHardWareAccelerated != -1) {
			activity.getWindow().setFlags(flagHardWareAccelerated,
					flagHardWareAccelerated);
		}
	}

	public static int getFlagHardWareAccelerated() {// 通过反射获得WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
		try {
			Field field = WindowManager.LayoutParams.class
					.getField("FLAG_HARDWARE_ACCELERATED");
			int result = field.getInt(WindowManager.LayoutParams.class); // 0x01000000
			return result;
		} catch (IllegalArgumentException e) {
			Utils.loge(e.getMessage());
		} catch (IllegalAccessException e) {
			Utils.loge(e.getMessage());
		} catch (SecurityException e) {
			Utils.loge(e.getMessage());
		} catch (NoSuchFieldException e) {
			Utils.loge(e.getMessage());
		}
		return -1;
	}

	public static File getUriFile(String filePath, Context context) {
		Uri uri = Uri.parse(filePath);
		File file = null;
		if (uri != null && uri.getScheme() != null
				&& uri.getScheme().equals("content")) {

			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = context.getContentResolver().query(uri,
					proj, null, null, null);

			if (actualimagecursor == null) {
				return null;
			}

			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			if (actualimagecursor.moveToFirst()) {
				String img_path = actualimagecursor
						.getString(actual_image_column_index);
				if (TextUtils.isEmpty(img_path)) {
					return null;
				}
				file = new File(img_path);

				// os4.2编辑直接share时第三方的provider MBAD-2764
				if (file == null || !file.exists()) {
					file = new File(Uri.parse(img_path).getLastPathSegment());
				}
			}
		} else if (uri != null && uri.getScheme() != null
				&& uri.getScheme().equals("file")) {
			file = new File(uri.getPath());
		} else {
			file = new File(filePath);
		}
		return file;
	}

	public static String getUriFilePath(String filePath, Context context) {

		File file = getUriFile(filePath, context);

		if (file != null) {
			return file.getAbsolutePath();
		}

		return null;
	}
	
	/**
	 * 确保指定文件或者文件夹存在
	 * 
	 * @param file_
	 * @return
	 */
	public static boolean makesureFileExist(File file_) {
		if (file_ == null){
			return false;
		}

		if (makesureParentDirExist(file_)) {
			if (file_.isFile()) {
				try {
					return file_.createNewFile();
				} catch (IOException e) {
				}
			} else if (file_.isDirectory()) {
				return file_.mkdir();
			}
		}

		return false;
	}

	/**
	 * 确保指定文件或者文件夹存在
	 * 
	 * @param filePath_
	 * @return
	 */
	public static boolean makesureFileExist(String filePath_) {
		if (TextUtils.isEmpty(filePath_)){
			return false;
		}
		return makesureFileExist(new File(filePath_));
	}

	/**
	 * 确保某文件或文件夹的父文件夹存在
	 * 
	 * @param file_
	 */
	public static boolean makesureParentDirExist(File file_) {
		if (file_ == null){
			return false;
		}
		final File parent = file_.getParentFile();
		if (parent == null || parent.exists())
			return true;
		return mkdirs(parent);
	}
	
	public static boolean mkdirs(File dir) {
		if (dir == null)
			return false;
		return dir.mkdirs();
	}
	
	public static boolean haveFreeSpace(){
		if(FileUtils.hasSDCardMounted()){
			StatFs st = new StatFs(Environment.getExternalStorageDirectory().getPath());
			int blockSize = st.getBlockSize();
			long available = st.getAvailableBlocks();
			long availableSize = (blockSize*available);
			if( availableSize < Constants.MIN_SDCARD_SPACE){
				return false;
			}
			return true;
		}
		return false;
	}

    public static String getAbsolutePath( Context context, Uri uri ) {
        try {
            return Utils.getUriFile( uri.toString(), context ).getAbsolutePath();
        }
        catch( NullPointerException e ) {
        }
        return null;
    }

	public static String getSDPath(){
		File sdDir = null;
	       boolean sdCardExist = Environment.getExternalStorageState()  
	                           .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
	       if(sdCardExist)  
	       {               
	         sdDir = Environment.getExternalStorageDirectory();//获取跟目录
	      }  
	       if(sdDir!=null)
	    	   return sdDir.toString();
	       else
	    	   return null;
	}
	
	/**
	 * 获取图片指定
	 * @param reflection
	 * @param tag
	 * @param exif
	 * @param defalut
	 * @return
	 */
	
	public static int getTagInt(Reflection reflection,String tag,Object exif,int defalut){
		try{
			String value =(String) reflection.invokeMethod(exif, "getAttribute",
					new Object[] { tag });
			if(value == null){
				return defalut;
			}
			return Integer.valueOf(value);
			
		}catch(Exception e){
			Utils.loge(e);
		}
		return defalut;
	}
	
	public static Object getPicExif(Reflection reflection, String path) {
		Object exifInterface = null;
		try {
			exifInterface = reflection.newInstance(
					"android.media.ExifInterface", new Object[] { path });
		} catch (IOException e) {
			Utils.loge(e);
			exifInterface = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utils.loge(e);
			exifInterface = null;
		}
		return exifInterface;
	}
	
	public static String getMyUid(Context context){
		String uid = "";
		uid = context.getSharedPreferences(Constants.MYUID, 0).getString(Constants.MYUID, "");
		return uid;
	}
	
	public static void setMyUid(Context context, String uid){
		if (uid == null){
			uid = "";
		}
		Editor editor = context.getSharedPreferences(Constants.MYUID, 0).edit();
		editor.putString(Constants.MYUID, uid);
		editor.commit();
	}
	
	public static String getFileNameFromPath(String path){
		if (TextUtils.isEmpty(path)){
			return null;
		}
        int start=path.lastIndexOf("/");  
        int end=path.lastIndexOf(".");  
        if(start!=-1 && end!=-1){  
            return path.substring(start+1,end);    
        }else{  
            return null;  
        }  	          
	}
	
	public static String translationThrowable(Context ctx, Throwable tr) {
		tr = getRootCause(tr);
		if(tr instanceof ChenzaoIOException){
			return ctx.getString(R.string.ChenzaoIOException);
		}else if (tr instanceof ChenzaoApiException) {
            String msg = tr.getMessage();
            /*
             * if (msg.contains(":")) { msg =
             * msg.substring(msg.lastIndexOf(":")+1); }
             */
            String flag = "Reason:";
            if (msg.contains(flag)) {
                msg = msg.substring(msg.indexOf(flag) + flag.length());
            }
            return msg;
        }else if (tr instanceof ChenzaoParseException) {
            return ctx.getString(R.string.ChenzaoParseException);
        }else {
		    if (tr == null || tr.getMessage() == null) {
		        return ctx.getString(R.string.OthersException);
		    } else {
		    	if (tr.getMessage().contains("failed:")){
					return ctx.getString(R.string.ChenzaoIOException);
				}else{
					return tr.getMessage();
				}
			}
		}
	}
	
	/**
	 * 取得异常或者错误的根本原因
	 * 
	 * @param tr
	 * @return
	 */
	public static Throwable getRootCause(Throwable tr) {
		if (tr == null)
			return null;
		Throwable error = null;
		Throwable lastCause, currentCause;
		lastCause = currentCause = tr.getCause();
		while (currentCause != null) {
			lastCause = currentCause;
			currentCause = currentCause.getCause();
		}

		if (lastCause == null) {
			error = tr;
		}
		else {
			error = lastCause;
		}
		return error;
	}
	
    /**
     * 
     * @param error
     * @return 如果error被处理则返回true 否则fasle
     */
    public static boolean handleErrorEvent(Throwable error, final Context ctx){
        final String errorMsg = Utils.translationThrowable( ctx,
                Utils.getRootCause( error ) );
        Utils.showToast(ctx, errorMsg, Toast.LENGTH_LONG);
        return true;
    }

    public static Bitmap getBitmapCache(String path, Map<String, Bitmap> map){
        if (TextUtils.isEmpty(path)) {
            return null;
        }
    	
        if (map != null && map.containsKey(path)) {
            Bitmap bmp = (Bitmap) map.get(path);
            if (bmp == null || bmp.isRecycled()) {
            	map.remove(path);
            }
            return bmp;
        }
        return null;
    }
    
    public static void saveBitmapCache(String path, Bitmap bitmap, Map<String, Bitmap> map){
        if (map == null || bitmap == null || bitmap.isRecycled() || TextUtils.isEmpty(path)) {
            return;
        }

        map.put(path, bitmap);
    }
    
    public static void recycleBitmapCache(Map<String, Bitmap> map){
        int size = map.size();
        for (int i = 0; i < size; i++) {
            Bitmap bmp = map.get(i);
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = null;
        }
        map.clear();
        map = null;
        System.gc();
	
    }
    public static boolean isEmptyOrBlank( CharSequence str ) {
        return (TextUtils.isEmpty(str)) || (str.toString().trim().length() == 0);
    }
    
	public static void getScreenRect(Context ctx_, Rect outrect_) {
		if (ctx_ == null || outrect_ == null){
			return;
		}
		Display screenSize = ((WindowManager) ctx_
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		outrect_.set(0, 0, screenSize.getWidth(), screenSize.getHeight());
	}
	
	public static long calculateLength(CharSequence c) {  
        double len = 0;  
        for (int i = 0; i < c.length(); i++) {  
            int tmp = (int) c.charAt(i);  
            if (tmp > 0 && tmp < 127) {  
                len += 0.5;  
            } else {  
                len++;  
            }  
        }  
        return Math.round(len);  
    } 
	
	public static void cancelAlarmAlert(Context context, TaskScheduler data){
		if(data == null){
			return;
		}
		
		int id = 0;
		try{
			id = Integer.valueOf(data.getID());
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		Intent intent1 = new Intent();
		intent1.setAction(Constants.TASK_SCHEDULER_REMIND_ACTION_1);
		intent1.putExtra("id", data.getID());
		AlarmHelper.getNetInstance(context).cancelAlarm(id + 1, intent1);

		Intent intent2 = new Intent();
		intent2.setAction(Constants.TASK_SCHEDULER_REMIND_ACTION_2);
		intent2.putExtra("id", data.getID());
		AlarmHelper.getNetInstance(context).cancelAlarm(id + 2, intent2);
	}
	
	public static void setAlarmAlert(Context context, TaskScheduler data){
		if(data == null){
			return;
		}
		
		int id = 0;
		try{
			id = Integer.valueOf(data.getID());
		}catch(NumberFormatException e){
			e.printStackTrace();
		}

		if(data.getOnoff1() == 1){
			String time = data.getRemindTime1();
			int hour, min;
			if (!TextUtils.isEmpty(time)) {
				hour = Integer.parseInt(time.substring(0, 2));
				min = Integer.parseInt(time.substring(2, 4));

				Intent intent = new Intent();
				intent.setAction(Constants.TASK_SCHEDULER_REMIND_ACTION_1);
				intent.putExtra("id", data.getID());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, min);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				AlarmHelper.getNetInstance(context).setAlarm(id + 1, intent, calendar.getTimeInMillis());
			}
		}
		
		if(data.getOnoff2() == 1){
			String time = data.getRemindTime2();
			int hour, min;
			if (!TextUtils.isEmpty(time)) {
				hour = Integer.parseInt(time.substring(0, 2));
				min = Integer.parseInt(time.substring(2, 4));

				Intent intent = new Intent();
				intent.setAction(Constants.TASK_SCHEDULER_REMIND_ACTION_2);
				intent.putExtra("id", data.getID());
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, min);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				AlarmHelper.getNetInstance(context).setAlarm(id + 2, intent, calendar.getTimeInMillis());
			}	
		}
	}
	
	public static String getLastTime(TaskScheduler data){
		if(data == null){
			return "0";
		}
		
		int year;
		int month;
		int day;
		
		if(data.getStartTime().equals("") || data.getEndTime().equals("")){
			return "0";
		}
		year = Integer.parseInt(data.getStartTime().substring(0, 4));
		month = Integer.parseInt(data.getStartTime().substring(4, 6));
		day = Integer.parseInt(data.getStartTime().substring(6, 8));
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(System.currentTimeMillis());
		calendar1.set(year, month - 1, day);
		
		if(calendar.compareTo(calendar1) > 0){
			//这里计算后+2天，分别是起始当天和今天，都是算在持续执行第N天内的
			return Long.toString((calendar.getTime().getTime() - calendar1.getTime().getTime()) / (60 * 60 * 24 * 1000) + 2);
		}
		return "0";
	}
	
	public static String format(int x){
		String s=""+x;
		if(s.length()==1){
			s="0"+s;
		}
		return s;
	}
	
    private static Object getMetaValue(Context context, String key)
    {
      Object value = null;

      String packageName = context.getPackageName();
      try
      {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        if (appInfo.metaData != null)
        {
          value = appInfo.metaData.get(key);
        }
      }
      catch (PackageManager.NameNotFoundException e)
      {
        e.printStackTrace();
      }

      return value;
    }
    
    public static String getUmengAppkey(Context context){
        return (String)getMetaValue(context, Constants.UMENG_APPKEY);
    }
    
    public static String getWeixinAppkey(Context context){
        return (String)getMetaValue(context, Constants.WEIXIN_APPKEY);
    }
    
    public static TaskScheduler getTaskFromDbByID(Context context, String id){
    	if (TextUtils.isEmpty(id)){
    		return null;
    	}
    	ChenzaoDBAdapter db = new ChenzaoDBAdapter(context);
		db.open();
		TaskScheduler task = db.getTaskSchedulerById(id);
		db.close();
		return task;
    }
}
