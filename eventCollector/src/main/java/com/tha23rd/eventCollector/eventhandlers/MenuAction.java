package com.tha23rd.eventCollector.eventhandlers;


import com.tha23rd.eventCollector.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Item;

/**
 * Data class that tracks all info related to a menu click action
 */
@AllArgsConstructor
public class MenuAction
{
	@Getter
	private ActionType type;
	@Getter
	private Item[] oldInventory;

	static class ItemAction extends MenuAction
	{

		@Getter
		private int itemID;
		@Getter
		private int slot;

		ItemAction(final ActionType type, final Item[] oldInventory, final int itemID, final int slot)
		{
			super(type, oldInventory);
			this.itemID = itemID;
			this.slot = slot;
		}
	}
}
