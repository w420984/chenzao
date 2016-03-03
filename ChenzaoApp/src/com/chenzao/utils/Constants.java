package com.chenzao.utils;

import android.os.Environment;

public class Constants {
	public static boolean DEBUG = true;
	public static String TAG = "chenzao";

	public static int REQUEST_ITEM_NUM = 15;
	public static int MAX_UPLOAD_PIC_NUM = 3;
	public static int TIMEOUT = 30000;
	public static int UPLOAD_TIMEOUT = 60000;
	public static int SOCKET_BUFFER_SIZE = 8192;
	public static String USER_AGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
	public static int HTTP_STATUS_OK = 200;
	public static int HTTP_STATUS_DOWNLOAD_OK = 206;
	public static final int REQUEST_TIMEOUT = 2*60*1000;
	public static String MYUID = "MYUID";
	public static int MIN_SDCARD_SPACE = 1024*1024*10;
	public static String ORI_AUDIO_DIR_SUFFIX = "/" + "chenzao/.chenzao_audio/";
    public static String CAMERA_IMAGE_BUCKET_NAME
			= Utils.getSDPath() + "/chenzao/.chenzao_pic/";
	public static int MAX_INPUT_TEXT_NUM = 500;
	
	public static String DATE_TIME_PICKER_START_TIME = "com.chenzao.taskscheduler.starttime";
	public static String DATE_TIME_PICKER_END_TIME = "com.chenzao.taskscheduler.endtime";
	public static String KEY_YEAR = "year";
	public static String KEY_MONTH = "month";
	public static String KEY_DAY = "day";
	
	public static final String TASK_SCHEDULER_REMIND_ACTION_1 = "com.chenzao.taskscheduler.remind1";
	public static final String TASK_SCHEDULER_REMIND_ACTION_2 = "com.chenzao.taskscheduler.remind2";
	public static final String ADD_TASK_SCHEDULER_SUCCESS = "com.chenzao.taskscheduler.addsuccess";
	public static final String GET_TASK_SCHEDULER_LISTS_SUCCESS = "com.chenzao.taskscheduler.getlistssuccess";
	public static final String UPDATE_TASK_SCHEDULER_SUCCESS = "com.chenzao.taskscheduler.updatelist";
	public static final String DELETE_TASK_SCHEDULER_SUCCESS = "com.chenzao.taskscheduler.deletesuccess";
	public static final String MODIFY_DAILYUPDATE_SUCCESS = "com.chenzao.dailyupdate.modifysuccess";
	
	public static final int TASK_SCHEDULER_CUSTOMRISE = 1;
	public static final int TASK_SCHEDULER_OFFICIAL = 2;
	
	public static final String TASK_SCHEDULER_ID ="taskschedulerid";
	public static final String TASK_SCHEDULER_TYPE = "tasktype";
	public static final int TASK_SCHEDULER_DAILY_UPDATE = 100;
	
	public static final String CUSTOMIZE_FOOD = "customize_food";
	
	public static final String WEB_BROWSER_URL ="url";
	public static final String WEB_BROWSER_CHENZAO_URL ="http://bbs.chenzao.com/forum.php";
	public static final String WEB_BROWSER_CHENZAO_STORE_URL ="http://chenzaoxiaodian.taobao.com";
	
	public static final String UMENG_APPKEY = "UMENG_APPKEY";
	public static final String WEIXIN_APPKEY = "WEIXIN_APPKEY";
	
	public static final String UPDATE_USER_INFO_DISPLAY = "com.chenzao.userinfo.update";
}
