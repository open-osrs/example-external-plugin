package com.example.customoneclick.comparables.skilling;

import com.google.common.base.Splitter;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import com.example.customoneclick.comparables.ClickCompare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Karambwans extends ClickCompare
{

	private static final Splitter NEWLINE_SPLITTER = Splitter
			.on("\n")
			.omitEmptyStrings()
			.trimResults();
	private final Map<Integer, List<Integer>> customClickMap = new HashMap<>();

	@Override
	public boolean isEntryValid(MenuEntry event)
	{
		return event.getOpcode() == MenuAction.EXAMINE_OBJECT.getId() && customClickMap.containsKey(event.getId()) && !event.isForceLeftClick();
	}

	@Override
	public void modifyEntry(MenuEntry event)
	{
		if (findItem(customClickMap.get(event.getIdentifier())).getLeft() == -1)
		{
			return;
		}
		System.out.print("modifyEntry:     ");
		System.out.println(event);
		int id = event.getIdentifier();
		int item = findItem(customClickMap.get(id)).getLeft();
		final String name = client.getItemComposition(item).getName();
		MenuEntry e = event.clone();
		e.setOption("Use");
		e.setTarget("<col=ff9040>" + name + "<col=ffffff> -> " + getTargetMap().get(id));
		e.setForceLeftClick(true);
		e.setOpcode(1);
		insert(e);
	}

	@Override
	public boolean isClickValid(MenuOptionClicked event)
	{
		return event.getMenuAction() == MenuAction.ITEM_USE_ON_GAME_OBJECT && event.getMenuTarget().contains("->") && customClickMap.containsKey(event.getId());
	}

	@Override
	public void modifyClick(MenuOptionClicked event)
	{
		if (updateSelectedItem(customClickMap.get(event.getId())) && plugin != null)
		{
			event.setMenuAction(MenuAction.ITEM_USE_ON_GAME_OBJECT);
			plugin.setTick(true);
		}
	}

	@Override
	public void backupEntryModify(MenuEntry e)
	{
		if (findItem(customClickMap.get(e.getIdentifier())).getLeft() == -1)
		{
			return;
		}
		System.out.print("backupEntryModify:     ");
		System.out.println(e);
		e.setOption("Use");
		final String name = client.getItemComposition(findItem(customClickMap.get(e.getIdentifier())).getLeft()).getName();
		e.setTarget("<col=ff9040>" + name + "<col=ffffff> -> " + getTargetMap().get(e.getIdentifier()));
		e.setForceLeftClick(true);
		e.setOpcode(1);
		insert(e);
	}

	public void updateMap(String swaps)
	{
		final Iterable<String> tmp = NEWLINE_SPLITTER.split(swaps);

		for (String s : tmp)
		{
			if (s.startsWith("//"))
			{
				continue;
			}

			String[] split = s.split(":");

			try
			{
				int oneClickThat = Integer.parseInt(split[0]);
				int withThis = Integer.parseInt(split[1]);
				if (customClickMap.containsKey(oneClickThat))
				{
					customClickMap.get(oneClickThat).add(withThis);
					continue;
				}
				customClickMap.put(oneClickThat, new ArrayList<>(withThis));
			}
			catch (Exception e)
			{
				//log.error("Error: ", e);
				return;
			}
		}
	}
}
