package com.tha23rd.customOneClick;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("customOneClick")
public interface CustomOneClickConfig extends Config
{
	@ConfigItem(
		keyName = "fishingspot",
		name = "Fishing Spot",
		description = "Clicks on the closest fishing spot to you when you click your fishing pole"
	)
	default boolean getFishingSpot()
	{
		return false;
	}
}
