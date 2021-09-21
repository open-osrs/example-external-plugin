package com.tha23rd.eventCollector;

import com.google.inject.Provides;
import com.tha23rd.eventCollector.eventhandlers.ItemConsumedHandler;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
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

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemConsumedHandler itemConsumedHandler;

	@Provides
	EventCollectorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EventCollectorConfig.class);
	}

	@Override
	public void startUp() throws Exception
	{
		System.out.println("Starting up");
		this.eventBus.register(itemConsumedHandler);
	}
	@Override
	public void shutDown() throws Exception
	{
		this.eventBus.unregister(itemConsumedHandler);
	}
}
