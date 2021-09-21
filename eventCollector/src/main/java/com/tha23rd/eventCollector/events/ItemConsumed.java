package com.tha23rd.eventCollector.events;

import java.util.Date;


public class ItemConsumed extends Timeable
{
	int item_id;

	public ItemConsumed(int item_id, Date timestamp)
	{
		super(timestamp);
		this.item_id = item_id;
	}
}
