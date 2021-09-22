package com.tha23rd.eventCollector;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("discordNotifier")
public interface EventCollectorConfig extends Config
{
	@ConfigItem(
		keyName = "apiurl",
		name = "API URL",
		description = "The base API URL to send events to."
	)
	default String apiurl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "playerid",
		name = "Player Id",
		description = "The player ID associated with events."
	)
	default String playerId()
	{
		return "";
	}

	@ConfigItem(
		keyName = "playername",
		name = "Player Name",
		description = "The player name associated with events."
	)
	default String playerName()
	{
		return "";
	}
}
