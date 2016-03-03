package com.chenzao.db;

import java.util.ArrayList;
import java.util.List;

import com.chenzao.models.TaskScheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChenzaoDBAdapter{
	private final static int Version = 6;
	//2014-8-15 增加createtime字段
	//2014-11-05 增加背景图片路径字段
	
    public final static String    TASK_SCHEDULER_TABLE     		 = "task_scheduler_table";
    public final static String    TASK_SCHEDULER_ID        		 = "id";
    public final static String    TASK_SCHEDULER_TITLE           = "title";
    public final static String    TASK_SCHEDULER_NOTE            = "note";
    public final static String    TASK_SCHEDULER_START_TIME      = "start_time";
    public final static String    TASK_SCHEDULER_END_TIME        = "end_time";
    public final static String    TASK_SCHEDULER_REMIND_TIME_1   = "remind_time1";
    public final static String    TASK_SCHEDULER_REMIND_TIME_2   = "remind_time2";
    public final static String    TASK_SCHEDULER_REPEAT_TYPE_1   = "repeat_type1";
    public final static String    TASK_SCHEDULER_REPEAT_TYPE_2   = "repeat_type2";
    public final static String    TASK_SCHEDULER_REMIND_ON_OFF_1 = "remind_onoff1";
    public final static String    TASK_SCHEDULER_REMIND_ON_OFF_2 = "remind_onoff2";
    public final static String    TASK_SCHEDULER_CONCENT_TIME    = "concent_time";
    public final static String    TASK_SCHEDULER_COMPLETE_WORDS  = "complete_words";
    public final static String    TASK_SCHEDULER_PROGRESS		 = "progress";
    public final static String    TASK_SCHEDULER_CREATE_TIME 	 = "createdate";
    public final static String    TASK_SCHEDULER_UPDATE_TIME	 = "update_time";
    public final static String	  TASK_SCHEDULER_TYPE            = "task_type";
    public final static String    TASK_SCHEDULER_UPDATE_COUNT    = "update_count";
    public final static String    TASK_SCHEDULER_BG_IMAGE    	 = "bg_image";
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

    	public DatabaseHelper(Context context){
    		super(context, TASK_SCHEDULER_TABLE, null, Version);
    	}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
	        try {
	            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
	            sql.append(TASK_SCHEDULER_TABLE).append(" (")
	            		.append(TASK_SCHEDULER_ID).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_TITLE).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_NOTE).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_START_TIME).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_END_TIME).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_REMIND_TIME_1).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_REPEAT_TYPE_1).append(" INTEGER, ")
	                    .append(TASK_SCHEDULER_REMIND_ON_OFF_1).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_REMIND_TIME_2).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_REPEAT_TYPE_2).append(" INTEGER, ")
	                    .append(TASK_SCHEDULER_REMIND_ON_OFF_2).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_CONCENT_TIME).append(" INTEGER, ")
	                    .append(TASK_SCHEDULER_PROGRESS).append(" FLOAT, ")
	                    .append(TASK_SCHEDULER_CREATE_TIME).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_UPDATE_TIME).append(" LONG, ")
	                    .append(TASK_SCHEDULER_TYPE).append(" INTEGER, ")
	                    .append(TASK_SCHEDULER_UPDATE_COUNT).append(" INTEGER, ")
	                    .append(TASK_SCHEDULER_BG_IMAGE).append(" TEXT, ")
	                    .append(TASK_SCHEDULER_COMPLETE_WORDS).append(" TEXT)");
	            db.execSQL(sql.toString());
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }				
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + TASK_SCHEDULER_TABLE;
	        db.execSQL(sql);
	        //db.execSQL(sql.toString());
	        
	        onCreate(db);			
		}
    	
    }
    
    private SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;
    
    public ChenzaoDBAdapter(Context context){
    	this.mContext = context;
    	mDBHelper = new DatabaseHelper(context);
    }
    
	public ChenzaoDBAdapter open() throws SQLException{
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDBHelper.close();
	}
	
	public void deleteTable(){
		final String sql = "DROP TABLE IF EXISTS " + TASK_SCHEDULER_TABLE;
        try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public void cleanTable(){
		final String sql = "delete from " + TASK_SCHEDULER_TABLE;
		try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public long insert(TaskScheduler data){
		if(data == null){
			return 0;
		}

        ContentValues c = data2ContentValues(data);
        return mDB.insert(TASK_SCHEDULER_TABLE, null, c);
	}
	
	public boolean delete(String taskschedulerId){
		String whereClause = String.format("%s=%s", TASK_SCHEDULER_ID, taskschedulerId);
		if(mDB.delete(TASK_SCHEDULER_TABLE, whereClause, null) > 0){
			return true;
		}
		return false;
	}
	
	public Cursor queryAll(){
		Cursor cursor = mDB.query(TASK_SCHEDULER_TABLE, new String[] {TASK_SCHEDULER_ID, TASK_SCHEDULER_TITLE, TASK_SCHEDULER_NOTE,
							TASK_SCHEDULER_START_TIME, TASK_SCHEDULER_END_TIME, TASK_SCHEDULER_REMIND_TIME_1,
							TASK_SCHEDULER_REPEAT_TYPE_1, TASK_SCHEDULER_REMIND_ON_OFF_1, TASK_SCHEDULER_REMIND_TIME_2,
							TASK_SCHEDULER_REPEAT_TYPE_2, TASK_SCHEDULER_REMIND_ON_OFF_2, TASK_SCHEDULER_CONCENT_TIME,
							TASK_SCHEDULER_COMPLETE_WORDS, TASK_SCHEDULER_PROGRESS, TASK_SCHEDULER_CREATE_TIME,
							TASK_SCHEDULER_UPDATE_TIME, TASK_SCHEDULER_TYPE, TASK_SCHEDULER_UPDATE_COUNT, TASK_SCHEDULER_BG_IMAGE}, null, null, null, null,
							TASK_SCHEDULER_CREATE_TIME + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor query(String taskschedulerId){
		String whereClause = String.format("%s=?", TASK_SCHEDULER_ID);
		Cursor cursor = mDB.query(TASK_SCHEDULER_TABLE, new String[] {TASK_SCHEDULER_ID, TASK_SCHEDULER_TITLE, TASK_SCHEDULER_NOTE,
				TASK_SCHEDULER_START_TIME, TASK_SCHEDULER_END_TIME, TASK_SCHEDULER_REMIND_TIME_1,
				TASK_SCHEDULER_REPEAT_TYPE_1, TASK_SCHEDULER_REMIND_ON_OFF_1, TASK_SCHEDULER_REMIND_TIME_2,
				TASK_SCHEDULER_REPEAT_TYPE_2, TASK_SCHEDULER_REMIND_ON_OFF_2, TASK_SCHEDULER_CONCENT_TIME,
				TASK_SCHEDULER_COMPLETE_WORDS, TASK_SCHEDULER_PROGRESS, TASK_SCHEDULER_CREATE_TIME, 
				TASK_SCHEDULER_UPDATE_TIME, TASK_SCHEDULER_TYPE, TASK_SCHEDULER_UPDATE_COUNT, TASK_SCHEDULER_BG_IMAGE}, whereClause, new String[]{taskschedulerId}, null, null, TASK_SCHEDULER_END_TIME + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean update(TaskScheduler data){
		if(data == null){
			return false;
		}
        ContentValues c = data2ContentValues(data);
        String whereClause = String.format("%s=?", TASK_SCHEDULER_ID);
        if(mDB.update(TASK_SCHEDULER_TABLE, c, whereClause, new String[]{data.getID()}) > 0){
        	return true;
        }
        return false;
	}
	
	public static final ContentValues data2ContentValues( TaskScheduler data ) {
		ContentValues c = new ContentValues();
		c.put(TASK_SCHEDULER_ID, null2Blank(data.getID()));
		c.put(TASK_SCHEDULER_TITLE, null2Blank(data.getTitle()));
		c.put(TASK_SCHEDULER_NOTE, null2Blank(data.getNote()));
		c.put(TASK_SCHEDULER_START_TIME, null2Blank(data.getStartTime()));
		c.put(TASK_SCHEDULER_END_TIME, null2Blank(data.getEndTime()));
		c.put(TASK_SCHEDULER_REMIND_TIME_1, null2Blank(data.getRemindTime1()));
		c.put(TASK_SCHEDULER_REPEAT_TYPE_1, data.getRepeatType1());
		c.put(TASK_SCHEDULER_REMIND_ON_OFF_1, data.getOnoff1());
		c.put(TASK_SCHEDULER_REMIND_TIME_2, null2Blank(data.getRemindTime2()));
		c.put(TASK_SCHEDULER_REPEAT_TYPE_2, data.getRepeatType2());
		c.put(TASK_SCHEDULER_REMIND_ON_OFF_2, data.getOnoff2());
		c.put(TASK_SCHEDULER_CONCENT_TIME, data.getConcentTime());
		c.put(TASK_SCHEDULER_COMPLETE_WORDS, null2Blank(data.getCompleteWords()));
		c.put(TASK_SCHEDULER_PROGRESS, data.getProgress());
		c.put(TASK_SCHEDULER_CREATE_TIME, null2Blank(data.getCreateTime()));
		c.put(TASK_SCHEDULER_UPDATE_TIME, data.getUpdateTime());
		c.put(TASK_SCHEDULER_TYPE, data.getType());
		c.put(TASK_SCHEDULER_UPDATE_COUNT, data.getUpdateCount());
		c.put(TASK_SCHEDULER_BG_IMAGE, null2Blank(data.getBGImage()));
		
		return c;
	}
 
	public static final TaskScheduler cursor2Taskscheduler(Cursor c){
		if(c == null){
			return null;
		}
		
		TaskScheduler ts = new TaskScheduler();
		ts.setID(getStringFromCursor(c, TASK_SCHEDULER_ID));
		ts.setTitle(getStringFromCursor(c, TASK_SCHEDULER_TITLE));
		ts.setNote(getStringFromCursor(c, TASK_SCHEDULER_NOTE));
		ts.setStartTime(getStringFromCursor(c, TASK_SCHEDULER_START_TIME));
		ts.setEndTime(getStringFromCursor(c, TASK_SCHEDULER_END_TIME));
		ts.setRemindTime1(getStringFromCursor(c, TASK_SCHEDULER_REMIND_TIME_1));
		ts.setRepeatType1(getIntFromCursor(c, TASK_SCHEDULER_REPEAT_TYPE_1));
		ts.setOnoff1(getIntFromCursor(c, TASK_SCHEDULER_REMIND_ON_OFF_1));
		ts.setRemindTime2(getStringFromCursor(c, TASK_SCHEDULER_REMIND_TIME_2));
		ts.setRepeatType2(getIntFromCursor(c, TASK_SCHEDULER_REPEAT_TYPE_2));
		ts.setOnoff2(getIntFromCursor(c, TASK_SCHEDULER_REMIND_ON_OFF_2));
		ts.setConcentTime(getIntFromCursor(c, TASK_SCHEDULER_CONCENT_TIME));
		ts.setCompleteWords(getStringFromCursor(c, TASK_SCHEDULER_COMPLETE_WORDS));
		ts.setProgress(getFloatFromCursor(c, TASK_SCHEDULER_PROGRESS));
		ts.setCreateTime(getStringFromCursor(c, TASK_SCHEDULER_CREATE_TIME));
		ts.setUpdateTime(getLongFromCursor(c, TASK_SCHEDULER_UPDATE_TIME));
		ts.setType(getIntFromCursor(c, TASK_SCHEDULER_TYPE));
		ts.setUpdateCount(getIntFromCursor(c, TASK_SCHEDULER_UPDATE_COUNT));
		ts.setBGImage(getStringFromCursor(c, TASK_SCHEDULER_BG_IMAGE));
		return ts;
	}
	
	public static final String null2Blank(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
	
	public static final long getLongFromCursor(Cursor c, String colName){
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return (long) 0;
        }
        
        try {
        	long value = c.getLong(colIndex);
        	return value;
        } catch (NumberFormatException e) {
        }
        return (long) 0;
	}
	
    public static final String getStringFromCursor(Cursor c, String colName) {
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return "";
        }
        String value = c.getString(colIndex);
        if (value == null) {
            value = "";
        }
        return value;
    }
    
    public static final int getIntFromCursor(Cursor c, String colName) {
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return 0;
        }
        try {
            int value = c.getInt(colIndex);
            return value;
        } catch (NumberFormatException e) {
        }
        return 0;
    }
    
    public static final float getFloatFromCursor(Cursor c, String colName) {
    	int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return 0;
        }
        try {
            float value = c.getFloat(colIndex);
            return value;
        } catch (NumberFormatException e) {
        }
        return 0;
    }
    
    public List<TaskScheduler> getTaskSchedulerList() {
        List<TaskScheduler> lst = new ArrayList<TaskScheduler>();
        Cursor c = queryAll();
        if(c == null){
        	return null;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
        	TaskScheduler task = cursor2Taskscheduler(c);
            lst.add(task);
            c.moveToNext();
        }
        if (c != null) {
            c.close();
        }
        return lst;
        
    }
    
    public List<TaskScheduler> getTaskSchedulerListByType(int type) {
        List<TaskScheduler> lst = new ArrayList<TaskScheduler>();
        Cursor c = mDB.query(TASK_SCHEDULER_TABLE, new String[] {TASK_SCHEDULER_ID, TASK_SCHEDULER_TITLE, TASK_SCHEDULER_NOTE,
				TASK_SCHEDULER_START_TIME, TASK_SCHEDULER_END_TIME, TASK_SCHEDULER_REMIND_TIME_1,
				TASK_SCHEDULER_REPEAT_TYPE_1, TASK_SCHEDULER_REMIND_ON_OFF_1, TASK_SCHEDULER_REMIND_TIME_2,
				TASK_SCHEDULER_REPEAT_TYPE_2, TASK_SCHEDULER_REMIND_ON_OFF_2, TASK_SCHEDULER_CONCENT_TIME,
				TASK_SCHEDULER_COMPLETE_WORDS, TASK_SCHEDULER_PROGRESS, TASK_SCHEDULER_CREATE_TIME,
				TASK_SCHEDULER_UPDATE_TIME, TASK_SCHEDULER_TYPE, TASK_SCHEDULER_UPDATE_COUNT, TASK_SCHEDULER_BG_IMAGE}, "task_type=?", new String[] { type+"" }, null, null,
				TASK_SCHEDULER_CREATE_TIME + " DESC");

        if(c == null){
        	return null;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
        	TaskScheduler task = cursor2Taskscheduler(c);
            lst.add(task);
            c.moveToNext();
        }
        if (c != null) {
            c.close();
        }
        return lst;
        
    }
    
    public TaskScheduler getTaskSchedulerById(String id) {
        Cursor c = query(id);
        if(c == null){
        	return null;
        }
        c.moveToFirst();

        TaskScheduler task = cursor2Taskscheduler(c);

        if (c != null) {
            c.close();
        }
        return task;
        
    }    
}
