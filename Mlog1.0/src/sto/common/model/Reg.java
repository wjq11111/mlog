package sto.common.model;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 新注册
 */

public class Reg  {
	
	private String loginname ;//登录名
	private String password;//邮箱帐号
	private String companyName;// 单位名称 
	private String contact;//联系人
	private String mobile;//手机号
	private String IDNumber;//身份证号
	private String certReg;//证书注册
	private String op;//操作：1、注册reg ;2、下载证书downCert
	//private String regType;//注册类型： 个人注册、单位注册
	
	/*非页面参数*/
	private String uid;//单位id
	public String getLoginname() {
		return loginname;
	}
	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	private String certId;
	private String signCert;//签名证书
	private String encryptCert;//加密证书
	private String verifyCode;//手机号验证码
	
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getSignCert() {
		return signCert;
	}
	public void setSignCert(String signCert) {
		this.signCert = signCert;
	}
	public String getEncryptCert() {
		return encryptCert;
	}
	public void setEncryptCert(String encryptCert) {
		this.encryptCert = encryptCert;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getIDNumber() {
		return IDNumber;
	}
	public void setIDNumber(String number) {
		IDNumber = number;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getVerifyCode() {
		return verifyCode;
	}
	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}
	public String getCertReg() {
		return certReg;
	}
	public void setCertReg(String certReg) {
		this.certReg = certReg;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}

	
	

}
