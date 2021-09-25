package com.tha23rd.eventCollector.events;

import net.runelite.api.coords.WorldPoint;

public class PlayerDeath
{
	int x;
	int y;
	int plane;

	public PlayerDeath(WorldPoint worldPoint)
	{
		x = worldPoint.getX();
		y = worldPoint.getY();
		plane = worldPoint.getPlane();
	}
}
