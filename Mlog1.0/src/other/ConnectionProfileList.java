package other;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Umut Gokbayrak
 */
public class ConnectionProfileList {
	private static Log log = LogFactory.getLog(ConnectionProfileList.class);
	public static HashMap conList = new HashMap();

	/**
	 * 
	 */
	public ConnectionProfileList() {
		super();
	}
	
	public void addConnectionProfile(ConnectionProfile con) {
		if (con == null) return;
		conList.put(con.getShortName(), con);
	}

	public static HashMap getConList() {
		return conList;
	}
	
	public static ConnectionProfile getProfileByShortName(String shortName) {
		ConnectionProfile con = (ConnectionProfile)conList.get(shortName);
		if (con == null) {
			log.warn("The Shortname searched at the ConnectionProfileList does not correspond to a ConnectionProfile");
			return null;
		}
		return con;
	}
    
    public static ConnectionProfile getDefaultProfile()
    {
        if(conList.size()==0)
            return null;
        
        Iterator i=conList.values().iterator();
        return (ConnectionProfile)i.next();
    }
}
