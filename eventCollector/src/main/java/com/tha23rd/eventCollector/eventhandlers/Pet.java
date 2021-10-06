package com.tha23rd.eventCollector.eventhandlers;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Pet
{
	// GWD Pets
	PET_GENERAL_GRAARDOR(ItemID.PET_GENERAL_GRAARDOR, BossTab.GENERAL_GRAARDOR.getName()),
	PET_KREEARRA(ItemID.PET_KREEARRA, BossTab.KREEARRA.getName()),
	PET_KRIL_TSUTSAROTH(ItemID.PET_KRIL_TSUTSAROTH, BossTab.KRIL_TSUTSAROTH.getName()),
	PET_ZILYANA(ItemID.PET_ZILYANA, BossTab.COMMANDER_ZILYANA.getName()),
	// Wildy Pets
	CALLISTO_CUB(ItemID.CALLISTO_CUB, BossTab.CALLISTO.getName()),
	PET_CHAOS_ELEMENTAL(ItemID.PET_CHAOS_ELEMENTAL, BossTab.CHAOS_ELEMENTAL.getName(), BossTab.CHAOS_FANATIC.getName()),
	SCORPIAS_OFFSPRING(ItemID.SCORPIAS_OFFSPRING, BossTab.SCORPIA.getName()),
	VENENATIS_SPIDERLING(ItemID.VENENATIS_SPIDERLING, BossTab.VENENATIS.getName()),
	VETION_JR(ItemID.VETION_JR, BossTab.VETION.getName()),
	// KBD isn't really in wildy but meh
	PRINCE_BLACK_DRAGON(ItemID.PRINCE_BLACK_DRAGON, BossTab.KING_BLACK_DRAGON.getName()),
	// Slayer Pets
	ABYSSAL_ORPHAN(ItemID.ABYSSAL_ORPHAN, BossTab.ABYSSAL_SIRE.getName()),
	HELLPUPPY(ItemID.HELLPUPPY, BossTab.CERBERUS.getName()),
	NOON(ItemID.NOON, BossTab.GROTESQUE_GUARDIANS.getName()),
	PET_KRAKEN(ItemID.PET_KRAKEN, BossTab.KRAKEN.getName()),
	PET_SMOKE_DEVIL(ItemID.PET_SMOKE_DEVIL, BossTab.THERMONUCLEAR_SMOKE_DEVIL.getName()),
	SKOTOS(ItemID.SKOTOS, BossTab.SKOTIZO.getName()),
	// Other Bosses
	BABY_MOLE(ItemID.BABY_MOLE, BossTab.GIANT_MOLE.getName()),
	KALPHITE_PRINCESS(ItemID.KALPHITE_PRINCESS, BossTab.KALPHITE_QUEEN.getName()),
	OLMLET(ItemID.OLMLET, BossTab.CHAMBERS_OF_XERIC.getName()),
	LIL_ZIK(ItemID.LIL_ZIK, BossTab.THEATRE_OF_BLOOD.getName()),
	PET_DARK_CORE(ItemID.PET_DARK_CORE, BossTab.CORPOREAL_BEAST.getName()),
	PET_SNAKELING(ItemID.PET_SNAKELING, BossTab.ZULRAH.getName()),
	PET_DAGANNOTH_REX(ItemID.PET_DAGANNOTH_REX, BossTab.DAGANNOTH_REX.getName()),
	PET_DAGANNOTH_PRIME(ItemID.PET_DAGANNOTH_PRIME, BossTab.DAGANNOTH_PRIME.getName()),
	PET_DAGANNOTH_SUPREME(ItemID.PET_DAGANNOTH_SUPREME, BossTab.DAGANNOTH_SUPREME.getName()),
	VORKI(ItemID.VORKI, BossTab.VORKATH.getName()),
	BLOODHOUND(ItemID.BLOODHOUND, BossTab.CLUE_SCROLL_MASTER.getName()),
	IKKLE_HYDRA(ItemID.IKKLE_HYDRA, BossTab.ALCHEMICAL_HYDRA.getName()),
	YOUNGLLEF(ItemID.YOUNGLLEF, BossTab.THE_GAUNTLET.getName()),
	SRARACHA(ItemID.SRARACHA, BossTab.SARACHNIS.getName()),
	SMOLCANO(ItemID.SMOLCANO, BossTab.ZALCANO.getName()),
	LITTLE_NIGHTMARE(ItemID.LITTLE_NIGHTMARE, BossTab.NIGHTMARE.getName(), BossTab.PHOSANIS_NIGHTMARE.getName()),
	HERBI(ItemID.HERBI, "Herbiboar"),
	// Pretty sure
	PHOENIX(ItemID.PHOENIX, BossTab.WINTERTODT.getName()),
	PET_PENANCE_QUEEN(ItemID.PET_PENANCE_QUEEN, "Barbarian Assault"),
	TINY_TEMPOR(ItemID.TINY_TEMPOR, BossTab.TEMPOROSS.getName());

	private static final ImmutableMap<String, Pet> BOSS_MAP;

	static
	{
		final ImmutableMap.Builder<String, Pet> byName = ImmutableMap.builder();
		for (final Pet pet : values())
		{
			for (final String bossName : pet.getBossNames())
			{
				byName.put(bossName.toUpperCase(), pet);
			}
		}

		BOSS_MAP = byName.build();
	}

	private final int petID;
	private final String[] bossNames;

	Pet(final int id, final String... bossNames)
	{
		this.petID = id;
		this.bossNames = bossNames;
	}

	public static Pet getByBossName(final String name)
	{
		return BOSS_MAP.get(name.toUpperCase());
	}
}