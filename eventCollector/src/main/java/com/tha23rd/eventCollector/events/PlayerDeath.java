package com.tha23rd.eventCollector.events;

import java.util.Date;
import net.runelite.api.coords.WorldPoint;

public class PlayerDeath extends Timeable
{
	int x;
	int y;
	int plane;

	public PlayerDeath(Date timestamp, WorldPoint worldPoint)
	{
		super(timestamp);
		x = worldPoint.getX();
		y = worldPoint.getY();
		plane = worldPoint.getPlane();
	}
}
