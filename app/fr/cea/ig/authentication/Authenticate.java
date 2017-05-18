package fr.cea.ig.authentication;
///Sc jkdfqd
import fr.cea.ig.authentication.activedirectory.ActiveDirectoryAuthentication;
import fr.cea.ig.authentication.activedirectory.ActiveDirectoryPlugin;
import fr.cea.ig.authentication.cas.CasAuthentication;
import fr.cea.ig.authentication.cas.CasPlugin;
import fr.cea.ig.authentication.html.HtmlAuthentication;
import fr.cea.ig.authentication.html.HtmlPlugin;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.Logger;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Result;

public class Authenticate extends Action.Simple{

	public static String BOTUSERAGENT = "bot";
	
	public Authenticate(){
		
	}
	
	public Authenticate(Action delegate){
		this.delegate = delegate;
	}
	
	@Override
	public F.Promise<Result> call(Context context) throws Throwable {
		
		String userAgent = context.request().getHeader("User-Agent");
		if(userAgent != null && userAgent.equals(BOTUSERAGENT)){
			//HtmlAuthentication authentificationMethod = new HtmlAuthentication();
			context.request().setUsername("ngsrg");
			return delegate.call(context);
		}else if(userAgent.contains("Honeywell")){
			context.request().setUsername("scanner");
			return delegate.call(context);
		}else{
			if(ActiveDirectoryPlugin.loadOk){
				ActiveDirectoryAuthentication authentificationMethod = new ActiveDirectoryAuthentication(delegate);
				return authentificationMethod.call(context);
			}else if(CasPlugin.loadOk){
				CasAuthentication authentificationMethod = new CasAuthentication(delegate);
				return authentificationMethod.call(context);
			}else if(HtmlPlugin.loadOk){
				HtmlAuthentication authentificationMethod = new HtmlAuthentication(delegate);
				return authentificationMethod.call(context);
			}else{
				context.request().setUsername("ngsrg");
				return delegate.call(context);
			}
		}
	}
}
