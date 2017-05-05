package fr.cea.ig.authentication.cas;
/**											Plugin Play Central Authentication Service(CAS)
*
*
*
*Variables in application.conf:
*								casUrlValidator = "url of the validator page"
*								casUrlLogin = "url of the login page"
*								casRenew = true or false
*								casMode = "prod" or "debug"
*								casTimeOut = "timeout in millisecond" (no time out if == 0)
*/


import fr.cea.ig.authentication.html.HtmlPlugin;
import play.Play;
import play.Plugin;
import play.Application;
import play.Logger;
import fr.cea.ig.authentication.Constants;

public class CasPlugin extends Plugin {

	private static final String CAS_URL_VALIDATOR = "auth.cas.urlvalidator";
	private static final String CAS_URL_LOGIN = "auth.cas.urllogin";
	private static final String CAS_RENEW = "auth.cas.renew";

	private Application app;

	public static String validator;
	public static String login;
	public static String role = "reader";
	public static boolean renew;
	public static String mode;
	public static Integer timeOut;
	public static String applicationCode;
	public static boolean loadOk = false;
	public static String errorMessage = "";


	public CasPlugin(Application app)
	{
		super();
		this.app = app;
	}


	 public void onStart() {
		 if(pluginVarVerif() == true)
		 {
			validator = app.configuration().getString(CAS_URL_VALIDATOR);
			login = app.configuration().getString(CAS_URL_LOGIN);
			renew  = app.configuration().getBoolean(CAS_RENEW);
			applicationCode  = app.configuration().getString(Constants.APPLICATION_CODE);
			if(mode == null){
			 	mode  = app.configuration().getString(Constants.MODE);
			}
			if(timeOut == null){
				timeOut = app.configuration().getInt(Constants.TIMEOUT);
			}
			if(app.configuration().getString(Constants.ROLE)!=null) {
					role = app.configuration().getString(Constants.ROLE);
			}
		 } else {
			Logger.error(errorMessage);
		}
	 }


	 private boolean pluginVarVerif() {
		if(app.configuration().getString(CAS_URL_VALIDATOR)==null) {
			errorMessage += "Error: missing argument auth.cas.urlvalidator in application.conf";
			return false;
		}
		if(app.configuration().getString(CAS_URL_LOGIN)==null) {
			errorMessage += "Error: missing argument auth.cas.urllogin in application.conf";
			return false;
		}
		if(app.configuration().getBoolean(CAS_RENEW)==null) {
			errorMessage += "Error: missing argument auth.cas.renew in application.conf";
			return false;
		}
		if(app.configuration().getString(Constants.MODE)==null) {
			mode = Constants.MODE_DEFAULT;
		}
		if(app.configuration().getString(Constants.TIMEOUT)==null) {
			timeOut = Integer.parseInt(Constants.TIMEOUT_DEFAULT);
		}
		if(app.configuration().getString(Constants.APPLICATION_CODE)==null) {
			errorMessage += "Error: missing argument auth.application in application.conf";
			return false;
		}

		loadOk = true;
		return true;
	}
}
