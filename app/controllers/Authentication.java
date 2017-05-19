package controllers;

import java.util.concurrent.TimeUnit;
//dsqjfkqsjfkhdsqkjfdhqkjsfhj
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import views.html.authentication;
import views.html.inscription;
import fr.cea.ig.authentication.SessionHelper;
import fr.cea.ig.authentication.activedirectory.ActiveDirectoryAuthentication;
import fr.cea.ig.authentication.activedirectory.ActiveDirectoryPlugin;
import fr.cea.ig.authentication.cas.CasAuthentication;
import fr.cea.ig.authentication.cas.CasPlugin;
import fr.cea.ig.authentication.html.HtmlAuthentication;
import fr.cea.ig.authentication.html.HtmlPlugin;


public class Authentication extends Controller {

	public static Result authentication(){
		
		if((session().get(SessionHelper.USER) == null))
		{
			Logger.debug("authentication !!!! ");
			
			return ok(authentication.render());
		}else{
			return redirect("/");
		}
	}
	
	public static Result verification() throws Throwable{
		DynamicForm form = play.data.Form.form().bindFromRequest();
		
		String login = form.get("login");
		String password = form.get("password");

		if(ActiveDirectoryPlugin.loadOk){
			ActiveDirectoryAuthentication authenticationAD = new ActiveDirectoryAuthentication(login, password);
			return authenticationAD.call(Http.Context.current()).get(10, TimeUnit.SECONDS);
		}else if(HtmlPlugin.loadOk){			
			HtmlAuthentication authenticationHtml = new HtmlAuthentication(login, password);
			return authenticationHtml.call(Http.Context.current()).get(10, TimeUnit.SECONDS);
		}else {
			return null ;
		}
		
	}
	
	public static Result logOut(){
		SessionHelper.clearSession(Context.current());
		return redirect("/");
	}
	public static  Result inscription() throws Throwable{
		
		if((session().get(SessionHelper.USER) == null))
		{
			Logger.debug("inscription !!!! ");
			
			return ok(inscription.render());
		}else{
			return redirect("/");
		}
	}
	public static  Result inscriptionverif() throws Throwable{
		
		return null;
	}
}
