package com.tha23rd.eventCollector;

import com.google.inject.Provides;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.eventhandlers.ItemConsumedHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
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

	private boolean heartbeatLoop = true;

	private boolean loggedIn = false;

	@Provides
	EventCollectorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EventCollectorConfig.class);
	}

	private void sendHeartBeat(int heartBeatIntervalMs) {
		Executor executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			while (heartbeatLoop) {
				if (loggedIn && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null && client.getLocalPlayer().getName().equalsIgnoreCase(config.playerName())) {
					System.out.println("Sending heartbeat");
					RsServiceClient.getClient(this.config.apiurl()).heartbeat(config.playerId());
					try
					{
						Thread.sleep(heartBeatIntervalMs);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				} else {
					// sleep for a shorter time
					try
					{
						Thread.sleep(1000 * 10);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

			}
		});
	}

	@Override
	public void startUp() throws Exception
	{
		System.out.println("Starting up");
		// send heartbeat every 4 seconds
		sendHeartBeat(1000 * 60 * 4);
		this.eventBus.register(itemConsumedHandler);
	}
	@Override
	public void shutDown() throws Exception
	{
		this.eventBus.unregister(itemConsumedHandler);
		this.heartbeatLoop = false;
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event)
	{
		System.out.println(client.getLocalPlayer().getName());
		System.out.println(event.getGameState().name());

		if (event.getGameState() == GameState.LOGGED_IN)
		{
			loggedIn = true;
		}

		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			loggedIn = false;
		}
	}

}
