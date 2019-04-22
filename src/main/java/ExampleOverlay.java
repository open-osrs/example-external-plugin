import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

public class ExampleOverlay extends Overlay
{
	private final Client client;
	private final ExamplePluginConfiguration config;

	@Inject
	public ExampleOverlay(@Nullable Client client, ExamplePluginConfiguration config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics2D) {
		if (client.getGameState() != GameState.LOGGED_IN || !config.enabled())
		{
			return null;
		}

		Player player = client.getLocalPlayer();
		String text = player.getName() + " (Level: " + player.getCombatLevel() + ")";

		Point textLocation = player.getCanvasTextLocation(graphics2D, text, player.getModelHeight());
		graphics2D.drawString(text, textLocation.getX(), textLocation.getY());

		return null;
	}
}
