package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Tournament implements Parcelable {

	@SerializedName("end_date")
	private String endDate;

	@SerializedName("description")
	private String description;

	@SerializedName("photo")
	private String photo;

	@SerializedName("title")
	private String title;

	@SerializedName("start_date")
	private String startDate;

	protected Tournament(Parcel in) {
		endDate = in.readString();
		description = in.readString();
		photo = in.readString();
		title = in.readString();
		startDate = in.readString();
	}

	public static final Creator<Tournament> CREATOR = new Creator<Tournament>() {
		@Override
		public Tournament createFromParcel(Parcel in) {
			return new Tournament(in);
		}

		@Override
		public Tournament[] newArray(int size) {
			return new Tournament[size];
		}
	};

	public void setEndDate(String endDate){
		this.endDate = endDate;
	}

	public String getEndDate(){
		return endDate;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setPhoto(String photo){
		this.photo = photo;
	}

	public String getPhoto(){
		return photo;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setStartDate(String startDate){
		this.startDate = startDate;
	}

	public String getStartDate(){
		return startDate;
	}

	@Override
 	public String toString(){
		return 
			"Tournament{" + 
			"end_date = '" + endDate + '\'' + 
			",description = '" + description + '\'' + 
			",photo = '" + photo + '\'' + 
			",title = '" + title + '\'' + 
			",start_date = '" + startDate + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(endDate);
		parcel.writeString(description);
		parcel.writeString(photo);
		parcel.writeString(title);
		parcel.writeString(startDate);
	}
}