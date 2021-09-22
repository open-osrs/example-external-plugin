package com.tha23rd.eventCollector.events;

import java.util.Date;

public class LevelUp extends Timeable
{
	int to_level;
	String skill_name;

	public LevelUp(int toLevel, String skillName, Date timestamp)
	{
		super(timestamp);
		this.to_level = toLevel;
		this.skill_name = skillName;
	}
}
