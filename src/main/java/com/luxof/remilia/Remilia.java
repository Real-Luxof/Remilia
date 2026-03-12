package com.luxof.remilia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.Identifier;
import vazkii.patchouli.api.PatchouliAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Remilia implements ModInitializer {
	public static final String MOD_ID = "remilia";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Not the bees? Why not?");

		RemiliaLoader.registerServer();

		PatchouliAPI.get().setConfigFlag(
			"remilia:devenv",
			FabricLoader.getInstance().isDevelopmentEnvironment()
		);
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }
}