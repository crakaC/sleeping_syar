package com.crakac.ofuton.util;

public class TwitterList {
	private long userId;
	private long listId;
	private String name;
	private String fullName;
	
	public TwitterList(long uid, long l, String name, String fname){
		userId = uid;
		listId = l;
		this.name = name;
		fullName = fname;
	}

	public long getUserId() {
		return userId;
	}

	public long getListId() {
		return listId;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}
	
	
}
