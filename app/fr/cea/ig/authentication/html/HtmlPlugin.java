package fr.cea.ig.authentication.html;

import play.Application;
import play.Logger;
import play.Plugin;
import fr.cea.ig.authentication.Constants;

public class HtmlPlugin  extends Plugin{
	
	public static boolean loadOk = false;
	public static String applicationCode;
	public static String mode;
	public static String role = "reader";
	private static String errorMessage = "";
	public static Integer timeOut;
	
	private Application app;

	public HtmlPlugin(Application app){
		super();
		this.app = app;
	}
	
	public void onStart() {
		 if(pluginVarVerif() == true)
		 {
			 applicationCode = app.configuration().getString(Constants.APPLICATION_CODE);
			 if(app.configuration().getString(Constants.TIMEOUT)!=null) {
				 timeOut = app.configuration().getInt(Constants.TIMEOUT);
			 }
			 if(mode == null){
				 	mode  = app.configuration().getString(Constants.MODE);
			 }
			 if(app.configuration().getString(Constants.ROLE)!=null) {
					role = app.configuration().getString(Constants.ROLE);
			 }			
		 } else {
			Logger.error(errorMessage);
		}
		 
	 }
	
	private boolean pluginVarVerif() {

		if (app.configuration().getString(Constants.APPLICATION_CODE) == null) {
			errorMessage += "Error: missing argument  auth.ad.application in application.conf";
			return false;
		}

		if (app.configuration().getString(Constants.TIMEOUT) == null) {
			timeOut = Integer.parseInt(Constants.TIMEOUT_DEFAULT);
		}

		if (app.configuration().getString(Constants.MODE) == null) {
			mode = Constants.MODE_DEFAULT;
		}
		loadOk = true;
		return true;
	}
	
}
