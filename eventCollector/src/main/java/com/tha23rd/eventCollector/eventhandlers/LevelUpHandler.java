package com.tha23rd.eventCollector.eventhandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.LevelUp;
import com.tha23rd.eventCollector.events.RsEvent;
import java.awt.Image;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetID.LEVEL_UP_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.QUEST_COMPLETED_GROUP_ID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

public class LevelUpHandler extends EventHandler<LevelUp>
{

	boolean parseLevelUp = false;
	private static final Pattern LEVEL_UP_PATTERN = Pattern.compile(".*Your ([a-zA-Z]+) (?:level is|are)? now (\\d+)\\.");
	private static final String EVENT_TYPE = "levelUp";
	@Inject
	public LevelUpHandler(Client client, EventCollectorConfig config)
	{
		super(client, config);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!parseLevelUp)
			return;

		parseLevelUp = false;
		if (client.getWidget(WidgetInfo.LEVEL_UP_LEVEL) != null)
		{
			List<String> skillLevelArray = parseLevelUpWidget(WidgetInfo.LEVEL_UP_LEVEL, client);
			if (skillLevelArray != null)
			{
				// if we don't meet min level and interval reqs, just return to avoid spamming channel
				int level = Integer.parseInt(skillLevelArray.get(1));
				String skillName = skillLevelArray.get(0);

				LevelUp levelUp = new LevelUp(level, skillName, new Date());
				RsEvent<LevelUp> rsEvent = new RsEvent<>(EVENT_TYPE, config.playerId(), levelUp);
				sendEvent(rsEvent);
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int groupId = event.getGroupId();
		if (groupId == LEVEL_UP_GROUP_ID)
		{
			parseLevelUp = true;
		}
	}

	static List<String> parseLevelUpWidget(WidgetInfo levelUpLevel, Client client)
	{
		Widget levelChild = client.getWidget(levelUpLevel);
		if (levelChild == null)
		{
			return null;
		}

		Matcher m = LEVEL_UP_PATTERN.matcher(levelChild.getText());
		if (!m.matches())
		{
			return null;
		}

		String skillName = m.group(1);
		String skillLevel = m.group(2);
		return Arrays.asList(skillName, skillLevel);
	}

	@Override
	void sendEvent(RsEvent<LevelUp> rsEvent)
	{
		try {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create();
			RsServiceClient.getClient(this.config.apiurl()).postEvent(gson.toJson(rsEvent));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
