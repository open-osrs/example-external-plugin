package com.tha23rd.eventCollector.events;

public class LootDropped
{
	int itemId;
	String itemName;
	int price;
	boolean pet;

	public LootDropped(int itemId, String itemName, int price, boolean pet)
	{
		this.itemId = itemId;
		this.price = price;
		this.itemName = itemName;
		this.pet = pet;
	}
}
