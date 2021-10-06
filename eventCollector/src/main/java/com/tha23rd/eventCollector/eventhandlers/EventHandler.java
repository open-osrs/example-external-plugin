package com.tha23rd.eventCollector.eventhandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.EventCollectorPlugin;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.RsEvent;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.runelite.api.Client;

@AllArgsConstructor
public abstract class EventHandler<T>
{
	@Setter
	Client client;
	@Setter
	EventCollectorConfig config;
	@Setter
	EventCollectorPlugin plugin;

	public void sendEvent(RsEvent<T> rsEvent)
	{
		String playerId = plugin.getPlayerId();
		if (playerId != null)
		{
			try
			{
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create();
				RsServiceClient.getClient(this.config.apiurl()).postEvent(gson.toJson(rsEvent.getEvent(playerId)), true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
