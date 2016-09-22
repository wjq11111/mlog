package sto.service.account;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sto.common.service.BaseServiceImpl;
import sto.common.util.Page;
import sto.common.util.StringReg;
import sto.dao.account.MsgDao;
import sto.form.MsgForm;
import sto.form.MsgReplyForm;
import sto.model.account.Msg;
import sto.model.account.SysSettings;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class MsgService extends BaseServiceImpl<Msg>{
	@Autowired
	private MsgDao msgDao;
	@Resource
	MsgReplyService msgReplyService;
	public List<MsgForm> getMsgList(Integer userid,String userids,Page<Object[]> page){
		String whereSql = "";
		if(!StringUtils.isBlank(userids)){
			whereSql = " and a.publisher in ("+userids+") ";
		}
		String sql = "select a.id,a.publisher userid,b.name username,a.content,a.image,a.createtime time,a.lgt longitude,a.lat latitude,a.addr address "
				+" from mlog_msg a,platform_t_user b,mlog_msg_receiver c "
				+" where a.publisher=b.id and a.id=c.msgid and a.types='0' and c.receiver="+userid +" "
				+ whereSql
				+ " order by createtime desc ";
		List<Object[]> list = msgDao.findBySql(page, sql).getResult();
		List<MsgForm> formlist = new ArrayList<MsgForm>();
		if(list != null){
			for(Object[] o : list){
				MsgForm form = new MsgForm();
				form.setId(Integer.parseInt(String.valueOf(o[0])));
				form.setUserid(Integer.parseInt(String.valueOf(o[1])));
				form.setUsername(String.valueOf(o[2]));
				form.setContent(StringReg.replaceToHref(String.valueOf(o[3])));
				form.setImage(String.valueOf(o[4]));
				form.setTime(String.valueOf(o[5]));
				form.setLongitude(String.valueOf(o[6]));
				form.setLatitude(String.valueOf(o[7]));
				form.setAddress(String.valueOf(o[8]));
				List<MsgReplyForm> replyList = msgReplyService.getMsgReplyList(form.getId());
				form.setReplies(replyList);
				formlist.add(form);
			}
		}
		return formlist;
	}
	
	public List<MsgForm> getMeetingList(Integer userid,String userids,Page<Object[]> page){
		String whereSql = "";
		if(!StringUtils.isBlank(userids)){
			whereSql = " and a.publisher in ("+userids+") ";
		}
		String sql = "select a.id,a.publisher userid,b.name username,a.content,a.image,a.createtime time,a.lgt longitude,a.lat latitude,a.addr address "
				+" from mlog_msg a,platform_t_user b,mlog_msg_receiver c "
				+" where a.publisher=b.id and a.id=c.msgid and a.types='1' and c.receiver="+userid +" "
				+ whereSql
				+ " order by createtime desc ";
		List<Object[]> list = msgDao.findBySql(page, sql).getResult();
		
		List<MsgForm> formlist = new ArrayList<MsgForm>();
		if(list != null){
			for(Object[] o : list){
				String sql1="SELECT  b.name from platform_t_user b,mlog_msg_receiver c WHERE c.receiver=b.id and msgid="+Integer.parseInt(String.valueOf(o[0]));
				List<String> list1=msgDao.findBySql(sql1);
				
				String meetingman1 =list1.toString();
				String meetingman=meetingman1.substring(1, meetingman1.length()-1);
				String sql2="SELECT COUNT(DISTINCT replyer) num from mlog_msg_reply WHERE recontent='"+"签到！"+"' and msgid="+Integer.parseInt(String.valueOf(o[0]));
			    List<BigInteger> list2=msgDao.findBySql(sql2);
				int Signcount=Integer.parseInt(list2.get(0).toString());
			    MsgForm form = new MsgForm();
				form.setId(Integer.parseInt(String.valueOf(o[0])));
				form.setUserid(Integer.parseInt(String.valueOf(o[1])));
				form.setUsername(String.valueOf(o[2]));
				form.setContent(StringReg.replaceToHref(String.valueOf(o[3])));
				form.setImage(String.valueOf(o[4]));
				form.setTime(String.valueOf(o[5]));
				form.setLongitude(String.valueOf(o[6]));
				form.setLatitude(String.valueOf(o[7]));
				form.setAddress(String.valueOf(o[8]));
				form.setMeetingman(meetingman);
				form.setSigncount(Signcount);
				form.setLeavecount(list1.size()-Signcount);
				List<MsgReplyForm> replyList = msgReplyService.getMsgReplyList(form.getId());
				form.setReplies(replyList);
				formlist.add(form);
			}
		}
		return formlist;
	}

	private int Integer(String string) {
		// TODO Auto-generated method stub
		return 0;
	}
}
