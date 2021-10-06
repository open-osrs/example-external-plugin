package com.tha23rd.eventCollector;

import com.google.inject.Provides;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.eventhandlers.BankOpenedHandler;
import com.tha23rd.eventCollector.eventhandlers.EventHandler;
import com.tha23rd.eventCollector.eventhandlers.GamerLoggedHandler;
import com.tha23rd.eventCollector.eventhandlers.ItemConsumedHandler;
import com.tha23rd.eventCollector.eventhandlers.LevelUpHandler;
import com.tha23rd.eventCollector.eventhandlers.LootDroppedHandler;
import com.tha23rd.eventCollector.eventhandlers.PlayerDeathHandler;
import com.tha23rd.eventCollector.eventhandlers.QuestCompletedHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(
	name = "Event Collector",
	description = "Collect all the events",
	tags = {"utility", "data", "collection", "gimp"}
)
@Extension
@Slf4j
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

	@Inject
	private LevelUpHandler levelUpHandler;

	@Inject
	private QuestCompletedHandler questCompletedHandler;

	@Inject
	private PlayerDeathHandler playerDeathHandler;

	@Inject
	private LootDroppedHandler lootDroppedHandler;

	@Inject
	private GamerLoggedHandler gamerLoggedHandler;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private BankOpenedHandler bankOpenedHandler;

	@Inject
	private ConfigManager configManager;

	private boolean heartbeatLoop = true;

	private boolean loggedIn = false;

	private EventHandler[] eventHandlers;

	@Provides
	EventCollectorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EventCollectorConfig.class);
	}

	private void sendHeartBeat(int heartBeatIntervalMs)
	{
		Executor executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			while (heartbeatLoop)
			{
				final String playerId = getPlayerId();
				if (loggedIn && playerId != null)
				{
					sendChatMessage("Sending heartbeat");
					RsServiceClient.getClient(this.config.apiurl()).heartbeat(playerId);
					try
					{
						Thread.sleep(heartBeatIntervalMs);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// sleep for a shorter time
					try
					{
						log.info("Logged in: " + loggedIn);
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
		// send heartbeat every 4 minutes
		sendHeartBeat(1000 * 60 * 4);
		this.eventHandlers = new EventHandler[]{itemConsumedHandler, levelUpHandler, questCompletedHandler,
			playerDeathHandler, lootDroppedHandler, gamerLoggedHandler, bankOpenedHandler};

		for (EventHandler eventHandler : eventHandlers)
		{
			eventBus.register(eventHandler);
		}
	}

	@Override
	public void shutDown() throws Exception
	{
		for (EventHandler eventHandler : eventHandlers)
		{
			eventBus.unregister(eventHandler);
		}
		this.heartbeatLoop = false;
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			loggedIn = true;
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			loggedIn = false;
		}
	}

	@Subscribe
	private void onCommandExecuted(CommandExecuted event)
	{
		if (event.getCommand().equals("setEventConfig") && canSend(config.playerName()))
		{
			configManager.setRSProfileConfiguration(EventCollectorConfig.CONFIG_GROUP, "name", config.playerName());
			configManager.setRSProfileConfiguration(EventCollectorConfig.CONFIG_GROUP, "playerId", config.playerId());
			sendChatMessage("Set profile configuration successfully");
		}
	}

	private boolean canSend(String playerName)
	{
		return client.getLocalPlayer() != null &&
			client.getLocalPlayer().getName() != null &&
			client.getLocalPlayer().getName().equalsIgnoreCase(playerName);
	}

	public String getPlayerId()
	{
		if (canSend(config.playerName()))
		{
			return config.playerId();
		}
		else if (canSend(configManager.getRSProfileConfiguration(EventCollectorConfig.CONFIG_GROUP, "name")))
		{
			return configManager.getRSProfileConfiguration(EventCollectorConfig.CONFIG_GROUP, "playerId");
		}
		return null;
	}

	private void sendChatMessage(String chatMessage)
	{
		final String message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(chatMessage)
			.build();

		chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(message)
				.build());
	}

}
