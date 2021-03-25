package com.rheotv.android.data.network.models.sportsScore;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Match implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("tournament")
    @Expose
    private String tournament;
    @SerializedName("venue")
    @Expose
    private String venue;
    @SerializedName("team1")
    @Expose
    private Team team1;
    @SerializedName("team1_score")
    @Expose
    private String team1Score;
    @SerializedName("team2")
    @Expose
    private Team team2;
    @SerializedName("team2_score")
    @Expose
    private String team2Score;
    @SerializedName("commentary")
    @Expose
    private String commentary;
    @SerializedName("is_live")
    @Expose
    private Boolean isLive;
    @SerializedName("role")
    @Expose
    private Integer role;

    protected Match(Parcel in) {
        id = in.readString();
        name = in.readString();
        tournament = in.readString();
        venue = in.readString();
        team1Score = in.readString();
        team2Score = in.readString();
        commentary = in.readString();
        byte tmpIsLive = in.readByte();
        isLive = tmpIsLive == 0 ? null : tmpIsLive == 1;
        if (in.readByte() == 0) {
            role = null;
        } else {
            role = in.readInt();
        }
    }

    public static final Creator<Match> CREATOR = new Creator<Match>() {
        @Override
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        @Override
        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTournament() {
        return tournament;
    }

    public void setTournament(String tournament) {
        this.tournament = tournament;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public String getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(String team1Score) {
        this.team1Score = team1Score;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public String getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(String team2Score) {
        this.team2Score = team2Score;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public Boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(tournament);
        parcel.writeString(venue);
        parcel.writeString(team1Score);
        parcel.writeString(team2Score);
        parcel.writeString(commentary);
        parcel.writeByte((byte) (isLive == null ? 0 : isLive ? 1 : 2));
        if (role == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(role);
        }
    }
}