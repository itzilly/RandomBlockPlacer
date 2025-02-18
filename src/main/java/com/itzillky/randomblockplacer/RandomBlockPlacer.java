package com.itzillky.randomblockplacer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.ClickEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Mod(RandomBlockPlacer.MODID)
public class RandomBlockPlacer {
    public static final String MODID = "randomblockplacer";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String CATEGORY = "key.categories.randomblockplacer";
    private static final String TOGGLE_KEY = "key.randomblockplacer.toggle";

    private static final int[] VALID_SLOTS = new int[9];

    private static final KeyMapping TOGGLE_MAPPING = new KeyMapping(
            TOGGLE_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    private static boolean isToggled = false;
    private static final Random random = new Random();

    public RandomBlockPlacer(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() != LogicalSide.CLIENT) return;
        if (!isToggled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        List<Integer> blockSlots = IntStream.range(0, 9)
                .filter(i -> mc.player.getInventory().getItem(i).getItem() instanceof BlockItem)
                .boxed()
                .toList();

        if (blockSlots.isEmpty()) {
            mc.player.displayClientMessage(Component.literal("No blocks found in hotbar!"), false);
            return;
        }

        int randomSlot = blockSlots.get(random.nextInt(blockSlots.size()));
        mc.player.getInventory().selected = randomSlot;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        while (TOGGLE_MAPPING.consumeClick()) {
            isToggled = !isToggled;

            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Random Block Mode: " + (isToggled ? "ON" : "OFF")),
                    true
            );
        }
    }

    @EventBusSubscriber(modid = RandomBlockPlacer.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class KeybindRegistry {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_MAPPING);
        }
    }
}
