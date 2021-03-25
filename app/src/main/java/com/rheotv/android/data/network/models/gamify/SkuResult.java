package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SkuResult {

	@SerializedName("skuList")
	private List<SkusItem> skus;

	public void setSkus(List<SkusItem> skus){
		this.skus = skus;
	}

	public List<SkusItem> getSkus(){
		return skus;
	}

	@Override
 	public String toString(){
		return 
			"Result{" + 
			"skus = '" + skus + '\'' + 
			"}";
		}
}