package com.tha23rd.eventCollector.eventhandlers;

import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.EventCollectorPlugin;
import com.tha23rd.eventCollector.events.PlayerDeath;
import com.tha23rd.eventCollector.events.RsEvent;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.eventbus.Subscribe;

public class PlayerDeathHandler extends EventHandler<PlayerDeath>
{
	private static final String EVENT_TYPE = "playerDeath";

	@Inject
	public PlayerDeathHandler(Client client, EventCollectorConfig config, EventCollectorPlugin plugin)
	{
		super(client, config, plugin);
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (client.isInInstancedRegion() || actorDeath.getActor() != client.getLocalPlayer())
		{
			return;
		}

		PlayerDeath playerDeath = new PlayerDeath(client.getLocalPlayer().getWorldLocation());
		RsEvent<PlayerDeath> rsEvent = new RsEvent<>(EVENT_TYPE, playerDeath);
		sendEvent(rsEvent);
	}
}
