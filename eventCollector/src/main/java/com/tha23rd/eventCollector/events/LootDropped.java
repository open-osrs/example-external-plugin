package com.tha23rd.eventCollector.events;

import java.util.Date;

public class LootDropped extends Timeable
{
	int itemId;
	String itemName;
	int price;
	boolean pet;

	public LootDropped(int itemId, String itemName, int price, boolean pet, Date timestamp)
	{
		super(timestamp);
		this.itemId = itemId;
		this.price = price;
		this.itemName = itemName;
		this.pet = pet;
	}
}
