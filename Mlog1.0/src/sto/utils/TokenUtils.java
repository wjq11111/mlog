package sto.utils;



import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

public class TokenUtils {
	
    final static String ak="TVXa8AdkTDCGMunsibuM2VjIqFqiJE3liceG7GOZ";
    final static String ck="bzLPPTXe2K3iAaG9FOi0bZaW0CSTLK5uIYSJ-grh";
    final static String bucket="hebca-kuaiban";
    final static String key = "kuaiban.plist";
    
    private static UploadManager uploadManager = new UploadManager();

    private static String getUpToken(){
    	Auth auth = Auth.create(ak,ck);
    	// 覆盖上传
    	return auth.uploadToken(bucket,key);
    }
	
    public static void uploadfile(byte[] byteOrFile){
    	
    	
    	//byte[] byteOrFile = null;
    	
    	
    	
    	try {
            Response res = uploadManager.put(byteOrFile, key, getUpToken());
            
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时简单状态信息
           // log.error(r.toString());
           e.printStackTrace();
         }
         
    }
}
