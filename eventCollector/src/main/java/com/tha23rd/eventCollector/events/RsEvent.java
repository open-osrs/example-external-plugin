package com.tha23rd.eventCollector.events;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RsEvent<T>
{
	String event_type;
	String player_id;
	T event_details;
}
