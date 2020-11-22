//Created by PluginCreator by ImNo: https://github.com/ImNoOSRS 
package net.runelite.client.plugins.discordNotifier;

import com.google.common.collect.ImmutableList;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.util.Text;
import static net.runelite.api.widgets.WidgetID.LEVEL_UP_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.QUEST_COMPLETED_GROUP_ID;
import net.runelite.api.widgets.WidgetInfo;
import static net.runelite.client.plugins.discordNotifier.Utils.parseLevelUpWidget;
import static net.runelite.client.plugins.discordNotifier.Utils.parseQuestCompletedWidget;
import net.runelite.client.plugins.discordNotifier.discord.Author;
import net.runelite.client.plugins.discordNotifier.discord.Embed;
import net.runelite.client.plugins.discordNotifier.discord.Field;
import net.runelite.client.plugins.discordNotifier.discord.Image;
import net.runelite.client.plugins.discordNotifier.discord.Webhook;
import net.runelite.client.ui.DrawManager;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import javax.inject.Inject;
import java.util.List;
import okhttp3.HttpUrl;
import org.json.JSONObject;

@PluginDescriptor(
		name = "Achievement Discord Notifier",
		description = "Notify discord when you do something awesome",
		type = PluginType.SYSTEM
)
@Slf4j
public class DiscordNotifierPlugin extends Plugin {
	// Injects our config
	@Inject
	private ConfigManager configManager;
	@Inject
	private DiscordNotifierConfig config;
	private static final String PET_MESSAGE_DUPLICATE = "You have a funny feeling like you would have been followed";
	private static final ImmutableList<String> PET_MESSAGES = ImmutableList.of(
		"You have a funny feeling like you're being followed", "You feel something weird sneaking into your backpack",
		"You have a funny feeling like you would have been followed", PET_MESSAGE_DUPLICATE);

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private DrawManager drawManager;

	private CompletableFuture<java.awt.Image> queuedScreenshot = null;

	private boolean shouldTakeScreenshot;

	@Provides
	DiscordNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordNotifierConfig.class);
	}

	private boolean shouldBeIgnored(String itemName)
	{
		String lowerName = itemName.toLowerCase();
		List<String> keywords = Arrays.asList(config.ignoredKeywords().split(","));
		return keywords.stream().anyMatch(key -> key.length() > 0 && lowerName.contains(key.toLowerCase()));
	}

	private void queueScreenshot()
	{
		if (queuedScreenshot == null && config.sendScreenshot())
		{
			queuedScreenshot = getScreenshot();
		}
	}

	private void sendScreenshotIfSupposedTo()
	{
		if (queuedScreenshot != null && config.sendScreenshot())
		{
			CompletableFuture<java.awt.Image> copy = queuedScreenshot;
			queuedScreenshot = null;
			copy.thenAccept(screenshot -> sendScreenshot(getWebhookUrls(), screenshot)).handle((v, e) ->
			{
				if (e != null)
				{
					log.error(String.format("sendScreenshotIfSupposedTo error: %s", e.getMessage()), e);
				}
				queuedScreenshot = null;
				return null;
			});
		}
	}

	private CompletableFuture<Void> queueAchievementNotification(String playerName, String playerIconUrl, int itemId,
																 int quantity, float rarity, int npcId, int npcCombatLevel, String npcName, String eventName, String webhookUrl)
	{
		Author author = new Author();
		author.setName(playerName);

		if (playerIconUrl != null)
		{
			author.setIcon_url(playerIconUrl);
		}

		Field rarityField = new Field();
		rarityField.setName("Rarity");
//		rarityField.setValue(getRarityString(rarity));
		rarityField.setInline(true);

		Field haValueField = new Field();
		haValueField.setName("HA Value");
//		haValueField.setValue(getGPValueString(itemManager.getItemComposition(itemId).getHaPrice() * quantity));
		haValueField.setInline(true);

		Field geValueField = new Field();
		geValueField.setName("GE Value");
//		geValueField.setValue(getGPValueString(itemManager.getItemPrice(itemId) * quantity));
		geValueField.setInline(true);

		Embed embed = new Embed();
		embed.setAuthor(author);
		embed.setFields(new Field[] { rarityField, haValueField, geValueField });

		Image thumbnail = new Image();
		CompletableFuture<Void> iconFuture = ApiTool.getInstance()
			.getIconUrl("item", itemId, "name").handle((iconUrl, e) ->
			{
				if (e != null)
				{
					log.error(String.format("queueLootNotification (icon %d) error: %s", itemId, e.getMessage()), e);
				}
				thumbnail.setUrl(iconUrl);
				embed.setThumbnail(thumbnail);
				return null;
			});

		CompletableFuture<Void> descFuture = getLootNotificationDescription(itemId, quantity, npcId, npcCombatLevel,
			npcName, eventName).handle((notifDesc, e) ->
		{
			if (e != null)
			{
				log.error(String.format("queueLootNotification (desc %d) error: %s", itemId, e.getMessage()), e);
			}
			embed.setDescription(notifDesc);
			return null;
		});

		return CompletableFuture.allOf(descFuture, iconFuture).thenCompose(_v ->
		{
			Webhook webhookData = new Webhook();
			webhookData.setEmbeds(new Embed[] { embed });
			return sendWebhookData(getWebhookUrls(), webhookData);
		});
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int groupId = event.getGroupId();
		switch (groupId)
		{
			case LEVEL_UP_GROUP_ID:
			case QUEST_COMPLETED_GROUP_ID:
			{
				// level up widget gets loaded prior to the text being set, so wait until the next tick
				shouldTakeScreenshot = true;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!shouldTakeScreenshot)
		{
			return;
		}

		shouldTakeScreenshot = false;

		if (client.getWidget(WidgetInfo.LEVEL_UP_LEVEL) != null)
		{
			List<String> skillLevelArray = parseLevelUpWidget(WidgetInfo.LEVEL_UP_LEVEL, client);
			if (config.sendScreenshot() && skillLevelArray != null)
			{
				// take screenshot
				CompletableFuture<java.awt.Image> screenshotFuture = config.sendScreenshot() ? getScreenshot()
					: CompletableFuture.completedFuture(null);

				screenshotFuture
					// Waiting for screenshot before checking pet allows us to wait one frame, in
					// case pet data is not available yet
					// TODO: Figure out how to get pet info
					.thenApply(screenshot -> queueLevelUpNotification(getPlayerName(), getPlayerIconUrl(), skillLevelArray.get(0), Integer.parseInt(skillLevelArray.get(1)))
						.thenCompose(_v -> screenshot != null ? sendScreenshot(getWebhookUrls(), screenshot)
							: CompletableFuture.completedFuture(null)))
					.exceptionally(e ->
					{
						log.error(String.format("onChatMessage (pet) error: %s", e.getMessage()), e);
						return null;
					});
			}

		}
//		else if (client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT) != null)
//		{
//			String text = client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT).getText();
//			fileName = parseQuestCompletedWidget(text);
//			screenshotSubDir = "Quests";
//		}
//
//		if (fileName != null)
//		{
//			takeScreenshot(fileName, screenshotSubDir);
//		}
	}

	private CompletableFuture<Void> queueLevelUpNotification(String playerName, String playerIconUrl, String skill,
														 int skillLevel)
	{
		Author author = new Author();
		author.setName(playerName);

		if (playerIconUrl != null)
		{
			author.setIcon_url(playerIconUrl);
		}

		Field rarityField = new Field();
		rarityField.setName("Skill");
		rarityField.setValue(skill);
		rarityField.setInline(true);

		Field haValueField = new Field();
		haValueField.setName("Skill Level");
		haValueField.setValue(skillLevel + "");
		haValueField.setInline(true);


		Embed embed = new Embed();
		embed.setAuthor(author);
		embed.setFields(new Field[] { /* rarityField */ });


		return CompletableFuture.allOf().thenCompose(_v ->
		{
			Webhook webhookData = new Webhook();
			webhookData.setEmbeds(new Embed[] { embed });
			return sendWebhookData(getWebhookUrls(), webhookData);
		});
	}

	private CompletableFuture<java.awt.Image> getScreenshot()
	{
		CompletableFuture<java.awt.Image> f = new CompletableFuture<>();
		drawManager.requestNextFrameListener(f::complete);
		return f;
	}

	private CompletableFuture<Void> sendWebhookData(List<String> webhookUrls, Webhook webhookData)
	{
		JSONObject json = new JSONObject(webhookData);
		String jsonStr = json.toString();

		List<Throwable> exceptions = new ArrayList<>();
		List<CompletableFuture<Void>> sends = webhookUrls.stream()
			.map(url -> ApiTool.getInstance().postRaw(url, jsonStr, "application/json").handle((_v, e) ->
			{
				if (e != null)
				{
					exceptions.add(e);
				}
				return null;
			}).thenAccept(_v ->
			{
			})).collect(Collectors.toList());

		return CompletableFuture.allOf(sends.toArray(new CompletableFuture[sends.size()])).thenCompose(_v ->
		{
			if (exceptions.size() > 0)
			{
				log.error(String.format("sendWebhookData got %d error(s)", exceptions.size()));
				exceptions.forEach(t -> log.error(t.getMessage()));
				CompletableFuture<Void> f = new CompletableFuture<>();
				f.completeExceptionally(exceptions.get(0));
				return f;
			}
			return CompletableFuture.completedFuture(null);
		});
	}

	private CompletableFuture<Void> sendScreenshot(List<String> webhookUrls, java.awt.Image screenshot)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write((BufferedImage) screenshot, "png", baos);
			byte[] imageBytes = baos.toByteArray();

			List<Throwable> exceptions = new ArrayList<>();
			List<CompletableFuture<Void>> sends = webhookUrls.stream()
				.map(url -> ApiTool.getInstance().postFormImage(url, imageBytes, "image/png").handle((_v, e) ->
				{
					if (e != null)
					{
						exceptions.add(e);
					}
					return null;
				}).thenAccept(_v ->
				{
				})).collect(Collectors.toList());

			return CompletableFuture.allOf(sends.toArray(new CompletableFuture[sends.size()])).thenCompose(_v ->
			{
				if (exceptions.size() > 0)
				{
					log.error(String.format("sendScreenshot got %d error(s)", exceptions.size()));
					exceptions.forEach(t -> log.error(t.getMessage()));
					CompletableFuture<Void> f = new CompletableFuture<>();
					f.completeExceptionally(exceptions.get(0));
					return f;
				}
				return CompletableFuture.completedFuture(null);
			});
		}
		catch (Exception e)
		{
			log.error("Unable to send screenshot", e);
			return CompletableFuture.completedFuture(null);
		}
	}

	// TODO: Add Pet notification

	private CompletableFuture<String> getLootNotificationDescription(int itemId, int quantity, int npcId,
																	 int npcCombatLevel, String npcName, String eventName)
	{
//		ItemComposition itemComp = itemManager.getItemComposition(itemId);

		return ApiTool.getInstance().getItem(itemId).thenCompose(itemJson ->
		{
			String itemUrl = itemJson.getString("wiki_url");
			String baseMsg = "Just got " + (quantity > 1 ? quantity + "x " : "") + "[" + "](" + itemUrl
				+ ")";

			if (npcId >= 0)
			{
				return ApiTool.getInstance().getNPC(npcId).thenApply(npcJson ->
				{
					String npcUrl = npcJson.getString("wiki_url");
					String fullMsg = baseMsg + " from lvl " + npcCombatLevel + " [" + npcName + "](" + npcUrl + ")";
					return fullMsg;
				}).exceptionally(e ->
				{
					log.error("!= NPC info for " + npcId + " (" + e.getMessage() + ")");
					return baseMsg + " from lvl " + npcCombatLevel + " " + npcName;
				});
			}
			else if (eventName != null)
			{
				String eventUrl = HttpUrl.parse("https://oldschool.runescape.wiki/").newBuilder()
					.addPathSegments("w/Special:Search").addQueryParameter("search", eventName).build().toString();
				String fullMsg = baseMsg + " from [" + eventName + "](" + eventUrl + ")";
				return CompletableFuture.completedFuture(fullMsg);
			}
			else
			{
				return CompletableFuture.completedFuture(baseMsg + " from something");
			}
		});
	}

	private String getPetNotificationDescription(boolean isDuplicate)
	{
		if (isDuplicate)
		{
			return "Would've gotten a pet, but already has it.";
		}
		else
		{
			return "Just got a pet.";
		}
	}

	private String getPlayerIconUrl()
	{
		switch (client.getAccountType())
		{
			case IRONMAN:
				return "https://oldschool.runescape.wiki/images/0/09/Ironman_chat_badge.png";
			case HARDCORE_IRONMAN:
				return "https://oldschool.runescape.wiki/images/b/b8/Hardcore_ironman_chat_badge.png";
			case ULTIMATE_IRONMAN:
				return "https://oldschool.runescape.wiki/images/0/02/Ultimate_ironman_chat_badge.png";
			default:
				return null;
		}
	}

	private String getPlayerName()
	{
		return client.getLocalPlayer().getName();
	}

	private List<String> getWebhookUrls()
	{
		return Arrays.asList(config.webhookUrl().split("\n")).stream().filter(u -> u.length() > 0)
			.collect(Collectors.toList());
	}
}