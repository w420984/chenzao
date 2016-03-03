package com.chenzao.net;

import java.util.List;

import com.chenzao.exception.ChenzaoApiException;
import com.chenzao.exception.ChenzaoIOException;
import com.chenzao.exception.ChenzaoParseException;
import com.chenzao.models.UserInfo;
import com.chenzao.utils.Constants;

import android.content.Context;
import android.os.Bundle;

public class NetEngine {

	private static NetEngine mInstance;
	private Context mContext;
	private String SERVER_HOST = "http://182.92.77.2:8080";
	private String SERVER_USER = "/app/services/user/";
	
	public NetEngine(Context context){
		mContext = context;
	}
	
	public static NetEngine getInstance(Context context){
		if (mInstance == null){
			mInstance = new NetEngine(context);
		}
		return mInstance;
	}
    
	public String register(String userName, String password, String rePassword, String email) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(String.format(
				"%sregister.do?username=%s&password=%s&repassword=%s&email=%s", 
				SERVER_HOST+SERVER_USER, userName, password, rePassword, email));
		
		String content = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		
		return content;
	}
	
	public String login(String userName, String password) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(String.format(
				"%slogin.do?username=%s&password=%s", 
				SERVER_HOST+SERVER_USER, userName, password));
		
		String content = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		
		return content;
	}
	
	
	public String forgetPassword(String userName, String email) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(String.format(
				"%sforgotpassword.do?username=%s&email=%s", 
				SERVER_HOST+SERVER_USER, userName, email));
		
		String content = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		
		return content;
	}

	public String resetPassWord(String userName, String captcha, String password, String rePassword) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(String.format(
				"%sresetpassword.do?username=%s&resetcode=%s&newpassword=%s&renewpassword=%s", 
				SERVER_HOST+SERVER_USER, userName, captcha, password, rePassword));
		
		String content = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		
		return content;
	}

	public String changePassWord(String uuid, String oldPassword, String newPassword, String rePassword) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(String.format(
				"%smodifypassword.do?uuid=%s&password=%s&newpassword=%s&renewpassword=%s", 
				SERVER_HOST+SERVER_USER, uuid, oldPassword, newPassword, rePassword));
		
		String content = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, null, mContext, true, null);
		
		return content;
	}
	
	//查询二维码类型及状态
	public String verifyQrcode(String uuid, String qrCode) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("checkcode.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("code", qrCode);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	//新增文件存储项记录
	public String addNewFileRecord(String uuid, String label, String content, String sound, List<String> pics) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("addrecord.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("label", label);
		if (content != null){
			params.putString("content", content);
		}
		if (sound != null){
			params.putString("sound", sound);
		}
		if (pics != null && !pics.isEmpty()){
			for(int i=0; i<pics.size(); i++){
				params.putString("image"+(i+1), pics.get(i));
			}
		}

		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	//查询文件存储项历史记录
	public String queryFileRecordHistory(String uuid, String offset) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("getrecord.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("offset", offset);
		params.putString("limit", ""+Constants.REQUEST_ITEM_NUM);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	//删除文件存储项记录
	public String deleteFileRecord(String uuid, String recordid) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("deleterecord.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("recordid", recordid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	//搜索文件存储项
	public String searchFileRecord(String uuid, String keyword, String offset) throws ChenzaoIOException, ChenzaoApiException, ChenzaoParseException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("searchrecord.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("keyword", keyword);
		params.putString("limit", ""+Constants.REQUEST_ITEM_NUM);
		params.putString("offset", offset);
		params.putSerializable("order", "asc");
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_GET, params, mContext, true, null);
		return result;
	}
	
	//新增计划任务项
	public String addTaskScheduler(String uuid, String title, String note, String startdate, String enddate, String alarm1time, int alarm1repeat, int alarm1on,
									String alarm2time, int alarm2repeat, int alarm2on, int focustime, String finishnote, String bgimage, int type)
									throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("addschedule.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("title", title);
		params.putString("note", note);
		params.putString("startdate", startdate);
		params.putString("enddate", enddate);
		params.putString("alarm1time", alarm1time);
		params.putInt("alarm1repeat", alarm1repeat);
		params.putInt("alarm1on", alarm1on);
		params.putString("alarm2time", alarm2time);
		params.putInt("alarm2repeat", alarm2repeat);
		params.putInt("alarm2on", alarm2on);	
		params.putInt("focustime", focustime);
		params.putString("finishnote", finishnote);
		params.putString("bgImg", bgimage);
		params.putInt("type", type);
		params.putInt("try", 1);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//删除任务项
	public String deleteTaskScheduler(String uuid, String schedulerId) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("delschedule.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("scheduleid", schedulerId);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//编辑计划任务项
	public String editTaskScheduler(String uuid, String schedulerid, String alarm1time, int alarm1repeat, int alarm1on,
									String alarm2time, int alarm2repeat, int alarm2on, float progress)
									throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("editschedule.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("scheduleid", schedulerid);
		params.putString("alarm1time", alarm1time);
		params.putInt("alarm1repeat", alarm1repeat);
		params.putInt("alarm1on", alarm1on);
		params.putString("alarm2time", alarm2time);
		params.putInt("alarm2repeat", alarm2repeat);
		params.putInt("alarm2on", alarm2on);	
		params.putFloat("progress", progress);	
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//获取指定计划任务信息
	public String getTaskScheduler(String uuid, String shedulerid) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("getschedule.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("scheduleid", shedulerid);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//搜索或列出全部计划任务信息
	public String searchTaskScheduler(String uuid, int limit, int offset, String orderby, String keyword, int type) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("searchschedule.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		if(limit > 0){
			params.putInt("limit", limit);
		}
		params.putInt("offset", offset);
		params.putString("order", orderby);
		params.putString("keyword", keyword);
		params.putInt("type", type);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	//上传当日任务进展
	public String updateDailyTaskScheduler(String uuid, String schedulerid, int dailyrate, String content, List<String> pics, int wechat, String customize) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("dailyupdate.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("scheduleid", schedulerid);
		params.putInt("dailyrate", dailyrate);
		params.putString("content", content);
		params.putString("customize", customize);
		if (pics != null && !pics.isEmpty()){
			for(int i=0; i<pics.size(); i++){
				params.putString("image"+(i+1), pics.get(i));
			}
		}
		params.putInt("wechat", wechat);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String modifyDailyUpdate(String uuid, String schedulerid, String scheduleLogid, int dailyrate, String content, List<String> pics, 
				int wechat, String customize) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("editdailyupdate.do");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("scheduleid", schedulerid);
		params.putString("scheduleLogid", scheduleLogid);
		params.putInt("dailyrate", dailyrate);
		params.putString("content", content);
		params.putString("customize", customize);
		if (pics != null && !pics.isEmpty()){
			for(int i=0; i<pics.size(); i++){
				params.putString("image"+(i+1), pics.get(i));
			}
		}
		params.putInt("wechat", wechat);

		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;
	}
	
	public String getTaskSchedulerDailyUpdate(String uuid,  String schedulerid, String offset) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("getdailyupdate");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		params.putString("limit", ""+Constants.REQUEST_ITEM_NUM);
		params.putString("offset", offset);
		params.putString("scheduleid", schedulerid);
		params.putSerializable("order", "desc");
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;		
	}
	
	public String getUserInfo(String uuid) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("updateprofile");
        
		Bundle params = new Bundle();
		params.putString("uuid", uuid);
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;		
	}
	
	public String updateUserInfo(UserInfo user) throws ChenzaoIOException, ChenzaoParseException, ChenzaoApiException{
		StringBuilder url = new StringBuilder(SERVER_HOST);
        url.append(SERVER_USER);
        url.append("updateprofile");
        
		Bundle params = new Bundle();
		params.putString("uuid", user.getUuid());
		params.putInt("sex", user.getGender());
		params.putInt("age", user.getAge());
		params.putString("astro", user.getConstellation());
		params.putString("dream", user.getExpactWords());
		
		String result = NetUtils.openUrl(url.toString(), NetUtils.METHOD_POST, params, mContext, true, null);
		return result;		
	}
}
