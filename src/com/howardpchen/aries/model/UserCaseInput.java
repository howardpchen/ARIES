package com.howardpchen.aries.model;

import java.sql.Date;

public class UserCaseInput {
	
	private int userid;
	private int caseid;
	private int eventid;
	private String sessionid;
	private String value;
	private String pageInfo;
	private String comments;
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return the pageInfo
	 */
	public String getPageInfo() {
		return pageInfo;
	}
	/**
	 * @param pageInfo the pageInfo to set
	 */
	public void setPageInfo(String pageInfo) {
		this.pageInfo = pageInfo;
	}
	private Date datetimeentered;
	
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getCaseid() {
		return caseid;
	}
	public void setCaseid(int caseid) {
		this.caseid = caseid;
	}
	public int getEventid() {
		return eventid;
	}
	public void setEventid(int eventid) {
		this.eventid = eventid;
	}
	public String getSessionid() {
		return sessionid;
	}
	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getDatetimeentered() {
		return datetimeentered;
	}
	public void setDatetimeentered(Date datetimeentered) {
		this.datetimeentered = datetimeentered;
	}
	

}
