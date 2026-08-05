package com.crolclient.buyer.data;

public class BuyerItem {
	public final String name;
	public final String icon;
	public final double price;
	public final int maxStack;
	
	public BuyerItem(String name, String icon, double price, int maxStack) {
		this.name = name;
		this.icon = icon;
		this.price = price;
		this.maxStack = maxStack;
	}
}
