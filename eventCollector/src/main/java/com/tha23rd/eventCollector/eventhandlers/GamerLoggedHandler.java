package com.tha23rd.eventCollector.eventhandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.RsEvent;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

public class GamerLoggedHandler extends EventHandler
{
	private Instant sixHourWarningTime;
	private static final Duration SIX_HOUR_LOGOUT_WARNING_AFTER_DURATION = Duration.ofMinutes(340);
	private boolean ready;
	private boolean notify6HourLogout = true;
	private static final String EVENT_TYPE = "sixHourLogged";


	@Inject
	public GamerLoggedHandler(Client client, EventCollectorConfig config)
	{
		super(client, config);
	}

	@Subscribe
	private void onGameTick(GameTick gameTick) {
		if (check6hrLogout())
		{
			RsEvent rsEvent = new RsEvent(EVENT_TYPE, config.playerId(), null);
			sendEvent(rsEvent);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{

		GameState state = gameStateChanged.getGameState();

		switch (state)
		{
			case LOGIN_SCREEN:
				break;
			case LOGGING_IN:
			case HOPPING:
			case CONNECTION_LOST:
				ready = true;
				break;
			case LOGGED_IN:
				if (ready)
				{
					sixHourWarningTime = Instant.now().plus(SIX_HOUR_LOGOUT_WARNING_AFTER_DURATION);
					ready = false;
				}
				break;
		}
	}

	private boolean check6hrLogout()
	{
		if (sixHourWarningTime == null)
		{
			return false;
		}

		if (Instant.now().compareTo(sixHourWarningTime) >= 0)
		{
			if (notify6HourLogout)
			{
				notify6HourLogout = false;
				return true;
			}
		}
		else
		{
			notify6HourLogout = true;
		}

		return false;
	}

	@Override
	void sendEvent(RsEvent rsEvent)
	{
		try {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create();
			RsServiceClient.getClient(this.config.apiurl()).postEvent(gson.toJson(rsEvent));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
