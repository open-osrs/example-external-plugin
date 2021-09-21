package com.tha23rd.eventCollector.eventhandlers;

import net.runelite.client.game.ItemManager;

import javax.inject.Singleton;

@Singleton
public class Prayer
{
	private ItemConsumedHandler handler;
	private ItemManager itemManager;
	private int bonesId = 0;

	public Prayer(ItemConsumedHandler plugin, ItemManager itemManager)
	{
		this.handler = plugin;
		this.itemManager = itemManager;
	}

	public void OnChat(String message)
	{
		String name = itemManager.getItemComposition(bonesId).getName().toLowerCase();

		if (bonesId <= 0 || !name.contains("bones"))
		{
			return;
		}
		if (message.toLowerCase().contains("you bury the bones"))
		{
			handler.buildEntries(bonesId);
		}
	}

	public void build()
	{
		handler.buildEntries(bonesId);
	}

	public void setBonesId(int bonesId)
	{
		this.bonesId = bonesId;
	}
}
