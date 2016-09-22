package sto.model.account;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * @ClassName: User
 * @Description:
 * @author zzh
 * @date 2014-10-31 11:07:12
 * 
 */
@Entity
@Table(name = "platform_t_accesscontrol")
@DynamicInsert @DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Door implements Serializable{
	
	private Integer id;
	private String controllerid; // 控制器序列号
	private String portid;//控制器商品号
	private String doorname;//门禁别名 
	private String doorstatus; //门禁状态  0为锁定，1为开启，默认为1，用于服务器的紧急锁定，优先级高于门禁策略
	private Integer regularflag; //策略标记，0为不启用，1为启用该策略
	private String regularstarttime; 
	private String regularendtime; 
	private String regularid; 
		
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	
	
	
	public String getRegularid() {
		return regularid;
	}
	public String getControllerid() {
		return controllerid;
	}
	
	public void setControllerid(String controllerid) {
		this.controllerid = controllerid;
	}
	public String getPortid() {
		return portid;
	}
	public void setPortid(String portid) {
		this.portid = portid;
	}
	public String getDoorname() {
		return doorname;
	}
	public void setDoorname(String doorname) {
		this.doorname = doorname;
	}
	public String getDoorstatus() {
		return doorstatus;
	}
	public void setDoorstatus(String doorstatus) {
		this.doorstatus = doorstatus;
	}
	
	
	public Integer getRegularflag() {
		return regularflag;
	}
	public void setRegularflag(Integer regularflag) {
		this.regularflag = regularflag;
	}
	public String getRegularstarttime() {
		return regularstarttime;
	}
	public void setRegularstarttime(String regularstarttime) {
		this.regularstarttime = regularstarttime;
	}
	public String getRegularendtime() {
		return regularendtime;
	}
	public void setRegularendtime(String regularendtime) {
		this.regularendtime = regularendtime;
	}
	public void setRegularid(String regularid) {
		this.regularid = regularid;
	}
	
	
		
}
