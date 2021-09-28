package com.tha23rd.eventCollector.eventhandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
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

	private boolean canSend() {
		return client.getLocalPlayer() != null &&
			client.getLocalPlayer().getName() != null &&
			client.getLocalPlayer().getName().equalsIgnoreCase(config.playerName());
	}

	public void sendEvent(RsEvent<T> rsEvent) {
		if (canSend()) {
			try {
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create();
				RsServiceClient.getClient(this.config.apiurl()).postEvent(gson.toJson(rsEvent));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
