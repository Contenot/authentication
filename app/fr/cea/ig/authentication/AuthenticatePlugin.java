package fr.cea.ig.authentication;

import fr.cea.ig.authentication.activedirectory.ActiveDirectoryPlugin;
import fr.cea.ig.authentication.cas.CasPlugin;
import fr.cea.ig.authentication.html.HtmlPlugin;
import play.Application;
import play.Plugin;

public class AuthenticatePlugin extends Plugin{

	private static final String AUTHMETHOD = "auth.method";
	
	private static final String AD = "activedirectory";
	private static final String CAS = "cas";
	private static final String HTML = "html";
	
	private Application app;
	private String method = HTML;
	
	
	public AuthenticatePlugin(Application app){
		super();
		this.app = app;
	}

	public void onStart() {
		if(app.configuration().getString(AUTHMETHOD) != null){
			method = app.configuration().getString(AUTHMETHOD);
		}
		
		if(method.equals(AD)){
			ActiveDirectoryPlugin adp = new ActiveDirectoryPlugin(app);
			adp.onStart();
		}else if(method.equals(CAS)){
			CasPlugin cp = new CasPlugin(app);
			cp.onStart();
		}else{
			HtmlPlugin hp = new HtmlPlugin(app);
			hp.onStart();
		}
	}
}
