package fr.cea.ig.authentication.activedirectory;

import fr.cea.ig.authentication.html.HtmlPlugin;
import play.Application;
import play.Logger;
import play.Plugin;
import fr.cea.ig.authentication.Constants;

public class ActiveDirectoryPlugin  extends Plugin{
	
	private static final String SERVER_ADDRESS = "auth.ad.server";
	private static final String DOMAIN = "auth.ad.domain";
	
	private Application app;
	
	public static String serverAddress;
	public static String domain;
	public static String role = "reader";
	public static Integer timeOut;
	public static String applicationCode = "";
	public static String mode;
	
	public static String errorMessage = "";
	public static boolean loadOk= false;
	
	
	
	public ActiveDirectoryPlugin(Application app){
		super();
		this.app = app;
	}
	
	public void onStart() {
		 if(pluginVarVerif() == true)
		 {
			 serverAddress = app.configuration().getString(SERVER_ADDRESS);
			 domain = app.configuration().getString(DOMAIN);
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
			if(app.configuration().getString(SERVER_ADDRESS)==null) {
				errorMessage += "Error: missing argument auth.ad.server in application.conf";
				return false;
			}
			
			if(app.configuration().getString(DOMAIN)==null) {
				errorMessage += "Error: missing argument auth.ad.domain in application.conf";
				return false;
			}
			
			if(app.configuration().getString(Constants.APPLICATION_CODE)==null) {
				errorMessage += "Error: missing argument  auth.ad.application in application.conf";
				return false;
			}
			
			if(app.configuration().getString(Constants.TIMEOUT)==null) {
				timeOut = Integer.parseInt(Constants.TIMEOUT_DEFAULT);
			}
			
			if(app.configuration().getString(Constants.MODE)==null) {
				mode = Constants.MODE_DEFAULT;
			}

			loadOk = true;
			return true;
		}
}
