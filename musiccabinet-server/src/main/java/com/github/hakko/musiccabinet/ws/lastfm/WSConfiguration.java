package com.github.hakko.musiccabinet.ws.lastfm;

public class WSConfiguration {

	private boolean authenticated;
	private boolean logInvocation;
	private int callAttempts;
	private int sleepTime;
	
	public static final WSConfiguration UNAUTHENTICATED_LOGGED = new WSConfiguration(false, true);
	public static final WSConfiguration UNAUTHENTICATED_UNLOGGED = new WSConfiguration(false, false);

	public static final WSConfiguration AUTHENTICATED_LOGGED = new WSConfiguration(true, true);
	public static final WSConfiguration AUTHENTICATED_UNLOGGED = new WSConfiguration(true, false);

	public static final WSConfiguration UNAUTHENTICATED_LOGGED_TEST = new WSConfiguration(false, true, 3, 0);
	public static final WSConfiguration AUTHENTICATED_UNLOGGED_TEST = new WSConfiguration(true, false, 3, 0);

	public WSConfiguration(boolean authenticated, boolean logInvocation) {
		this.authenticated = authenticated;
		this.logInvocation = logInvocation;
		this.callAttempts = 3;
		this.sleepTime = 1000 * 60 * 5;
	}
	
	public WSConfiguration(boolean authenticated, boolean logInvocation, int callAttempts, int sleepTime) {
		this.authenticated = authenticated;
		this.logInvocation = logInvocation;
		this.callAttempts = callAttempts;
		this.sleepTime = sleepTime;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isLogInvocation() {
		return logInvocation;
	}

	public void setLogInvocation(boolean logInvocation) {
		this.logInvocation = logInvocation;
	}

	public int getCallAttempts() {
		return callAttempts;
	}

	public void setCallAttempts(int callAttempts) {
		this.callAttempts = callAttempts;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	
}