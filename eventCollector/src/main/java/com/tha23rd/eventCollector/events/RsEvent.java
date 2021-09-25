package com.tha23rd.eventCollector.events;

import java.util.Date;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RsEvent<T>
{

	final String event_type;
	final String player_id;
	final T event_details;
	Date timestamp = new Date();
}
