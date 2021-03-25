package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class ValidationMessage {

	@SerializedName("userAccount")
	private String userAccount;

	@SerializedName("username")
	private String username;

	public void setUserAccount(String userAccount){
		this.userAccount = userAccount;
	}

	public String getUserAccount(){
		return userAccount;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return username;
	}

	@Override
 	public String toString(){
		return 
			"Message{" + 
			"userAccount = '" + userAccount + '\'' + 
			",username = '" + username + '\'' +
			"}";
		}
}