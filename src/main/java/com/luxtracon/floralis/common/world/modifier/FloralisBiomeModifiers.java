package com.luxtracon.floralis.common.world.modifier;

import com.luxtracon.floralis.Floralis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FloralisBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Floralis.MODID);

    public static RegistryObject<Codec<FloralisVegetalBiomeModifier>> VEGETAL_MODIFIERS = BIOME_MODIFIERS.register("vegetal", () -> RecordCodecBuilder.create(pBuilder -> pBuilder.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter(FloralisVegetalBiomeModifier::pSet), PlacedFeature.CODEC.fieldOf("feature").forGetter(FloralisVegetalBiomeModifier::pHolder)).apply(pBuilder, FloralisVegetalBiomeModifier::new)));

    public static void register(IEventBus eventBus) {
        BIOME_MODIFIERS.register(eventBus);
    }
}
