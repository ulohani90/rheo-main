package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class SkuResponse{

	@SerializedName("result")
	private SkuResult result;

	@SerializedName("id")
	private String id;

	@SerializedName("jsonrpc")
	private String jsonrpc;

	public void setResult(SkuResult result){
		this.result = result;
	}

	public SkuResult getResult(){
		return result;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setJsonrpc(String jsonrpc){
		this.jsonrpc = jsonrpc;
	}

	public String getJsonrpc(){
		return jsonrpc;
	}

	@Override
 	public String toString(){
		return 
			"SkuResponse{" + 
			"result = '" + result + '\'' + 
			",id = '" + id + '\'' + 
			",jsonrpc = '" + jsonrpc + '\'' + 
			"}";
		}
}