package net.runelite.client.plugins.swcg;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;

//10822
public enum TreeTypes {
    YEW(ObjectID.YEW),
    MAGIC(ObjectID.MAGIC_TREE_10834);

    @Getter(AccessLevel.PACKAGE)
    public final int treeID;



    TreeTypes(final int treeTypeID) {
        this.treeID = treeTypeID;

    }
}