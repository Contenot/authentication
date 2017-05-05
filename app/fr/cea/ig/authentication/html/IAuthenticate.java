package fr.cea.ig.authentication.html;




public  interface IAuthenticate {

	public boolean isExistUserWithLoginAndPassword(String login, String password);

	public boolean isUserAccessApplication(String login, String application);
	
	public void setDefaultRole(String login, String role);
	
	public String getUserPassword(String login);
	
	public boolean isUserActive(String login);
}
