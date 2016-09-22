package sto.form;

import java.util.List;

public class MsgForm {
	private Integer id;
	private Integer userid;
	private String username;
	private String content;
	private String image;
	private String time;
	private String longitude;
	private String latitude;
	private String address;
	private String meetingman; //会议参加人员：
	private int signcount;//签到人数：
	private int leavecount;// 请假人数：
	
	private List<MsgReplyForm> replies;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public List<MsgReplyForm> getReplies() {
		return replies;
	}
	public void setReplies(List<MsgReplyForm> replies) {
		this.replies = replies;
	}
	public String getMeetingman() {
		return meetingman;
	}
	public void setMeetingman(String meetingman) {
		this.meetingman = meetingman;
	}
	public int getSigncount() {
		return signcount;
	}
	public void setSigncount(int  signcount) {
		this.signcount = signcount;
	}
	public int  getLeavecount() {
		return leavecount;
	}
	public void setLeavecount(int leavecount) {
		this.leavecount = leavecount;
	}
}
