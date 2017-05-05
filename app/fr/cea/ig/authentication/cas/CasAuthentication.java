package fr.cea.ig.authentication.cas;
/**											Play Authentication system for Central Authentication Service(CAS)
 *
 *Use with :  @With(CasAuthentication.class)
 *
 *This class is dedicated to CAS, it redirect users to login page if they're not logged in
 *verify the validity of tikets and keep alive session with play session
 *
 */

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;
import play.api.modules.spring.Spring;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import fr.cea.ig.authentication.Constants;
import fr.cea.ig.authentication.SessionHelper;
import fr.cea.ig.authentication.html.IAuthenticate;

public class CasAuthentication extends Action.Simple{
	private Action actionDelegate = null;
	
	public CasAuthentication(){
	}
	
	public CasAuthentication(Action delegate){
		this.actionDelegate = delegate;
	}
	
	@Override
	//function called by play
	public  F.Promise<Result> call(Http.Context context) throws Throwable {
		if(delegate != null){
			this.actionDelegate = delegate;
		}
		
		if(CasPlugin.loadOk) {
			final String service = context.request().host()+context.request().uri();
			final String uri = context.request().uri();
			if(CasPlugin.mode.equals(Constants.MODE_DEFAULT) && (context.session().isEmpty() == true || context.session().get(SessionHelper.USER) == null)) {
				String ticket = "";
				
				if(!(getParameters(context.request().uri(),"ticket").equals(""))){
					ticket = getParameters(context.request().uri(),"ticket");
				}

				if(ticket == null || ticket.equals("")){
					return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								      return redirect(urlToCas(cleanUrlTicket(service))+"&renew=true");
								    }
								  }
							 );
				}

				WSRequestHolder holder =  WS.url(CasPlugin.validator);
				holder.setQueryParameter("service", "http://"+cleanUrlTicket(service));
				holder.setQueryParameter("ticket",ticket);
				Promise<WSResponse> validatorResp = holder.get();
				IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
				if(a.isUserAccessApplication(getId(validatorResp.get(10, TimeUnit.SECONDS).getBody()), CasPlugin.applicationCode)){	
					if(!isTicketAlive(validatorResp.get(10, TimeUnit.SECONDS).getBody())){
						return Promise.promise(
							 new Function0<Result>() {
								    public Result apply() {
								      return redirect(urlToCas(cleanUrlTicket(service)));
								    }
								  }
							 );
					} else {
						SessionHelper.createCookie(context, getId(validatorResp.get(10, TimeUnit.SECONDS).getBody()));
						SessionHelper.setDefaultRole(getId(validatorResp.get(10, TimeUnit.SECONDS).getBody()),CasPlugin.role);
						if(!cleanUrlTicket(service).equals(service)){
							return Promise.promise(
									 new Function0<Result>() {
										    public Result apply() {
										    	return redirect(cleanUrlTicket(uri));
										    }
									 }
							);
						}else{
							return this.actionDelegate.call(context);
						}
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
			} else if(CasPlugin.mode.equals(Constants.MODE_DEFAULT) && SessionHelper.timeOutSession(context,CasPlugin.timeOut)) {
				context.session().clear();
				return Promise.promise(
						 new Function0<Result>() {
							    public Result apply() {
							      return redirect(urlToCas(cleanUrlTicket(service))+"&renew=true");
							    }
							  }
						 );
			} else if(!CasPlugin.mode.equals(Constants.MODE_DEFAULT)) {
				SessionHelper.createCookie(context, "ngsrg");
				return this.actionDelegate.call(context);
			} else if((context.session().get(SessionHelper.USER) != null)) {
				IAuthenticate a = Spring.getBeanOfType(IAuthenticate.class);
				if(a.isUserAccessApplication(context.session().get(SessionHelper.USER).toLowerCase(), CasPlugin.applicationCode)){	
					SessionHelper.setDefaultRole(context.session().get(SessionHelper.USER).toLowerCase(),CasPlugin.role);
					context.request().setUsername(context.session().get(SessionHelper.USER).toLowerCase());
					return this.actionDelegate.call(context);
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
								     return internalServerError(CasPlugin.errorMessage);
								    }
								  }
							 );
	}

	
	//Return the url for the login with the service
	private String urlToCas(String service) {
		String loginUrl ="";
		try{
			loginUrl = CasPlugin.login+"?service=http://"+java.net.URLEncoder.encode(service, "UTF-8");
		}catch(java.io.UnsupportedEncodingException e){}
		if(CasPlugin.renew)
			loginUrl += "&renew=true";
		return loginUrl;
	}

	//Return a parameter in url
	private String getParameters(String url,String param) {
		String[] tmp = url.split(param+"=");
		if(tmp.length > 1) {
			tmp = tmp[1].split("&");
			return tmp[0];
		}

		return "";
	}

	//Return the url without the parameter
	private String cleanUrl(String url) {
		String[] tmp = url.split("\\?");
		return tmp[0];
	}

	//Return the url without the parameter ticket
	private String cleanUrlTicket(String url) {
		String urlClean = url.replaceAll("\\?ticket=.+&", "\\?");
		urlClean = urlClean.replaceAll("\\?ticket=.+", "");
		urlClean = urlClean.replaceAll("&ticket=.+", "");
		return urlClean;
	}

	//Return true if the tiket is a valid ticket
	private boolean isTicketAlive(String body) {
		return body.contains("authenticationSuccess");
	}

	//Return the Id of user in the confirmation xml
	private String getId(String body) {
		Pattern p = Pattern .compile("<cas:user>(.+)</cas:user>");
		Matcher m = p.matcher(body);
		if(m.find()){
			return m.group(1);
		}
		
		Logger.error("No cas user in the response: "+body);
		
		return "";
	}
}