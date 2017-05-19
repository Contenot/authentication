package controllers.resources;
/**											
 * Plugin Play Resource Server
 *
 * @author ydeshayes
 *
 * Variables in application.conf:
 *		devResourceServerUrl = "url of the dev resource server"
 *		prodResourceServerUrl = "uurl of the prod resource server"
 *	    modeResourceServer = "prod" or "debug"
 */
//jdkqfjkdhfdjksqhfjdk
///GFSDGfdsgfsdgfd

import play.Application;
import play.Logger;
import play.Plugin;

public class AssetPlugin extends Plugin {

	private static final String ASSET_URL = "asset.url";
	
	private Application app;

	public static String url;
	public static boolean loadOk = false;
	public static String errorMessage = "";


	public AssetPlugin(Application app)
	{
		super();
		this.app = app;
	}


	 public void onStart() {
		 if(pluginVarVerif() == true)
		 {
			url = app.configuration().getString(ASSET_URL);
			Logger.info("Asset URL = "+url);
		 }
		 else
			Logger.error("Asset Error = "+errorMessage);
	 }


	 private boolean pluginVarVerif() {
			if(app.configuration().getString(ASSET_URL)==null) {
				errorMessage += "Error: missing argument "+ASSET_URL+" in application.conf";
				return false;
			}
			
			loadOk = true;
			return true;
		}
		
	public static String getServer(){
		return url;
	}
}
