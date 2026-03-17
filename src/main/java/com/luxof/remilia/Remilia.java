package com.luxof.remilia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.util.TriConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vazkii.patchouli.api.PatchouliAPI;

public class Remilia implements ModInitializer {
	public static final String MOD_ID = "remilia";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Not the bees? What did they ever do to you?");

		RemiliaLoader.registerServer();

		PatchouliAPI.get().setConfigFlag(
			"remilia:devenv",
			FabricLoader.getInstance().isDevelopmentEnvironment()
		);

        RemiliaServer.initializeServerSideStuff();
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }

	
    private static boolean sameAsItWasBefore(
		HashMap<UUID, HashMap<String, Object>> prevShared,
		UUID uuid,
		String varName,
		Object value
	) {
        var variables = prevShared.get(uuid);
        if (variables == null)
            return false;
        return variables.get(varName).equals(value);
    }

	private static void put(
        HashMap<UUID, HashMap<String, Object>> yes,
        UUID uuid,
        String varName,
        Object value
    ) {
        if (!yes.containsKey(uuid))
            yes.put(uuid, new HashMap<>());
        yes.get(uuid).put(varName, value);
    }

	public static void shareVars(
		HashMap<UUID, HashMap<String, Object>> prevShared,
		TriConsumer<UUID, Identifier, PacketByteBuf> sendPacket,
		BiConsumer<Identifier, PacketByteBuf> sendPacketToNull
	) {
        HashMap<UUID, HashMap<String, Object>> changed = new HashMap<>();
        var nowShared = RemiliaAPI.Sharing.shared;
        var ours = RemiliaAPI.Sharing.ours;

        for (var entry : nowShared.entrySet()) {
            UUID uuid = entry.getKey();
            for (var variable : entry.getValue().entrySet()) {
                String varName = variable.getKey();
                Object value = variable.getValue().get();

                if (!sameAsItWasBefore(prevShared, uuid, varName, value)) {
                    put(changed, uuid, varName, value);
                    put(prevShared, uuid, varName, value);
                }
            }
        }

        List<UUID> deletedUuids = new ArrayList<>();
        HashMap<UUID, List<String>> deletedVars = new HashMap<>();
        for (var entry : prevShared.entrySet()) {
            UUID uuid = entry.getKey();

            if (!ours.containsKey(uuid)) {
                deletedUuids.add(uuid);
                prevShared.remove(uuid);
                continue;
            }

            var nowVars = nowShared.get(uuid);
            for (String varName : ours.get(uuid)) {
                if (nowVars.containsKey(varName)) continue;

                if (!deletedVars.containsKey(uuid))
                    deletedVars.put(uuid, new ArrayList<>());
                deletedVars.get(uuid).add(varName);

                prevShared.get(uuid).remove(varName);
            }
        }

        if (changed.isEmpty() && deletedUuids.isEmpty() && deletedVars.isEmpty()) return;

        for (UUID uuid : deletedUuids) {
            if (uuid == null) {
				sendPacketToNull.accept(
					id("you_just_got_deleted_buddy"),
					PacketByteBufs.empty()
				);
                continue;
            }
			sendPacket.accept(
				uuid,
                id("you_just_got_deleted_buddy"),
                PacketByteBufs.empty()
			);
        }
        
        for (var entry : deletedVars.entrySet()) {
            UUID uuid = entry.getKey();

            PacketByteBuf packet = PacketByteBufs.create();
            packet.writeCollection(entry.getValue(), (buf, str) -> buf.writeString(str));

            if (uuid == null) {
				sendPacketToNull.accept(
					id("your_global_vars_just_got_deleted_buddy"),
					packet
				);
                continue;
            }

            sendPacket.accept(
                uuid,
                id("your_vars_just_got_deleted_buddy"),
                packet
            );
        }

        for (var entry : changed.entrySet()) {
            UUID uuid = entry.getKey();
            String UUIDname = uuid == null ? "null UUID" : uuid.toString();

            PacketByteBuf packet = PacketByteBufs.create();

            for (var variable : entry.getValue().entrySet()) {
                packet.writeString(variable.getKey());

                Object value = variable.getValue();
                if (value instanceof Integer integ) {
                    packet.writeInt(0);
                    packet.writeInt(integ);
                } else if (value instanceof Double dub) {
                    packet.writeInt(1);
                    packet.writeDouble(dub);
                } else if (value instanceof Boolean bool) {
                    packet.writeInt(2);
                    packet.writeBoolean(bool);
                } else if (value instanceof String str) {
                    packet.writeInt(3);
                    packet.writeString(str);
                } else {
                    LOGGER.error("Tried to sync variable " + variable.getKey() + " to " + UUIDname + " but failed as it was an unsupported type!");
                }
            }

            if (uuid == null) {
				sendPacketToNull.accept(
					id("changed_global_vars"),
					packet
				);
                continue;
            }

            sendPacket.accept(
                uuid,
                id("changed_vars"),
                packet
            );
        }
	}
    

    public static void readAndExecuteThisOnVars(
        PacketByteBuf packet,
        BiConsumer<String, Object> execute
    ) {
        while (packet.isReadable()) {
            execute.accept(
                packet.readString(),
                switch (packet.readInt()) {
                    case 0 -> packet.readInt();
                    case 1 -> packet.readDouble();
                    case 2 -> packet.readBoolean();
                    case 3 -> packet.readString();
                    default -> null;
                }
            );
        }
    }
}