package com.rheotv.android.data.network.models.directVideo;

import com.rheotv.android.data.network.models.objects.PostObject;

import java.util.List;

public class VideoResult {
	private int count;
	private int type;
	private List<PostObject> posts;

	public void setCount(int count){
		this.count = count;
	}

	public int getCount(){
		return count;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public void setPosts(List<PostObject> posts){
		this.posts = posts;
	}

	public List<PostObject> getPosts(){
		return posts;
	}

	@Override
 	public String toString(){
		return 
			"ResultsItem{" + 
			"count = '" + count + '\'' + 
			",type = '" + type + '\'' + 
			",posts = '" + posts + '\'' + 
			"}";
		}
}