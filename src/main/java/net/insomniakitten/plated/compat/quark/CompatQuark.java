package net.insomniakitten.plated.compat.quark;

import net.insomniakitten.plated.client.StateMapperPlated;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder("quark")
public final class CompatQuark {
    public static final Block OBSIDIAN_PRESSURE_PLATE = Blocks.AIR;
    public static final Block SPRUCE_PRESSURE_PLATE = Blocks.AIR;
    public static final Block BIRCH_PRESSURE_PLATE = Blocks.AIR;
    public static final Block JUNGLE_PRESSURE_PLATE = Blocks.AIR;
    public static final Block ACACIA_PRESSURE_PLATE = Blocks.AIR;
    public static final Block DARK_OAK_PRESSURE_PLATE = Blocks.AIR;

    private CompatQuark() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(new BlockPressurePlatePlatedObsidian("obsidian_pressure_plate"));
        registry.register(new BlockPressurePlatePlatedWooden("spruce_pressure_plate"));
        registry.register(new BlockPressurePlatePlatedWooden("birch_pressure_plate"));
        registry.register(new BlockPressurePlatePlatedWooden("jungle_pressure_plate"));
        registry.register(new BlockPressurePlatePlatedWooden("acacia_pressure_plate"));
        registry.register(new BlockPressurePlatePlatedWooden("dark_oak_pressure_plate"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterModels(final ModelRegistryEvent event) {
        StateMapperPlated.registerFor(CompatQuark.OBSIDIAN_PRESSURE_PLATE);
        StateMapperPlated.registerFor(CompatQuark.SPRUCE_PRESSURE_PLATE);
        StateMapperPlated.registerFor(CompatQuark.BIRCH_PRESSURE_PLATE);
        StateMapperPlated.registerFor(CompatQuark.JUNGLE_PRESSURE_PLATE);
        StateMapperPlated.registerFor(CompatQuark.ACACIA_PRESSURE_PLATE);
        StateMapperPlated.registerFor(CompatQuark.DARK_OAK_PRESSURE_PLATE);
    }

}
