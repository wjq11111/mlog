package sto.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

//import com.alibaba.fastjson.JSONObject;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
//import com.alibaba.fastjson.*;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
//import org.claros.groupware.admin.controllers.SettingController;
//import org.claros.groupware.admin.controllers.SettingControllerFactory;

import sun.net.www.http.HttpClient;

/**
 * 参考：RESTAPI_JSON.java
 * 
 * @author Administrator
 * 
 */

public class HttpHandler {

	private final static String HEADER_RESULT_CODE = "Result-Code";
	private final static String HEADER_ERROR_MESSAGE = "errorMessage";
	
	public static JSONObject doPost(List p,String url) throws Exception {

	/*	SettingController controller = SettingControllerFactory.getMailboxSettingController();
		String projectId = controller.getSetting("project.id").getValue();//项目id
*/		if(p==null){
			p = new ArrayList();
		}
	//	p.add(new BasicNameValuePair("ProjectID", projectId));
		
		return basePost(p,url);
	}
	

public static JSONObject basePost(List p,String url) throws Exception {

		HttpPost httpPost = null;
		try {
			/*SettingController controller = SettingControllerFactory
					.getMailboxSettingController();
			String onlineIp = controller.getSetting("online.ip").getValue();
*/
			//String onlineIp="http://121.28.48.243:9001/Hebca/interface/";
			String onlineIp=MlogPM.get("online.url");
			DefaultHttpClient httpclient = new DefaultHttpClient();
			httpPost = new HttpPost(onlineIp+url);
			
			
			httpPost.setEntity(new UrlEncodedFormEntity(p,HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httpPost);
			checkError(httpPost, response);
			
			JSONObject ret = (JSONObject)getResponseObject(response);
			if(ret==null){
				throw new Exception("请求正常,但无返回结果");
			}
			return ret;
			
		} finally {
			if (httpPost != null)
				httpPost.releaseConnection();
		}
	}

	
	
private static void checkError(HttpRequestBase method, HttpResponse response)
			throws Exception {
		int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			method.abort(); // close connection
			throw new Exception("http response " + status);
		}

		String resultCode = getHeaderValue(response, HEADER_RESULT_CODE);
		if (!resultCode.equals("")) {
			int code = Integer.parseInt(resultCode);
			if (code != 0) {
				String message = "";
				String m = getHeaderValue(response, HEADER_ERROR_MESSAGE);
				if (m.equals(""))
					message = "错误号为：" + code;
				else {
					try {
						message = URLDecoder.decode(m, "utf-8");
					} catch (Exception e) {
						message = "错误号为：" + code;
					}
				}
				method.abort(); 
			/*	if (code == ServerResponseException.ERROR_SESSION_INVALID) {

					String random = "";
					try {
						JSONObject ret = (JSONObject) getResponseObject(response);
						random = ret.getString("randomString");
					} catch (Exception e) {
						e.printStackTrace();
					}

					throw new SessionInvalidException(code, message, random);
				} else
					throw new ServerResponseException(code, message);*/
			}
		}
	}
	
	private static String getHeaderValue(HttpResponse response, String headerName) {
		Header[] headers = response.getHeaders(headerName);
		if (headers.length != 0) {
			String header = headers[0].toString();
			String[] headerArr = header.split(":");
			if (headerArr.length != 2)
				return "";
			else
				return headerArr[1].trim();
		} else
			return "";
	}
	
	private static Object getResponseObject(HttpResponse response) throws Exception {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			StringBuffer sb = new StringBuffer("");
			String line = "";

			while ((line = in.readLine()) != null) {
				sb.append(line);
			}

			String ret = sb.toString().trim();
			if (ret.equals(""))
				return null;
			String s = URLDecoder.decode(sb.toString(), "utf-8");
			
			Object object = new JSONTokener(s).nextValue();
			return object;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
