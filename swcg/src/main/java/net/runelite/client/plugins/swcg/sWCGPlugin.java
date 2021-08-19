package net.runelite.client.plugins.swcg;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.callback.ClientThread;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.runelite.api.ItemID.DRAGON_AXE;
import static net.runelite.client.plugins.iutils.iUtils.iterating;
import static net.runelite.client.plugins.swcg.sWCGState.*;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "sWoocutting Guild",
        enabledByDefault = false,
        description = "Samler - Woodcutting Guild",
        tags = {"samler", "woodcutting", "bot"}
)

@Slf4j
public class sWCGPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private sWCGConfig config;

    @Inject
    private iUtils utils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private BankUtils bank;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private ObjectUtils object;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private sWCGOverlay overlay;

    @Inject
    private ReflectBreakHandler chinBreakHandler;

    @Inject
    private ClientThread clientThread;

    MenuEntry targetMenu;
    Instant botTimer;
    Player player;
    sWCGState state;
    sWCGState axeState;
    sWCGState inventoryState;

    LocalPoint beforeLoc = new LocalPoint(0, 0);
    GameObject bankChest;
    GameObject yewTree;
    Widget bankItem;
    WidgetItem useableItem;

    Set<Integer> AXE = Set.of(ItemID.IRON_AXE, ItemID.STEEL_AXE, ItemID.BLACK_AXE, ItemID.MITHRIL_AXE, ItemID.ADAMANT_AXE, ItemID.RUNE_AXE, ItemID.DRAGON_AXE,
            ItemID.DRAGON_AXE_OR, ItemID.CRYSTAL_AXE, ItemID.INFERNAL_AXE, ItemID.INFERNAL_AXE_OR);
    Set<Integer> SPEC_AXE = Set.of(ItemID.DRAGON_AXE, ItemID.DRAGON_AXE_OR, ItemID.CRYSTAL_AXE, ItemID.INFERNAL_AXE, ItemID.INFERNAL_AXE_OR);
    List<Integer> REQUIRED_ITEMS = new ArrayList<>();

    boolean startBot;
    boolean setTree;
    boolean noAxe;
    long sleepLength;
    int tickLength;
    int timeout;
    int totalLogsChopped;
    int treeID;
    TreeTypes treeTypeID;
    int axeTypeID;


    @Provides
    sWCGConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(sWCGConfig.class);
    }

    @Override
    protected void startUp() {
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        resetVals();
        chinBreakHandler.unregisterPlugin(this);
    }

    private void resetVals() {
        log.info("stopping sWCGuild");
        chinBreakHandler.stopPlugin(this);
        startBot = false;
        botTimer = null;
        overlayManager.remove(overlay);
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("sWCGuild")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                startBot = true;
                chinBreakHandler.startPlugin(this);
                botTimer = Instant.now();
                initCounters();
                state = null;
                axeState = null;
                targetMenu = null;
                treeTypeID = config.getTreeType();
                axeTypeID = config.getAxe().getId();
                REQUIRED_ITEMS = List.of(axeTypeID);
                //updatePrices();
                botTimer = Instant.now();
                overlayManager.add(overlay);
            } else {
                resetVals();
            }
        }
    }

    @Subscribe
    private void onConfigChange(ConfigChanged event) {
        if (!event.getGroup().equals("sWCGuild")) {
            return;
        }
        switch (event.getKey()) {
            case "getAxe":
                axeTypeID = config.getAxe().getId();
                break;
            case "getTreeType":
                treeTypeID = config.getTreeType();
                break;
        }
        REQUIRED_ITEMS = List.of(axeTypeID);
        //updatePrices();
    }

    private void initCounters() {
        timeout = 0;
        //runesPH = 0;
        //profitPH = 0;
        //totalProfit = 0;
        totalLogsChopped = 0;
    }

    //private void updatePrices

    private int itemTotals(int itemID, int beforeAmount, boolean stackableItem) {
        int currentAmount = inventory.getItemCount(itemID, stackableItem);
        return (beforeAmount > currentAmount) ? beforeAmount - currentAmount : 0;
    }

    //private void updatetotals

    //public void updatestats

    private long sleepDelay() {
        sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int shortDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), 1, 3, 1, 2);
        log.info("tick delay for {} ticks", tickLength);
        return tickLength;
    }
    private int tickDelay() {
        tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.info("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    private sWCGState getItemState(Set<Integer> itemIDs) {
        if (inventory.containsItem(itemIDs)) {
            useableItem = inventory.getWidgetItem(itemIDs);
            return OPEN_BANK;
        }
        return OUT_OF_ITEM;
    }

    private sWCGState getRequiredItemState() {
        if ((!inventory.containsItem(axeTypeID))) {
            bankItem = null;
            return OPEN_BANK;
        }
        return OUT_OF_ITEM;
    }

    private sWCGState getState() {
        if (timeout > 0) {
            playerUtils.handleRun(20, 30);
            return TIMEOUT;
        }
        if (iterating) {
            return ITERATING;
        }
        if (playerUtils.isMoving(beforeLoc))
        {
            playerUtils.handleRun(20, 30);
            return MOVING;
        }
        if (player.getAnimation() == 2846 || player.getAnimation() == 867) //chopping animation
        {
            return CHOPPING;
        }
        if (!playerUtils.isItemEquipped(AXE)) {
            utils.sendGameMessage("Axe not equipped. Stopping.");
            return OUT_OF_ITEM;
        }
        if (chinBreakHandler.shouldBreak(this)) {
            return HANDLE_BREAK;
        }

        yewTree = object.findNearestGameObject(config.getTreeType().getTreeID()); //Yew Tree
        bankChest = object.findNearestGameObject(ObjectID.BANK_CHEST_28861);

        if (yewTree != null) {
            if (inventory.isEmpty()) {
                return CHOP_TREE;
            }
        }
        if (playerUtils.isItemEquipped(SPEC_AXE) && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) {
            return USE_SPEC;
        }
        if (inventory.isFull() && (bankChest != null)) {
            if (!bank.isOpen()) {
                return OPEN_BANK;
            }
            if (bank.isOpen()) {
                if (inventory.isFull())  {
                    return DEPOSIT_ALL;
                }
                if (inventory.containsExcept(REQUIRED_ITEMS)) {
                    return DEPOSIT_ALL_EXCEPT;
                }
                return OPEN_BANK;
            }
        }
        return CHOP_TREE;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startBot || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if (!client.isResized()) {
                utils.sendGameMessage("sWCGuild - client must be set to resizable.");
                startBot = false;
                return;
            }
            state = getState();
            log.debug(state.name());
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case ITERATING:
                    break;
                case MOVING:
                    timeout = tickDelay();
                    break;
                case CHOPPING:
                    timeout = tickDelay();
                    break;
                case CHOP_TREE:
                    utils.doGameObjectActionMsTime(yewTree, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                    timeout = tickDelay();
                    break;
                case OPEN_BANK:
                    utils.doGameObjectActionMsTime(bankChest, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                    timeout = tickDelay();
                    break;
                case DEPOSIT_ALL:
                    utils.doGameObjectActionMsTime(bankChest, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
                    timeout = shortDelay();
                    bank.depositAll();
                    sleepDelay();
                    timeout = shortDelay();;
                    break;
                case DEPOSIT_ALL_EXCEPT:
                    bank.depositAllExcept(REQUIRED_ITEMS);
                    break;
                case WITHDRAW_ITEM:
                    bank.withdrawItem(bankItem);
                    break;
                case WITHDRAW_ALL_ITEM:
                    bank.withdrawAllItem(bankItem);
                    break;
                case HANDLE_BREAK:
                    chinBreakHandler.startBreak(this);
                    timeout = 10;
                    break;
                case OUT_OF_ITEM:
                    utils.sendGameMessage("Out of required items.");
                    startBot = false;
                    resetVals();
                    break;
                case USE_SPEC:
                    timeout = tickDelay();
                    clientThread.invoke(() -> client.invokeMenuAction("<col=ff9040>Special Attack</col>", "",
                            1, MenuAction.CC_OP.getId(), -1, 38862884));
                    timeout = shortDelay();
                    break;
            }
            beforeLoc = player.getLocalLocation();
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (!startBot) {
            return;
        }
        if (event.getGameState() == GameState.LOGGED_IN) {
            state = TIMEOUT;
            timeout = 2;
        }
    }
}