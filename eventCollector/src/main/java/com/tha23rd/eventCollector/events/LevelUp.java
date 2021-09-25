package com.tha23rd.eventCollector.events;

public class LevelUp
{
	int to_level;
	String skill_name;

	public LevelUp(int toLevel, String skillName)
	{
		this.to_level = toLevel;
		this.skill_name = skillName;
	}
}
