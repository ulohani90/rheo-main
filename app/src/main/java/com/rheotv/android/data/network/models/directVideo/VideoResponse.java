package com.rheotv.android.data.network.models.directVideo;

import java.util.List;

public class VideoResponse{
	private int count;
	private List<VideoResult> results;

	public void setCount(int count){
		this.count = count;
	}

	public int getCount(){
		return count;
	}

	public void setResults(List<VideoResult> results){
		this.results = results;
	}

	public List<VideoResult> getResults(){
		return results;
	}

	@Override
 	public String toString(){
		return 
			"VideoResponse{" + 
			"count = '" + count + '\'' + 
			",results = '" + results + '\'' + 
			"}";
		}
}