package com.rheotv.android.data.network.models.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PlayersListItem implements Parcelable {

	@SerializedName("score")
	private int score;

	@SerializedName("is_alive")
	private boolean isAlive;

	@SerializedName("team")
	private String team;

	@SerializedName("player")
	private Player player;

	protected PlayersListItem(Parcel in) {
		score = in.readInt();
		isAlive = in.readByte() != 0;
		team = in.readString();
		player = in.readParcelable(Player.class.getClassLoader());
	}

	public static final Creator<PlayersListItem> CREATOR = new Creator<PlayersListItem>() {
		@Override
		public PlayersListItem createFromParcel(Parcel in) {
			return new PlayersListItem(in);
		}

		@Override
		public PlayersListItem[] newArray(int size) {
			return new PlayersListItem[size];
		}
	};

	public void setScore(int score){
		this.score = score;
	}

	public int getScore(){
		return score;
	}

	public String getKills(){
		return score + "";
	}

	public void setIsAlive(boolean isAlive){
		this.isAlive = isAlive;
	}

	public boolean isIsAlive(){
		return isAlive;
	}

	public boolean isDead(){
		return !isAlive;
	}

	public void setTeam(String team){
		this.team = team;
	}

	public String getTeam(){
		return team;
	}

	public void setPlayer(Player player){
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}

	@Override
 	public String toString(){
		return 
			"PlayersListItem{" + 
			"score = '" + score + '\'' + 
			",is_alive = '" + isAlive + '\'' + 
			",team = '" + team + '\'' + 
			",player = '" + player + '\'' + 
			"}";
		}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(score);
		parcel.writeByte((byte) (isAlive ? 1 : 0));
		parcel.writeString(team);
		parcel.writeParcelable(player, i);
	}
}