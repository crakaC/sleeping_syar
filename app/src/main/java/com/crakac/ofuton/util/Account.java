package com.crakac.ofuton.util;

public class Account {
	private long userId;
	private String screenName;
	private String iconUrl;
	private String token;
	private String tokenSecret;
	private boolean isCurrent;
	
	public Account(long id, String name, String url, String token, String secret, boolean current){
		userId = id;
		screenName = name;
		iconUrl = url;
		this.token = token;
		tokenSecret = secret;
		isCurrent = current;
	}
	public long getUserId() {
		return userId;
	}
	public String getScreenName() {
		return screenName;
	}
	public String getIconUrl() {
		return iconUrl;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public boolean IsCurrent() {
		return isCurrent;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Account && userId == ((Account)other).userId;
	}
}
