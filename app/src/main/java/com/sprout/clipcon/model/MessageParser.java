package com.sprout.clipcon.model;

import org.json.JSONException;

public class MessageParser {


	/**
	 * @param m
	 * 			Message object received from server
	 * @return Contents 
	 * 			Contents object converted from message
	 */
	public static Contents getContentsbyMessage(Message m) throws JSONException {
		Contents rtnContents = new Contents(m.get("contentsType"), m.getLong("contentsSize"), m.get("contentsPKName"), m.get("uploadUserName"), m.get("uploadTime"), m.get("contentsValue"));
		if (m.get("contentsType").equals(Contents.TYPE_IMAGE)) {
            rtnContents.setContentsValue(m.get("imageString"));
		}
		return rtnContents;
	}

	/**
	 * @param imageString
	 * 			The String that transformed the Image received from the server
	 * @return InputStream
	 * 			An InputStream for creating Javafx Image objects
	 */
}
