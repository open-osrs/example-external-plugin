package com.tha23rd.eventCollector.eventhandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.PlayerDeath;
import com.tha23rd.eventCollector.events.RsEvent;
import java.util.Date;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.eventbus.Subscribe;

public class PlayerDeathHandler extends EventHandler<PlayerDeath>
{
	private static final String EVENT_TYPE = "playerDeath";
	@Inject
	public PlayerDeathHandler(Client client, EventCollectorConfig config)
	{
		super(client, config);
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (client.isInInstancedRegion() || actorDeath.getActor() != client.getLocalPlayer())
		{
			return;
		}

		PlayerDeath playerDeath = new PlayerDeath(new Date(), client.getLocalPlayer().getWorldLocation());
		RsEvent<PlayerDeath> rsEvent = new RsEvent<>(EVENT_TYPE, config.playerId(), playerDeath);
		sendEvent(rsEvent);
	}

	@Override
	void sendEvent(RsEvent<PlayerDeath> rsEvent)
	{
		try {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create();
			RsServiceClient.getClient(this.config.apiurl()).postEvent(gson.toJson(rsEvent));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
