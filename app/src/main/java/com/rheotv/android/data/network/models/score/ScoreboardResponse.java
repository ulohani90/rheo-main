package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScoreboardResponse implements Parcelable {

	@SerializedName("start_time")
	private String startTime;

	@SerializedName("post_id")
	private String postId;

	@SerializedName("current_status_remark")
	private Object currentStatusRemark;

	@SerializedName("description")
	private String description;

	@SerializedName("logo")
	private String logo;

	@SerializedName("score_unit")
	private String scoreUnit;

	@SerializedName("tournament")
	private Tournament tournament;

	@SerializedName("title")
	private String title;

	@SerializedName("teams_list")
	private List<TeamsListItem> teamsList;

	protected ScoreboardResponse(Parcel in) {
		startTime = in.readString();
		postId = in.readString();
		description = in.readString();
		logo = in.readString();
		scoreUnit = in.readString();
		tournament = in.readParcelable(Tournament.class.getClassLoader());
		title = in.readString();
		teamsList = in.createTypedArrayList(TeamsListItem.CREATOR);
	}

	public static final Creator<ScoreboardResponse> CREATOR = new Creator<ScoreboardResponse>() {
		@Override
		public ScoreboardResponse createFromParcel(Parcel in) {
			return new ScoreboardResponse(in);
		}

		@Override
		public ScoreboardResponse[] newArray(int size) {
			return new ScoreboardResponse[size];
		}
	};

	public void setStartTime(String startTime){
		this.startTime = startTime;
	}

	public String getStartTime(){
		return startTime;
	}

	public void setPostId(String postId){
		this.postId = postId;
	}

	public String getPostId(){
		return postId;
	}

	public void setCurrentStatusRemark(Object currentStatusRemark){
		this.currentStatusRemark = currentStatusRemark;
	}

	public Object getCurrentStatusRemark(){
		return currentStatusRemark;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setLogo(String logo){
		this.logo = logo;
	}

	public String getLogo(){
		return logo;
	}

	public void setScoreUnit(String scoreUnit){
		this.scoreUnit = scoreUnit;
	}

	public String getScoreUnit(){
		return scoreUnit;
	}

	public void setTournament(Tournament tournament){
		this.tournament = tournament;
	}

	public Tournament getTournament(){
		return tournament;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setTeamsList(List<TeamsListItem> teamsList){
		this.teamsList = teamsList;
	}

	public List<TeamsListItem> getTeamsList(){
		return teamsList;
	}

	@Override
 	public String toString(){
		return 
			"ScoreboardResponse{" + 
			"start_time = '" + startTime + '\'' + 
			",post_id = '" + postId + '\'' + 
			",current_status_remark = '" + currentStatusRemark + '\'' + 
			",description = '" + description + '\'' + 
			",logo = '" + logo + '\'' + 
			",score_unit = '" + scoreUnit + '\'' + 
			",tournament = '" + tournament + '\'' + 
			",title = '" + title + '\'' + 
			",teams_list = '" + teamsList + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(startTime);
		parcel.writeString(postId);
		parcel.writeString(description);
		parcel.writeString(logo);
		parcel.writeString(scoreUnit);
		parcel.writeParcelable(tournament, i);
		parcel.writeString(title);
		parcel.writeTypedList(teamsList);
	}
}