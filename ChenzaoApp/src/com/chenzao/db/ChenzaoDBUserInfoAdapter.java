package com.chenzao.db;

import java.util.ArrayList;
import java.util.List;

import com.chenzao.models.UserInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChenzaoDBUserInfoAdapter {
	private final static int Version = 1;
	public final static String    USER_INFO_TABLE     		 = "user_info_table";
	
	public final static String    USER_INFO_UUID				 = "uuid";
    public final static String    USER_INFO_NICK_NAME     		 = "nick";
    public final static String    USER_INFO_GENDER        		 = "gender";
    public final static String    USER_INFO_AGE           		 = "age";
    public final static String    USER_INFO_CONSTELLATION        = "note";
    public final static String    USER_INFO_EXPACT_SELF      	 = "expact";

    
    private static class UserInfoDatabaseHelper extends SQLiteOpenHelper{

    	public UserInfoDatabaseHelper(Context context){
    		super(context, USER_INFO_TABLE, null, Version);
    	}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
	        try {
	            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
	            sql.append(USER_INFO_TABLE).append(" (")
	            		.append(USER_INFO_UUID).append(" TEXT, ")
	            		.append(USER_INFO_NICK_NAME).append(" TEXT, ")
	                    .append(USER_INFO_GENDER).append(" INTEGER, ")
	                    .append(USER_INFO_AGE).append(" INTEGER, ")
	                    .append(USER_INFO_CONSTELLATION).append(" TEXT, ")
	                    .append(USER_INFO_EXPACT_SELF).append(" TEXT)");
	            db.execSQL(sql.toString());
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }				
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + USER_INFO_TABLE;
	        db.execSQL(sql);
	        //db.execSQL(sql.toString());
	        
	        onCreate(db);			
		}
    	
    }
    
    private SQLiteDatabase mDB;
    private UserInfoDatabaseHelper mDBHelper;
    private Context mContext;
    
    public ChenzaoDBUserInfoAdapter(Context context){
    	this.mContext = context;
    	mDBHelper = new UserInfoDatabaseHelper(context);
    }
    
	public ChenzaoDBUserInfoAdapter open() throws SQLException{
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDBHelper.close();
	}
	
	public void deleteTable(){
		final String sql = "DROP TABLE IF EXISTS " + USER_INFO_TABLE;
        try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public void cleanTable(){
		final String sql = "delete from " + USER_INFO_TABLE;
		try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public long insert(UserInfo data){
		if(data == null){
			return 0;
		}

        ContentValues c = data2ContentValues(data);
        return mDB.insert(USER_INFO_TABLE, null, c);
	}
	
	public boolean delete(String uuid){
		String whereClause = String.format("%s=%s", USER_INFO_UUID, uuid);
		if(mDB.delete(USER_INFO_TABLE, whereClause, null) > 0){
			return true;
		}
		return false;
	}
	
	public Cursor queryAll(){
		Cursor cursor = mDB.query(USER_INFO_TABLE, new String[] {USER_INFO_UUID, USER_INFO_NICK_NAME, USER_INFO_GENDER, USER_INFO_AGE,
				USER_INFO_CONSTELLATION, USER_INFO_EXPACT_SELF}, null, null, null, null,
				USER_INFO_UUID + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor query(String uuid){
		String whereClause = String.format("%s=?", USER_INFO_UUID);
		Cursor cursor = mDB.query(USER_INFO_TABLE, new String[] {USER_INFO_UUID, USER_INFO_NICK_NAME, USER_INFO_GENDER, USER_INFO_AGE,
				USER_INFO_CONSTELLATION, USER_INFO_EXPACT_SELF}, whereClause, new String[]{uuid}, null, null, USER_INFO_UUID + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean update(UserInfo data){
		if(data == null){
			return false;
		}
        ContentValues c = data2ContentValues(data);
        String whereClause = String.format("%s=?", USER_INFO_UUID);
        if(mDB.update(USER_INFO_TABLE, c, whereClause, new String[]{data.getUuid()}) > 0){
        	return true;
        }
        return false;
	}
	
	public static final ContentValues data2ContentValues( UserInfo data ) {
		ContentValues c = new ContentValues();
		c.put(USER_INFO_UUID, null2Blank(data.getUuid()));
		c.put(USER_INFO_NICK_NAME, null2Blank(data.getNickName()));
		c.put(USER_INFO_EXPACT_SELF, null2Blank(data.getExpactWords()));
		c.put(USER_INFO_GENDER, data.getGender());
		c.put(USER_INFO_AGE, data.getAge());
		c.put(USER_INFO_CONSTELLATION, null2Blank(data.getConstellation()));
		
		return c;
	}
 
	public static final UserInfo cursor2UserInfo(Cursor c){
		if(c == null){
			return null;
		}
		
		String uid = getStringFromCursor(c, USER_INFO_UUID);
		String nick = getStringFromCursor(c, USER_INFO_NICK_NAME);
		String expact = getStringFromCursor(c, USER_INFO_EXPACT_SELF);
		int gender = getIntFromCursor(c, USER_INFO_GENDER);
		int age = getIntFromCursor(c, USER_INFO_AGE);
		String constellation = getStringFromCursor(c, USER_INFO_CONSTELLATION);
		
		UserInfo user = new UserInfo(uid, nick, gender, age, constellation, expact);

		return user;
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
    
    public UserInfo getUserInfoByUuid(String uuid) {
        Cursor c = query(uuid);
        if(c == null){
        	return null;
        }
        c.moveToFirst();

        UserInfo user = cursor2UserInfo(c);

        if (c != null) {
            c.close();
        }
        return user;
    }
    
    public List<UserInfo> getUserList() {
        List<UserInfo> lst = new ArrayList<UserInfo>();
        Cursor c = queryAll();
        if(c == null){
        	return null;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
        	UserInfo user = cursor2UserInfo(c);
            lst.add(user);
            c.moveToNext();
        }
        if (c != null) {
            c.close();
        }
        return lst;
        
    }
}
