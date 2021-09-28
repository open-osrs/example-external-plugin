package com.tha23rd.eventCollector.eventhandlers;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tha23rd.eventCollector.EventCollectorConfig;
import com.tha23rd.eventCollector.client.RsServiceClient;
import com.tha23rd.eventCollector.events.LootDropped;
import com.tha23rd.eventCollector.events.RsEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;


@Slf4j
public class LootDroppedHandler extends EventHandler<LootDropped>
{
	private static final String SIRE_FONT_TEXT = "you place the unsired into the font of consumption...";
	private static final String SIRE_REWARD_TEXT = "the font consumes the unsired";
	private static final int MAX_TEXT_CHECK = 25;
	private static final int MAX_PET_TICKS = 5;
	private static final int NMZ_MAP_REGION = 9033;
	// Kill count handling
	private static final Pattern CLUE_SCROLL_PATTERN = Pattern.compile("You have completed ([0-9]+) ([a-z]+) Treasure Trails.");
	private static final Pattern BOSS_NAME_NUMBER_PATTERN = Pattern.compile("Your (.*) kill count is:? ([0-9]*).");
	private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");

	private static final ImmutableSet<String> PET_MESSAGES = ImmutableSet.of("You have a funny feeling like you're being followed.",
		"You feel something weird sneaking into your backpack.",
		"You have a funny feeling like you would have been followed...");

	private boolean unsiredReclaiming = false;
	private int unsiredCheckCount = 0;
	// Some pets aren't handled (skilling pets) so reset gotPet after a few ticks
	private int petTicks = 0;
	private boolean gotPet = false;
	private static final String EVENT_TYPE = "lootDropped";
	@Inject
	private ItemManager itemManager;
	@Inject
	public LootDroppedHandler(Client client, EventCollectorConfig config)
	{
		super(client, config);
		System.out.println("LootHandler booted up");
	}

	@Subscribe
	public void onLootReceived(final LootReceived event)
	{
		if (isInNightmareZone())
		{
			return;
		}

		if (gotPet)
		{
			final Pet p = Pet.getByBossName(event.getName());
			if (p != null)
			{
				gotPet = false;
				petTicks = 0;
				LootDropped lootDropped = new LootDropped(p.getPetID(), p.name(), -1, true);
				RsEvent<LootDropped> rsEvent = new RsEvent<>(EVENT_TYPE, config.playerId(), lootDropped);
				sendEvent(rsEvent);
			}
		}
		Collection<UniqueItem> uItems = UniqueItem.getUniquesForBoss(event.getName().toUpperCase());
		for (ItemStack item: event.getItems()) {
			uItems.forEach(uniqueItem -> {
				if (uniqueItem.getItemID() == item.getId()) {
					int price = itemManager.getItemPrice(item.getId());
					LootDropped lootDropped = new LootDropped(item.getId(), uniqueItem.getName(), price, false);
					RsEvent<LootDropped> rsEvent = new RsEvent<>(EVENT_TYPE, config.playerId(), lootDropped);
					sendEvent(rsEvent);
				}
			});
		}

	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		if (gotPet)
		{
			if (petTicks > MAX_PET_TICKS)
			{
				gotPet = false;
				petTicks = 0;
			}
			else
			{
				petTicks++;
			}
		}

//		if (unsiredReclaiming)
//		{
//			if (hasUnsiredWidgetUpdated())
//			{
//				unsiredReclaiming = false;
//				return;
//			}
//
//			unsiredCheckCount++;
//			if (unsiredCheckCount >= MAX_TEXT_CHECK)
//			{
//				unsiredReclaiming = false;
//			}
//		}
	}

	// Handles checking for unsired loot reclamation
//	private boolean hasUnsiredWidgetUpdated()
//	{
//		final Widget text = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
//		// Reclaimed an item?
//		if (text != null && text.getText().toLowerCase().contains(SIRE_REWARD_TEXT))
//		{
//			final Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE);
//			if (sprite == null || sprite.getItemId() == -1)
//			{
//				return false;
//			}
//
//			log.debug("Unsired was exchanged for item ID: {}", sprite.getItemId());
//			receivedUnsiredLoot(sprite.getItemId());
//			return true;
//		}
//
//		return false;
//	}

	// Handles adding the unsired loot to the tracker
//	private void receivedUnsiredLoot(int itemID)
//	{
//		client.getClientThread().invokeLater(() ->
//		{
//			Collection<LTRecord> data = getDataByName(LootRecordType.NPC, BossTab.ABYSSAL_SIRE.getName());
//			ItemComposition c = itemManager.getItemComposition(itemID);
//			LTItemEntry itemEntry = new LTItemEntry(c.getName(), itemID, 1, 0);
//
//			log.debug("Received Unsired item: {}", c.getName());
//
//			// Don't have data for sire, create a new record with just this data.
//			if (data == null)
//			{
//				log.debug("No previous Abyssal sire loot, creating new loot record");
//				LTRecord r = new LTRecord(BossTab.ABYSSAL_SIRE.getName(), 350, -1, LootRecordType.NPC, Collections.singletonList(itemEntry), new Date());
//				addRecord(r);
//				return;
//			}
//
//			log.debug("Adding drop to last abyssal sire loot record");
//			// Add data to last kill count
//			final List<LTRecord> items = new ArrayList<>(data);
//			final LTRecord r = items.get(items.size() - 1);
//			r.addDropEntry(itemEntry);
//			writer.writeLootTrackerFile(BossTab.ABYSSAL_SIRE.getName(), items);
//			if (config.enableUI())
//			{
//				SwingUtilities.invokeLater(panel::refreshUI);
//			}
//		});
//	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		final String chatMessage = Text.removeTags(event.getMessage());

		if (PET_MESSAGES.contains(chatMessage))
		{
			gotPet = true;
		}

		// Check if message is for a clue scroll reward
		final Matcher m = CLUE_SCROLL_PATTERN.matcher(chatMessage);
		if (m.find())
		{
			final String eventType;
			switch (m.group(2).toLowerCase())
			{
				case "beginner":
					eventType = "Clue Scroll (Beginner)";
					break;
				case "easy":
					eventType = "Clue Scroll (Easy)";
					break;
				case "medium":
					eventType = "Clue Scroll (Medium)";
					break;
				case "hard":
					eventType = "Clue Scroll (Hard)";
					break;
				case "elite":
					eventType = "Clue Scroll (Elite)";
					break;
				case "master":
					eventType = "Clue Scroll (Master)";
					break;
				default:
					return;
			}

			final int killCount = Integer.valueOf(m.group(1));
//			killCountMap.put(eventType.toUpperCase(), killCount);
			return;
		}

		// Barrows KC
		if (chatMessage.startsWith("Your Barrows chest count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
//				killCountMap.put("BARROWS", Integer.valueOf(n.group()));
				return;
			}
		}

		// Raids KC
		if (chatMessage.startsWith("Your completed Chambers of Xeric count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
//				killCountMap.put("CHAMBERS OF XERIC", Integer.valueOf(n.group()));
				return;
			}
		}

		// Tob KC
		if (chatMessage.startsWith("Your completed Theatre of Blood count is"))
		{
			Matcher n = NUMBER_PATTERN.matcher(chatMessage);
			if (n.find())
			{
//				killCountMap.put("THEATRE OF BLOOD", Integer.valueOf(n.group()));
				return;
			}
		}

		// Handle all other boss
		final Matcher boss = BOSS_NAME_NUMBER_PATTERN.matcher(chatMessage);
		if (boss.find())
		{
			final String bossName = boss.group(1);
			final int killCount = Integer.valueOf(boss.group(2));
//			killCountMap.put(bossName.toUpperCase(), killCount);
		}
	}

	/**
	 * Is the player inside the NMZ arena?
	 */
	private boolean isInNightmareZone()
	{
		if (client.getLocalPlayer() == null) {
			return false;
		}

		// It seems that KBD shares the map region with NMZ but NMZ is never in plane 0.
		int[] regions = client.getMapRegions();
		return Arrays.asList(regions).contains( NMZ_MAP_REGION) && client.getLocalPlayer().getWorldLocation().getPlane() > 0;
	}
}
