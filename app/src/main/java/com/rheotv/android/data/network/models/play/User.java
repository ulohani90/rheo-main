package com.rheotv.android.data.network.models.play;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class User{

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("id")
	private int id;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("username")
	private String username;

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return username;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return getId() == user.getId() &&
				Objects.equals(getLastName(), user.getLastName()) &&
				Objects.equals(getFirstName(), user.getFirstName()) &&
				Objects.equals(getUsername(), user.getUsername());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getLastName(), getId(), getFirstName(), getUsername());
	}

	@Override
 	public String toString(){
		return 
			"User{" + 
			"last_name = '" + lastName + '\'' + 
			",id = '" + id + '\'' + 
			",first_name = '" + firstName + '\'' + 
			",username = '" + username + '\'' +
			"}";
		}
}