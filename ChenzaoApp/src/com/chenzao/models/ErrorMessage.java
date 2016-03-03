package com.chenzao.models;


import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.chenzao.exception.ChenzaoParseException;

import android.text.TextUtils;


/**
 * @author nieyu1
 *
 */
public class ErrorMessage extends DataObject implements Serializable {

    private static final long serialVersionUID = -2861166030329864848L;

    
    /**
     * 错误码
     */
    public String errorcode;
    /**
     * 错误信息
     */
    public String errmsg;
	
	public ErrorMessage(){
    }
    
    public ErrorMessage(String xmlStr) throws ChenzaoParseException{
        super(xmlStr);
    }
    
    public ErrorMessage(XmlPullParser _parser) throws ChenzaoParseException{
        initFromParser(_parser);
    }
    
	public ErrorMessage initFromParser(XmlPullParser _parser) throws ChenzaoParseException {
        parser = _parser;
        return parse();
    }
	
    @Override
    public ErrorMessage initFromString(String xmlStr) throws ChenzaoParseException {
    	if(TextUtils.isEmpty(xmlStr)){
    		return null;
    	}
    	if(xmlStr.startsWith("<?xml")){
    		try {
    			parser.setInput(new StringReader(xmlStr));
    		} catch (XmlPullParserException ex) {
    			throw new ChenzaoParseException(ex);
    		}
    		return parse();
    	}else{
    		try {
				parseJson(xmlStr);
			} catch (JSONException e) {
				throw new ChenzaoParseException(PARSE_ERROR);
			}
    		return this;
    	}
    }
    
    private void parseJson(String xmlStr) throws JSONException{
    	JSONObject jsonObject = null;
    	try {
    		jsonObject = new JSONObject(xmlStr);
    		errmsg = jsonObject.optString("errmsg");
    		errorcode = jsonObject.optString("errorcode");
    	} catch (JSONException e) {
    		JSONArray jsonArr = new JSONArray(xmlStr);
    	}
	}

	@Override
    protected ErrorMessage parse() throws ChenzaoParseException {
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("errorcode")) {
                        this.errorcode = parseText(parser);
                    } else if (parser.getName().equals("errmsg")) {
                        this.errmsg = parseText(parser);
                    }
                    break;
                default:
                    break;
                }
            }

        } catch (XmlPullParserException e) {
            throw new ChenzaoParseException(PARSE_ERROR, e);
        } catch (IOException e) {
            throw new ChenzaoParseException(PARSE_ERROR, e);
        }finally{
            parser = null;
        }
        return this;
    }
}