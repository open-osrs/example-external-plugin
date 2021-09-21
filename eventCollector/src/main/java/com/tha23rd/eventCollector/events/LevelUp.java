package com.tha23rd.eventCollector.events;

import java.util.Date;

public class LevelUp extends Timeable
{
	int to_level;
	String skill_name;

	public LevelUp(Date timestamp)
	{
		super(timestamp);
	}
}
