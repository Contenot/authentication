package fr.cea.ig.authentication.activedirectory;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import fr.cea.ig.authentication.Authenticate;
import fr.cea.ig.authentication.SessionHelper;
import fr.cea.ig.authentication.html.IAuthenticate;

import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;

public class ActiveDirectoryAuthentication extends Action.Simple{

	private String user, password = "";
	private Action actionDelegate = null;
	
	public ActiveDirectoryAuthentication(){
		this.user = "";
		this.password = "";
	}
	
	public ActiveDirectoryAuthentication(Action delegate){
		this.actionDelegate = delegate;
		this.user = "";
		this.password = "";
	}
	
	public ActiveDirectoryAuthentication(String user, String password){
		this.user = user;
		this.password = password;
	}
	
	public  F.Promise<Result> call(final play.mvc.Http.Context context) throws Throwable {	
		if(delegate != null){
			this.actionDelegate = delegate;
		}
		
		if((context.request().uri().equals(controllers.routes.Authentication.authentication().url()) && (context.session().get(SessionHelper.USER) == null)) || (context.request().uri().equals(controllers.routes.Authentication.verification().url()) && this.user.equals("") && this.password.equals(""))){
			return this.actionDelegate.call(context);
		}else if(!ActiveDirectoryPlugin.mode.equals("prod")){
			SessionHelper.createCookie(context, "ngsrg");
			return this.actionDelegate.call(context);
		}else{
			if(context.session().isEmpty() == true || context.session().get(SessionHelper.USER) == null){
				if(authenticate(this.user,this.password)){
					SessionHelper.createCookie(context, this.user);
					Logger.debug("AUTH OK");
					IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
					if(a.isUserAccessApplication(this.user, ActiveDirectoryPlugin.applicationCode)){
						SessionHelper.setDefaultRole(this.user,ActiveDirectoryPlugin.role);
						context.request().setUsername(this.user);
						if(context.session().get("REFER") != null){
						return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								     return redirect(context.session().get("REFER"));
								    }
								  }
							 );
							
						}else{
							return Promise.promise(
									 new Function0<Result>() {
										    public Result apply() {
										     return redirect("/");
										    }
										  }
									 );
						}
					}else{
					return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								     return unauthorized();
								    }
								  }
							 );
					}
				}else{
					setRefer(context);
					Logger.debug("AUTH FAIL");
					return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								     return redirect(controllers.routes.Authentication.authentication().url());
								    }
								  }
							 );
				}
			}else if(SessionHelper.timeOutSession(context, ActiveDirectoryPlugin.timeOut)){
				context.session().clear();
				setRefer(context);
				Logger.debug("TIME OUT");
				return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								     return redirect(controllers.routes.Authentication.authentication().url());
								    }
								  }
							 );
			} else if((context.session().get(SessionHelper.USER) != null)) {
				IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
				if(a.isUserAccessApplication(context.session().get(SessionHelper.USER).toLowerCase(), ActiveDirectoryPlugin.applicationCode)){
					SessionHelper.setDefaultRole(context.session().get(SessionHelper.USER).toLowerCase(),ActiveDirectoryPlugin.role);
					context.request().setUsername(context.session().get(SessionHelper.USER).toLowerCase());
					if(this.actionDelegate != null){
						return this.actionDelegate.call(context);
					}else{
						redirect("/");
					}
				}else{
				return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								     return unauthorized();
								    }
								  }
							 );
				}
			}
		}	
		return Promise.promise(
		 new Function0<Result>() {
								    public Result apply() {
								     return internalServerError();
								    }
								  }
							 );
	}
	

	private void setRefer(Context context){
		if(!context.request().uri().equals(controllers.routes.Authentication.authentication().url()) && !context.request().uri().equals(controllers.routes.Authentication.verification().url())){
			context.session().put("REFER", context.request().uri());
		}
	}
	
	private boolean authenticate(String user, String password){
		ActiveDirectoryLdapAuthenticationProvider ad = new ActiveDirectoryLdapAuthenticationProvider(ActiveDirectoryPlugin.domain,ActiveDirectoryPlugin.serverAddress);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(this.user,this.password);
		Authentication a = null;
		try{
			a = ad.authenticate(auth);
		}catch(BadCredentialsException e){
			Logger.debug(e.toString());
			return false;
		}
		
		return a.isAuthenticated();
	}

}
