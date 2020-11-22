/*
 * Copyright (c) 2020 ImNoOSRS <https://github.com/ImNoOSRS>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *	list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	this list of conditions and the following disclaimer in the documentation
 *	and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tha23rd.discordNotifier;

import net.runelite.client.config.*;

@ConfigGroup("discordNotifier")

public interface DiscordNotifierConfig extends Config {

	@ConfigItem(
		keyName = "webhookurl",
		name = "Discord webhook URL(s)",
		description = "The Discord Webhook URL(s) to use, separated by newline"
	)
	default String webhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "minlevel",
		name = "Min Level",
		description = "The lowest level to start sending notifications at"
	)
	default int minLevel()
	{
		return 34;
	}

	@ConfigItem(
		keyName = "interval",
		name = "Level Interval",
		description = "The interval at which to send notifications"
	)
	default int interval()
	{
		return 1;
	}

	@ConfigItem(keyName = "sendscreenshot", name = "Send Screenshot", description = "Whether to send a screenshot")
	default boolean sendScreenshot()
	{
		return true;
	}

}