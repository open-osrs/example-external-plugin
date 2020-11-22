//Created by PluginCreator by ImNo: https://github.com/ImNoOSRS
package com.tha23rd.discordNotifier;

import static com.tha23rd.discordNotifier.Utils.parseQuestCompletedWidget;
import com.tha23rd.discordNotifier.discord.Author;
import com.tha23rd.discordNotifier.discord.Embed;
import com.tha23rd.discordNotifier.discord.Field;
import com.tha23rd.discordNotifier.discord.Webhook;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import static net.runelite.api.WorldType.LEAGUE;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import static net.runelite.api.widgets.WidgetID.LEVEL_UP_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.QUEST_COMPLETED_GROUP_ID;
import net.runelite.api.widgets.WidgetInfo;
import static com.tha23rd.discordNotifier.Utils.parseLevelUpWidget;
import net.runelite.client.ui.DrawManager;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import javax.inject.Inject;
import java.util.List;
import org.json.JSONObject;
import org.pf4j.Extension;

@PluginDescriptor(
	name = "Achievement Discord Notifier",
	description = "Notify discord when you do something awesome",
	type = PluginType.UTILITY
)
@Extension
@Slf4j
public class DiscordNotifierPlugin extends Plugin {

	@Inject
	private ConfigManager configManager;

	@Inject
	private DiscordNotifierConfig config;

	@Inject
	private Client client;

	@Inject
	private DrawManager drawManager;

	private CompletableFuture<java.awt.Image> queuedScreenshot = null;

	private boolean shouldTakeScreenshot;

	@Provides
	DiscordNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordNotifierConfig.class);
	}

	@Override
	public void startUp() throws Exception
	{

	}

	@Override
	public void shutDown() throws Exception
	{

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
				// if we don't meet min level and interval reqs, just return to avoid spamming channel
				if (Integer.parseInt(skillLevelArray.get(1)) < config.minLevel()) {
					return;
				}
				if (Integer.parseInt(skillLevelArray.get(1)) % config.interval() != 0) {
					return;
				}
				// take screenshot
				CompletableFuture<java.awt.Image> screenshotFuture = config.sendScreenshot() ? getScreenshot()
					: CompletableFuture.completedFuture(null);
				screenshotFuture
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
		else if (client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT) != null)
		{
			String text = client.getWidget(WidgetInfo.QUEST_COMPLETED_NAME_TEXT).getText();
			String quest = parseQuestCompletedWidget(text);
			// take screenshot
			CompletableFuture<java.awt.Image> screenshotFuture = config.sendScreenshot() ? getScreenshot()
				: CompletableFuture.completedFuture(null);
			screenshotFuture
				.thenApply(screenshot -> queueQuestCompletionNotification(getPlayerName(), getPlayerIconUrl(), quest)
					.thenCompose(_v -> screenshot != null ? sendScreenshot(getWebhookUrls(), screenshot)
						: CompletableFuture.completedFuture(null)))
				.exceptionally(e ->
				{
					log.error(String.format("onChatMessage (pet) error: %s", e.getMessage()), e);
					return null;
				});
		}
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

		Field skillNameField = new Field();
		skillNameField.setName("Skill");
		skillNameField.setValue(getLevelNameString(skill));
		skillNameField.setInline(true);

		Field skillLevelField = new Field();
		skillLevelField.setName("Skill Level");
		skillLevelField.setValue(getLevelNumberString(skillLevel));
		skillLevelField.setInline(true);

		Embed embed = new Embed();
		embed.setAuthor(author);
		embed.setFields(new Field[] { skillNameField, skillLevelField });

		return CompletableFuture.allOf().thenCompose(_v ->
		{
			Webhook webhookData = new Webhook();
			webhookData.setEmbeds(new Embed[] { embed });
			return sendWebhookData(getWebhookUrls(), webhookData);
		});
	}

	private CompletableFuture<Void> queueQuestCompletionNotification(String playerName, String playerIconUrl, String quest)
	{
		Author author = new Author();
		author.setName(playerName);

		if (playerIconUrl != null)
		{
			author.setIcon_url(playerIconUrl);
		}

		Field questNameField = new Field();
		questNameField.setName("Quest");
		questNameField.setValue(getLevelNameString(quest));
		questNameField.setInline(true);

		Embed embed = new Embed();
		embed.setAuthor(author);
		embed.setFields(new Field[] { questNameField });
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

	private String getPlayerIconUrl()
	{
		switch (client.getAccountType())
		{
			case IRONMAN:
				if (client.getWorldType().contains(LEAGUE)) {
					return "https://oldschool.runescape.wiki/w/Trailblazer_League/Tasks#/media/File:Leagues_II_-_Trailblazer_(3).png";
				} else {
					return "https://oldschool.runescape.wiki/images/0/09/Ironman_chat_badge.png";
				}
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

	private String getLevelNumberString(int value)
	{
		return "```fix\n Level: " + NumberFormat.getNumberInstance(Locale.US).format(value) + "\n```";
	}

	private String getLevelNameString(String levelName)
	{
		return "```glsl\n# " + levelName + "\n```";
	}
}