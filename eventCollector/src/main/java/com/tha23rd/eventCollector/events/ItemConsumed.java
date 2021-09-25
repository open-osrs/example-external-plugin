package com.tha23rd.eventCollector.events;

public class ItemConsumed
{
	int item_id;
	int count;
	long price;

	public ItemConsumed(long price, int item_id, int count)
	{
		this.item_id = item_id;
		this.price = price;
		this.count = count;
	}
}
