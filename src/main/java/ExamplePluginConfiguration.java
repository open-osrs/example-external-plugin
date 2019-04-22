import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExamplePluginConfiguration extends Config
{
	@ConfigItem(
		keyName = "enabled",
		name = "Enable overlay",
		description = "Configures whether the overlay is enabled"
	)
	default boolean enabled()
	{
		return true;
	}
}
