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
 * @ClassName: Leave
 * @Description:
 * @author song
 * @date 2015-4-27 11:07:12
 * 
 */
@Entity
@Table(name = "mlog_leave")
@DynamicInsert @DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Leave implements Serializable{
	
	private Integer id;
	private Integer userid;
	private String name; //姓名
	
	private String divid;
	private Integer deptid;

	
	private Dept dept;

	
	private Unit unit;
	private String deptname;
	private String divname;
	private Date leavedate;
	private String leavetype;
	private User user;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	
	

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "deptid")
	@NotFound(action = NotFoundAction.IGNORE)
	public Dept getDept() {
		return dept;
	}
	public void setDept(Dept dept) {
		this.dept = dept;
	}

	@Transient
	public String getDeptname() {
		if(null != dept){
			return dept.getDeptname();
		}else {
			return "";
		}
	}
	public void setDeptname(String deptname) {
		this.deptname = deptname;
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
	public Date getLeavedate() {
		return leavedate;
	}
	public void setLeavedate(Date leavedate) {
		this.leavedate = leavedate;
	}
	public String getLeavetype() {
		return leavetype;
	}
	public void setLeavetype(String leavetype) {
		this.leavetype = leavetype;
	}
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "userid")
	@NotFound(action = NotFoundAction.IGNORE)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	@Transient
	public String getName() {
		if(null != user){
			return user.getName();
		}else {
			return "";
		}
	}
	public void setName(String name) {
		this.name = name;
	}
}
