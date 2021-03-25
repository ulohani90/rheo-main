package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Player implements Parcelable {

	@SerializedName("profile_photo")
	private String profilePhoto;

	@SerializedName("name")
	private String name;

	@SerializedName("description")
	private String description;

	protected Player(Parcel in) {
		profilePhoto = in.readString();
		name = in.readString();
		description = in.readString();
	}

	public static final Creator<Player> CREATOR = new Creator<Player>() {
		@Override
		public Player createFromParcel(Parcel in) {
			return new Player(in);
		}

		@Override
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

	public void setProfilePhoto(String profilePhoto){
		this.profilePhoto = profilePhoto;
	}

	public String getProfilePhoto(){
		return profilePhoto;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	@Override
 	public String toString(){
		return 
			"Player{" + 
			"profile_photo = '" + profilePhoto + '\'' + 
			",name = '" + name + '\'' + 
			",description = '" + description + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(profilePhoto);
		parcel.writeString(name);
		parcel.writeString(description);
	}
}