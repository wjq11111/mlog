package sto.model.account;

import java.io.Serializable;
import java.sql.Date;

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
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 * @ClassName: User
 * @Description:
 * @author zzh
 * @date 2014-10-31 11:07:12
 * 
 */
@Entity
@Table(name = "platform_t_accessrecord")
@DynamicInsert @DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Accessrecord implements Serializable{ 
	
	private Integer id;
	private Integer accesscontrolid; // 门禁ID
	private Integer userid;;//用户ID
	private String recordtime;
	private Integer status;
	private User user;
	private String name;//用户姓名
	private Integer type;//开门类型
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAccesscontrolid() {
		return accesscontrolid;
	}
	public void setAccesscontrolid(Integer accesscontrolid) {
		this.accesscontrolid = accesscontrolid;
	}
	
	public String getRecordtime() {
		return recordtime;
	}
	public void setRecordtime(String recordtime) {
		this.recordtime = recordtime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
		
		
}
