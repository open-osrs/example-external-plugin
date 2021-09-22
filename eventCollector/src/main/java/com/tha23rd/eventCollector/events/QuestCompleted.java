package com.tha23rd.eventCollector.events;

import java.util.Date;

public class QuestCompleted extends Timeable
{

	String quest_name;

	public QuestCompleted(String questName, Date timestamp)
	{
		super(timestamp);
		this.quest_name = questName;
	}
}
