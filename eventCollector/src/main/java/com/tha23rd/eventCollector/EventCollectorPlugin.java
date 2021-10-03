package com.tha23rd.eventCollector;

import com.google.inject.Provides;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.eventhandlers.BankOpenedHandler;
import com.tha23rd.eventCollector.eventhandlers.GamerLoggedHandler;
import com.tha23rd.eventCollector.eventhandlers.ItemConsumedHandler;
import com.tha23rd.eventCollector.eventhandlers.LevelUpHandler;
import com.tha23rd.eventCollector.eventhandlers.LootDroppedHandler;
import com.tha23rd.eventCollector.eventhandlers.PlayerDeathHandler;
import com.tha23rd.eventCollector.eventhandlers.QuestCompletedHandler;
import com.tha23rd.eventCollector.events.QuestCompleted;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
	tags = {"utility", "data", "collection"}
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
					sendChatMessage("Sending heartbeat");
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
		this.eventBus.register(itemConsumedHandler);
		this.eventBus.register(levelUpHandler);
		this.eventBus.register(questCompletedHandler);
		this.eventBus.register(playerDeathHandler);
		this.eventBus.register(lootDroppedHandler);
		this.eventBus.register(gamerLoggedHandler);
		this.eventBus.register(bankOpenedHandler);
	}
	@Override
	public void shutDown() throws Exception
	{
		this.eventBus.unregister(itemConsumedHandler);
		this.eventBus.unregister(levelUpHandler);
		this.eventBus.unregister(questCompletedHandler);
		this.eventBus.unregister(playerDeathHandler);
		this.eventBus.unregister(lootDroppedHandler);
		this.eventBus.unregister(gamerLoggedHandler);
		this.eventBus.unregister(bankOpenedHandler);
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
