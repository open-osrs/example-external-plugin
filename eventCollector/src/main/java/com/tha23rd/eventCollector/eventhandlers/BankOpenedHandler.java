package com.tha23rd.eventCollector.eventhandlers;

import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.EventCollectorPlugin;
import com.tha23rd.eventCollector.events.BankItem;
import com.tha23rd.eventCollector.events.BankOpened;
import com.tha23rd.eventCollector.events.RsEvent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
public class BankOpenedHandler extends EventHandler<BankOpened>
{
	@Inject
	ItemManager itemManager;
	private static final String EVENT_TYPE = "bankOpened";

	@Inject
	public BankOpenedHandler(Client client, EventCollectorConfig config, EventCollectorPlugin plugin)
	{
		super(client, config, plugin);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.BANK_GROUP_ID)
		{
			ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
			if (bankContainer != null)
			{
				int bankValue = 0;
				final List<BankItem> cachedItems = new ArrayList<>(bankContainer.getItems().length);
				for (Item item : bankContainer.getItems())
				{
					if (itemManager.canonicalize(item.getId()) != item.getId() || item.getId() == -1)
					{
						continue;
					}
					int itemPrice = itemManager.getItemPrice(item.getId());
					bankValue += itemPrice;
					cachedItems.add(new BankItem(item.getId(), item.getQuantity()));
				}
				BankItem[] bankItemsArray = new BankItem[cachedItems.size()];
				BankOpened bankOpened = new BankOpened(false, cachedItems.toArray(bankItemsArray), bankValue);
				RsEvent<BankOpened> rsEvent = new RsEvent<>(EVENT_TYPE, bankOpened);
				sendEvent(rsEvent);
			}
			else
			{
				log.info("Bank container is null for some reason!");
			}
		}
	}
}
