package com.rheotv.android.data.network.models.play;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class FromUserProfile implements Parcelable {

	@SerializedName("is_top_streamer")
	private boolean isTopStreamer;

	@SerializedName("cover_pic")
	private String coverPic;

	@SerializedName("followers_count")
	private int followersCount;

	@SerializedName("profile_pic")
	private String profilePic;

	@SerializedName("bio")
	private Object bio;

	@SerializedName("total_views")
	private int totalViews;

	@SerializedName("id")
	private String id;

	@SerializedName("user")
	private User user;

	@SerializedName("is_verified")
	private boolean isVerified;

	@SerializedName("is_prime")
	private boolean isPrime;

	@SerializedName("game_username")
	private String gameUserName;

	public FromUserProfile(){}
	protected FromUserProfile(Parcel in) {
		isTopStreamer = in.readByte() != 0;
		coverPic = in.readString();
		followersCount = in.readInt();
		profilePic = in.readString();
		totalViews = in.readInt();
		id = in.readString();
		isVerified = in.readByte() != 0;
		isPrime = in.readByte() != 0;
		gameUserName = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (isTopStreamer ? 1 : 0));
		dest.writeString(coverPic);
		dest.writeInt(followersCount);
		dest.writeString(profilePic);
		dest.writeInt(totalViews);
		dest.writeString(id);
		dest.writeByte((byte) (isVerified ? 1 : 0));
		dest.writeByte((byte) (isPrime ? 1 : 0));
		dest.writeString(gameUserName);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<FromUserProfile> CREATOR = new Creator<FromUserProfile>() {
		@Override
		public FromUserProfile createFromParcel(Parcel in) {
			return new FromUserProfile(in);
		}

		@Override
		public FromUserProfile[] newArray(int size) {
			return new FromUserProfile[size];
		}
	};

	public void setIsTopStreamer(boolean isTopStreamer){
		this.isTopStreamer = isTopStreamer;
	}

	public boolean isIsTopStreamer(){
		return isTopStreamer;
	}

	public void setCoverPic(String coverPic){
		this.coverPic = coverPic;
	}

	public String getCoverPic(){
		return coverPic;
	}

	public void setFollowersCount(int followersCount){
		this.followersCount = followersCount;
	}

	public int getFollowersCount(){
		return followersCount;
	}

	public void setProfilePic(String profilePic){
		this.profilePic = profilePic;
	}

	public String getProfilePic(){
		return profilePic;
	}

	public void setBio(Object bio){
		this.bio = bio;
	}

	public Object getBio(){
		return bio;
	}

	public void setTotalViews(int totalViews){
		this.totalViews = totalViews;
	}

	public int getTotalViews(){
		return totalViews;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setUser(User user){
		this.user = user;
	}

	public User getUser(){
		return user;
	}

	public void setIsVerified(boolean isVerified){
		this.isVerified = isVerified;
	}

	public boolean isIsVerified(){
		return isVerified;
	}

	public void setIsPrime(boolean isPrime){
		this.isPrime = isPrime;
	}

	public boolean isIsPrime(){
		return isPrime;
	}

	public String getGameUserName() {
		return gameUserName;
	}

	public void setGameUserName(boolean String) {
		this.gameUserName = gameUserName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FromUserProfile)) return false;
		FromUserProfile that = (FromUserProfile) o;
		return isTopStreamer == that.isTopStreamer &&
				getFollowersCount() == that.getFollowersCount() &&
				getTotalViews() == that.getTotalViews() &&
				isVerified == that.isVerified &&
				isPrime == that.isPrime &&
				Objects.equals(getCoverPic(), that.getCoverPic()) &&
				Objects.equals(getProfilePic(), that.getProfilePic()) &&
				Objects.equals(getBio(), that.getBio()) &&
				Objects.equals(getId(), that.getId()) &&
				Objects.equals(getUser(), that.getUser()) &&
				Objects.equals(getGameUserName(), that.getGameUserName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(isTopStreamer, getCoverPic(), getFollowersCount(), getProfilePic(), getBio(), getTotalViews(), getId(), getUser(), isVerified, isPrime, getGameUserName());
	}

	@Override
 	public String toString(){
		return 
			"FromUserProfile{" + 
			"is_top_streamer = '" + isTopStreamer + '\'' + 
			",cover_pic = '" + coverPic + '\'' + 
			",followers_count = '" + followersCount + '\'' + 
			",profile_pic = '" + profilePic + '\'' + 
			",bio = '" + bio + '\'' + 
			",total_views = '" + totalViews + '\'' + 
			",id = '" + id + '\'' + 
			",user = '" + user + '\'' + 
			",is_verified = '" + isVerified + '\'' + 
			",is_prime = '" + isPrime + '\'' + 
			"}";
		}
}