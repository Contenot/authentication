package fr.cea.ig.authentication.html;


import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import fr.cea.ig.authentication.SessionHelper;
import fr.cea.ig.authentication.activedirectory.ActiveDirectoryPlugin;
import fr.cea.ig.authentication.cas.CasPlugin;
import fr.cea.ig.authentication.html.IAuthenticate;


import play.Logger;
import play.api.modules.spring.Spring;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;



public class HtmlAuthentication extends Action.Simple{

	private Action actionDelegate = null;
	private String user, password = "";
	
	public HtmlAuthentication(){
		this.user = "";
		this.password = "";
	}
	
	public HtmlAuthentication(Action delegate){
		this.actionDelegate = delegate;
		this.user = "";
		this.password = "";
	}
	
	public HtmlAuthentication(String user, String password){
		this.user = user;
		this.password = password;
	}
	
	
	public  F.Promise<Result> call(final play.mvc.Http.Context context) throws Throwable {	
		if(delegate != null){
			this.actionDelegate = delegate;
		}
		
		
		if((context.request().uri().equals(controllers.routes.Authentication.authentication().url()) && (context.session().get(SessionHelper.USER) == null)) || (context.request().uri().equals(controllers.routes.Authentication.verification().url()) && this.user.equals("") && this.password.equals(""))){
			return this.actionDelegate.call(context);
		}else if(!HtmlPlugin.mode.equals("prod")){
			SessionHelper.createCookie(context, "ngsrg");
			return this.actionDelegate.call(context);
		}else{
			if(context.session().isEmpty() == true || context.session().get(SessionHelper.USER) == null){
				IAuthenticate authenticate = Spring.getBeanOfType(IAuthenticate.class);
				if (authenticate(this.user, this.password)) {
					SessionHelper.createCookie(context, this.user);
					Logger.debug("AUTH OK");
					if (authenticate.isUserAccessApplication(this.user,	HtmlPlugin.applicationCode)) {
						SessionHelper.setDefaultRole(this.user, HtmlPlugin.role);
						context.request().setUsername(this.user);
						if (context.session().get("REFER") != null) {
							return Promise.promise(new Function0<Result>() {
								public Result apply() {
									return redirect(context.session().get(
											"REFER"));
								}
							});

						}else{
							return Promise.promise(
									 new Function0<Result>() {
										    public Result apply() {
										     return redirect("/");
										    }
										  }
									 );
						}

					} else {
						return Promise.promise(new Function0<Result>() {
							public Result apply() {
								return unauthorized();
							}
						});
					}
				} else {
					setRefer(context);
					Logger.debug("AUTH FAIL");
					return Promise.promise(new Function0<Result>() {
						public Result apply() {
							return redirect(controllers.routes.Authentication
									.authentication().url());
						}
					});
				}
				
			}else if(SessionHelper.timeOutSession(context, HtmlPlugin.timeOut)){
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
			} else if ((context.session().get(SessionHelper.USER) != null)) {
				IAuthenticate c = Spring.getBeanOfType(IAuthenticate.class);
				if (c.isUserAccessApplication(
						context.session().get(SessionHelper.USER).toLowerCase(),
						HtmlPlugin.applicationCode)) {
					SessionHelper.setDefaultRole(context.session().get(SessionHelper.USER).toLowerCase(), HtmlPlugin.role);
					context.request().setUsername(context.session().get(SessionHelper.USER).toLowerCase());
					if (this.actionDelegate != null) {
						return this.actionDelegate.call(context);
					} else {
						redirect("/");
					}
				} else {
					return Promise.promise(new Function0<Result>() {
						public Result apply() {
							return unauthorized();
						}
					});
				}
			}
		}
		return Promise.promise(new Function0<Result>() {
			public Result apply() {
				return internalServerError();
			}
		});
	}

	private void setRefer(Context context) {
		if (!context
				.request()
				.uri()
				.equals(controllers.routes.Authentication.authentication()
						.url())
				&& !context
						.request()
						.uri()
						.equals(controllers.routes.Authentication
								.verification().url())) {
			context.session().put("REFER", context.request().uri());
		}
	}

	private boolean authenticate(String user, String password) {
		if(StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)){
			IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
			
			//si l'utilisateur n'est pas actif, on n'autorise pas l'authentification
            if (!a.isUserActive(user)){
                    Logger.debug("L'utilisateur " + user + " n'est pas actif, on refuse l'authentification");
                    return false;
            }
			
			String  passwordInDB =  a.getUserPassword(user) ;
			if((passwordInDB !=  null) && ( a.getUserPassword(user) != null)){
				return BCrypt.checkpw(password,passwordInDB);
			}
		}
		return false;		
	}

	

}
