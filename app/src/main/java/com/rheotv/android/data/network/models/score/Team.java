package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Team implements Parcelable {

	@SerializedName("name")
	private String name;

	@SerializedName("logo")
	private String logo;

	@SerializedName("description")
	private String description;

	protected Team(Parcel in) {
		name = in.readString();
		logo = in.readString();
		description = in.readString();
	}

	public static final Creator<Team> CREATOR = new Creator<Team>() {
		@Override
		public Team createFromParcel(Parcel in) {
			return new Team(in);
		}

		@Override
		public Team[] newArray(int size) {
			return new Team[size];
		}
	};

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setLogo(String logo){
		this.logo = logo;
	}

	public String getLogo(){
		return logo;
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
			"Team{" + 
			"name = '" + name + '\'' + 
			",logo = '" + logo + '\'' + 
			",description = '" + description + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(name);
		parcel.writeString(logo);
		parcel.writeString(description);
	}
}