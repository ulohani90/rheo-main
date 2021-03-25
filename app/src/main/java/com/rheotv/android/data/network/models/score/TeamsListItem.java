package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TeamsListItem implements Parcelable {

	@SerializedName("score")
	private int score;

	@SerializedName("match")
	private String match;

	@SerializedName("final_rank")
	private int finalRank;

	@SerializedName("team")
	private Team team;

	@SerializedName("players_list")
	private List<PlayersListItem> playersList;

	protected TeamsListItem(Parcel in) {
		score = in.readInt();
		match = in.readString();
		finalRank = in.readInt();
		playersList = in.createTypedArrayList(PlayersListItem.CREATOR);
	}

	public static final Creator<TeamsListItem> CREATOR = new Creator<TeamsListItem>() {
		@Override
		public TeamsListItem createFromParcel(Parcel in) {
			return new TeamsListItem(in);
		}

		@Override
		public TeamsListItem[] newArray(int size) {
			return new TeamsListItem[size];
		}
	};

	public void setScore(int score){
		this.score = score;
	}

	public int getScore(){
		return score;
	}

	public void setMatch(String match){
		this.match = match;
	}

	public String getMatch(){
		return match;
	}

	public void setFinalRank(int finalRank){
		this.finalRank = finalRank;
	}

	public int getFinalRank(){
		return finalRank;
	}

	public void setTeam(Team team){
		this.team = team;
	}

	public Team getTeam(){
		return team;
	}

	public void setPlayersList(List<PlayersListItem> playersList){
		this.playersList = playersList;
	}

	public List<PlayersListItem> getPlayersList(){
		return playersList;
	}

	@Override
 	public String toString(){
		return 
			"TeamsListItem{" + 
			"score = '" + score + '\'' + 
			",match = '" + match + '\'' + 
			",final_rank = '" + finalRank + '\'' + 
			",team = '" + team + '\'' + 
			",players_list = '" + playersList + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(score);
		parcel.writeString(match);
		parcel.writeInt(finalRank);
		parcel.writeTypedList(playersList);
	}
}