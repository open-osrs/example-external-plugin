package com.tha23rd.eventCollector;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.ItemConsumed;
import com.tha23rd.eventCollector.events.RsEvent;
import java.util.Date;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(
	name = "Event Collector",
	description = "Collect all the events",
	tags = {"utility", "data", "collection"}
)
@Extension
public class EventCollectorPlugin extends Plugin
{
	@Inject
	private EventCollectorConfig config;

	@Inject
	private Client client;

	@Provides
	EventCollectorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EventCollectorConfig.class);
	}

	@Override
	public void startUp() throws Exception
	{
		ItemConsumed itemConsumed = new ItemConsumed(435, new Date());
		RsEvent<ItemConsumed> rsEvent = new RsEvent<>("itemConsumed", getPlayerId(), itemConsumed);
		Gson gson = new Gson();
		RsServiceClient.getClient(getApiUrl()).postEvent(gson.toJson(rsEvent));
	}

	private String getApiUrl()
	{
		return config.apiurl();
	}

	private String getPlayerId()
	{
		return config.playerId();
	}
}
