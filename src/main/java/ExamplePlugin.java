import com.google.common.eventbus.Subscribe;
import com.google.inject.Binder;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Example plugin",
	description = "Example on how to have an external plugin support our api additions",
	type = PluginType.EXTERNAL
)
public class ExamplePlugin extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(ExamplePlugin.class);

	@Inject
	Client client;

	@Inject
	ExampleOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		logger.info("Example plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		logger.info("Example plugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{

		}
	}

	@Override
	public void configure(Binder binder)
	{
		binder.bind(ExampleOverlay.class);
	}

	@Provides
	ExamplePluginConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExamplePluginConfiguration.class);
	}

}
