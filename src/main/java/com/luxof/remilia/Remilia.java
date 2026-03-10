package com.luxof.remilia;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Remilia implements ModInitializer {
	public static final String MOD_ID = "remilia";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Sakuya Warudo or something. I've run out of funny shit to say.");
	}
}