package com.rheotv.android.data.network.models.gamify;

import com.google.gson.annotations.SerializedName;

public class ValidationResult {

	@SerializedName("orderId")
	private String orderId;

	@SerializedName("message")
	private ValidationMessage message;

	@SerializedName("sku")
	private SkusItem sku;

	public void setOrderId(String orderId){
		this.orderId = orderId;
	}

	public String getOrderId(){
		return orderId;
	}

	public void setMessage(ValidationMessage message){
		this.message = message;
	}

	public ValidationMessage getMessage(){
		return message;
	}

	public void setSku(SkusItem sku){
		this.sku = sku;
	}

	public SkusItem getSku(){
		return sku;
	}

	@Override
 	public String toString(){
		return 
			"Result{" + 
			"orderId = '" + orderId + '\'' + 
			",message = '" + message + '\'' + 
			",sku = '" + sku + '\'' + 
			"}";
		}
}