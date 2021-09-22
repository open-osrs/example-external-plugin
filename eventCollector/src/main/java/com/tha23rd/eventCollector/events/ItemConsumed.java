package com.tha23rd.eventCollector.events;

import java.util.Date;


public class ItemConsumed extends Timeable
{
	int item_id;
	long price;

	public ItemConsumed(long price, int item_id, Date timestamp)
	{
		super(timestamp);
		this.item_id = item_id;
		this.price = price;
	}
}
