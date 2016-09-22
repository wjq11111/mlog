package sto.service.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import other.AuthProfile;
import sto.common.service.BaseServiceImpl;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.dao.account.AppDao;
import sto.model.account.App;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class AppService extends BaseServiceImpl<App>{
	@Autowired
	AppDao appDao;
	public App getLastVersion(int versiontype){
		List<App> list = appDao.findBySql("select * from mlog_app where versiontype="+versiontype+" order by apkversion desc limit 1", null, App.class);
		if(list !=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	public App getLastNormalVersion(){
		List<App> list = appDao.findBySql("select * from mlog_app where status=0 order by apkversion desc limit 1", null, App.class);
		if(list !=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	public List<Map<String,Object>> getAppList(Page<Object[]> page,Map<String,Object> parammap){
		Parameter parameter = new Parameter();
		String whereSql = "";
		System.out.println(parammap.get("apkversion"));
		System.out.println(parammap.get("versiontype"));
		if(parammap.get("apkversion") != null && !parammap.get("apkversion").equals("")){
			whereSql= " and a.apkversion=:p1 ";
			parameter.put("p1", parammap.get("apkversion"));
		}
		if(parammap.get("versiontype")!=null && !parammap.get("versiontype").equals("")){
			whereSql+=" and a.versiontype=:p2 ";
			parameter.put("p2", parammap.get("versiontype"));
		}
		System.out.println(whereSql);
		String sql = "select a.id,a.publisher userid,b.username,b.name,a.apkversion,a.downloadurl,a.isforceupdate,a.status,a.publishtime,a.description,a.versiontype"
				+" from mlog_app a,platform_t_user b "
				+" where a.publisher=b.id "
				+ whereSql+"order by  a.publishtime desc";
		
		List<Object[]> list = appDao.findBySql(page, sql, parameter).getResult();
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("userid", String.valueOf(o[1]));
				map.put("username", String.valueOf(o[2]));
				map.put("name", String.valueOf(o[3]));
				map.put("apkversion", String.valueOf(o[4]));
				map.put("downloadurl", String.valueOf(o[5]));
				map.put("isforceupdate", String.valueOf(o[6]));
				map.put("status", String.valueOf(o[7]));
				map.put("publishtime", String.valueOf(o[8]));
				map.put("description", String.valueOf(o[9]));
				map.put("versiontype", String.valueOf(o[10]));
				list1.add(map);
			}
		}
		return list1;
	}
}
