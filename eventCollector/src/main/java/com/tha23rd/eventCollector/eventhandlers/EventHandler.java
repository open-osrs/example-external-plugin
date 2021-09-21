package com.tha23rd.eventCollector.eventhandlers;

import com.tha23rd.eventCollector.EventCollectorConfig;
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

	abstract void sendEvent(RsEvent<T> rsEvent);
}
