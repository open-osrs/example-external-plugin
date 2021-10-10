package com.tha23rd.customOneClick;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

public class CustomOneClickPlugin
{

	@Inject
	private Client client;

	@Inject
	private CustomOneClickConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	private boolean tick = false;

	@Provides
	CustomOneClickConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomOneClickConfig.class);
	}

	final private Set<Integer> fishingPoles = new HashSet<>(Arrays.asList(ItemID.BARBARIAN_ROD, ItemID.OILY_FISHING_ROD));

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		MenuEntry entryToSwap = new MenuEntry();
		if (config.getFishingSpot() && event.getMenuAction().getId() == MenuAction.ITEM_USE.getId() && fishingPoles.contains(event.getId()))
		{
			NPC spot = findNearestFishingSpot(event.getIdentifier());
			if (spot == null)
			{
				return;
			}
			event.setOption("Fish");
			event.setOpcode(MenuAction.NPC_FIRST_OPTION.getId());
			event.setIdentifier(spot.getIndex());
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		MenuEntry entryToSwap = new MenuEntry();
		if (tick)
		{
			// only needed for one tick shit
			System.out.println("consumed");
			event.consume();
		}
		else if (config.getFishingSpot() && event.getMenuAction().getId() == MenuAction.ITEM_USE.getId() && fishingPoles.contains(event.getId()))
		{
			// send the id of the fishing rod to this method, so we find the right spot
			NPC spot = findNearestFishingSpot(event.getId());
			if (spot == null)
			{
				return;
			}

			entryToSwap.setOption("Fish");
			entryToSwap.setOpcode(MenuAction.NPC_FIRST_OPTION.getId());
			entryToSwap.setIdentifier(spot.getIndex());
		}
		client.setLeftClickMenuEntry(entryToSwap);
		event.setMenuEntry(entryToSwap);
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		tick = false;
	}

	private NPC findNearestFishingSpot(int rod)
	{
		int spot = -1;
		switch (rod)
		{
			case ItemID.BARBARIAN_ROD:
				spot = NpcID.FISHING_SPOT_1542;
				break;
			case ItemID.FLY_FISHING_ROD:
				spot = NpcID.ROD_FISHING_SPOT_1515;
				break;
		}

		WorldPoint me = client.getLocalPlayer().getWorldLocation();

		int minDist = Integer.MAX_VALUE;
		NPC minSpot = null;
		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() != spot)
			{
				continue;
			}

			int dist = npc.getWorldLocation().distanceTo(me);
			if (dist < minDist)
			{
				minDist = dist;
				minSpot = npc;
			}
		}

		return minSpot;
	}
}
