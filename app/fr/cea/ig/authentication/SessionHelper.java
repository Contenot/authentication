package fr.cea.ig.authentication;

import fr.cea.ig.authentication.cas.CasPlugin;
import fr.cea.ig.authentication.html.IAuthenticate;
import play.api.modules.spring.Spring;
import play.mvc.Http;

public class SessionHelper {
	public static final String USER = "NGL_FILTER_USER";
	public static final String TIMEOUT = "NGL_FILTER_TIMEOUT";
	
	
	//Cree la session
		public static void createCookie(Http.Context context, String id) {
			java.util.Date date= new java.util.Date();
			int timeStamp =  (int)date.getTime();
			
			context.session().put(SessionHelper.USER, id);
			context.session().put(SessionHelper.TIMEOUT, String.valueOf(timeStamp));
			
			context.request().setUsername(id);
		}
		
		//Verifie if the session is timed out
		//true if she is timed out or CasPlugin.timeOut == 0
		public static boolean timeOutSession(Http.Context context, int timeOut) {
			if(timeOut != 0){
				java.util.Date date= new java.util.Date();
				int timeStampSession = Integer.parseInt(context.session().get(SessionHelper.TIMEOUT));
				int actualTimeStamp =  (int)date.getTime();
				return ((actualTimeStamp-timeStampSession) >= timeOut);
			} else {
				return false;
			}
		}
		
		public static void clearSession(Http.Context context){
			context.session().clear();
			context.session().remove(USER);
			context.session().remove(TIMEOUT);
		}
		
		public static void setDefaultRole(String login,String role){
			IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
			a.setDefaultRole(login, role);
		}
}
