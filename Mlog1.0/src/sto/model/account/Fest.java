package sto.model.account;

import java.io.Serializable;
//import java.util.Date;
import java.sql.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
 * @ClassName: Fest
 * @Description:
 * @author zzh
 * @date 2015-4-27 11:07:12
 * 
 */
@Entity
@Table(name = "mlog_fest")
@DynamicInsert @DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Fest implements Serializable{
	
	private Integer id;
//	private String divid;

	private Date date;
	private String remark;//节日备注信息
	private Unit unit;
	private String divname;
//	private Dept dept;
//	private Role role;
	private String rolename;
	
	
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
//	public String getDivid() {
//		return divid;
//	}
//	public void setDivid(String divid) {
//		this.divid = divid;
//	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}



	
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "divid",referencedColumnName = "divid", unique = true, nullable = true)
	@NotFound(action = NotFoundAction.IGNORE)
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	@Transient
	public String getDivname() {
		if(null != unit){
			return unit.getDivname();
		}else {
			return "";
		}
	}
	public void setDivname(String divname) {
		this.divname = divname;
	}
//	public String getPushchannelid() {
//		return pushchannelid;
//	}
//	public void setPushchannelid(String pushchannelid) {
//		this.pushchannelid = pushchannelid;
//	}
//	public String getPushuserid() {
//		return pushuserid;
//	}
//	public void setPushuserid(String pushuserid) {
//		this.pushuserid = pushuserid;
//	}
//	public String getImei() {
//		return imei;
//	}
//	public void setImei(String imei) {
//		this.imei = imei;
//	}
//	public String getDeviceid() {
//		return deviceid;
//	}
//	public void setDeviceid(String deviceid) {
//		this.deviceid = deviceid;
//	}
//	public String getPackageversion() {
//		return packageversion;
//	}
//	public void setPackageversion(String packageversion) {
//		this.packageversion = packageversion;
//	}
}
