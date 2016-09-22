package sto.model.account;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Table(name = "mlog_door_rules")
public class DoorRules implements Serializable {
	private Integer id;
	private Integer userid;
	private Integer accesscontrolid;	//门禁ID
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
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

	public Integer getAccesscontrolid() {
		return accesscontrolid;
	}

	public void setAccesscontrolid(Integer accesscontrolid) {
		this.accesscontrolid = accesscontrolid;
	}



}