package net.runelite.client.plugins.swcg;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum AxeTypes {
    BRONZE_AXE("Bronze Axe", ItemID.BRONZE_AXE),
    IRON_AXE("Iron Axe", ItemID.IRON_AXE),
    STEEL_AXE("Steel Axe", ItemID.STEEL_AXE),
    BLACK_AXE("Black Axe", ItemID.BLACK_AXE),
    MITHRIL_AXE("Mithril Axe", ItemID.MITHRIL_AXE),
    ADAMANT_AXE("Adamant Axe", ItemID.ADAMANT_AXE),
    RUNE_AXE("Rune Axe", ItemID.RUNE_AXE),
    DRAGON_AXE("Dragon Axe", ItemID.DRAGON_AXE),
    DRAGON_AXE_OR("Dragon Axe (OR)", ItemID.DRAGON_AXE_OR),
    INFERNAL_AXE("Infernal Axe", ItemID.INFERNAL_AXE),
    INFERNAL_AXE_OR("Infernal Axe", ItemID.INFERNAL_AXE_OR),
    CRYSTAL_AXE("Crystal Axe", ItemID.CRYSTAL_AXE);

    private final String name;
    private final int id;

    AxeTypes(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
