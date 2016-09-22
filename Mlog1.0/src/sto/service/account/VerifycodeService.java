package sto.service.account;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sto.common.service.BaseServiceImpl;
import sto.dao.account.VerifycodeDao;
import sto.model.account.Verifycode;

/**
 * @ClassName: VerifycodeService
 * @Description: service
 * @author szj
 * @date 2015年4月21日 09:36:47
 * 
 */
@Service
public class VerifycodeService extends BaseServiceImpl<Verifycode> {

	@Resource
	VerifycodeDao verifycodeDao;
	
	@Transactional(readOnly=false)
	public void save(Verifycode verifycode){
		verifycodeDao.save(verifycode);
	}
	
	public List<Verifycode> getVerifycodeList(Verifycode verifycode){
		String sqlString = "select * from mlog_verifycode o where 1 = 1 ";
		if(verifycode != null && verifycode.getMobile() != null && verifycode.getVerifycode() != null){
			sqlString += " and o.mobile = '" + verifycode.getMobile() + "' and o.verifycode = '" + verifycode.getVerifycode() + "'";
			List list = verifycodeDao.findBySql(sqlString,null,Verifycode.class);
			return list;
		}else {
			return null;
		}
	}
}
