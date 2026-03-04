package dev.keii.grasslands.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class GrasslandsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            GrasslandsConfig cfg = GrasslandsConfig.get();
            GrasslandsConfig defaults = new GrasslandsConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("title.grasslands.config"));

            builder.setSavingRunnable(() -> {
                GrasslandsConfig.save();
                GrasslandsConfig.get().computeDerived();
                GrassColorUtil.applyConfig();
                BillboardRenderer.applyConfig();
            });

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(
                    Component.translatable("category.grasslands.general"));

            general.addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("option.grasslands.enableBlades"),
                    cfg.enableBlades)
                    .setDefaultValue(defaults.enableBlades)
                    .setTooltip(Component.translatable("tooltip.grasslands.enableBlades"))
                    .setSaveConsumer(val -> cfg.enableBlades = val)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(
                    Component.translatable("option.grasslands.scanRadius"),
                    cfg.scanRadius, 8, 64)
                    .setDefaultValue(defaults.scanRadius)
                    .setTooltip(Component.translatable("tooltip.grasslands.scanRadius"))
                    .setSaveConsumer(val -> cfg.scanRadius = val)
                    .build());

            ConfigCategory color = builder.getOrCreateCategory(
                    Component.translatable("category.grasslands.color"));

            color.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.colorLighten"),
                    cfg.colorLighten)
                    .setDefaultValue(defaults.colorLighten)
                    .setMin(1.0f).setMax(2.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.colorLighten"))
                    .setSaveConsumer(val -> cfg.colorLighten = val)
                    .build());

            color.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.colorDarken"),
                    cfg.colorDarken)
                    .setDefaultValue(defaults.colorDarken)
                    .setMin(0.0f).setMax(1.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.colorDarken"))
                    .setSaveConsumer(val -> cfg.colorDarken = val)
                    .build());

            color.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.colorNoiseFrequency"),
                    cfg.colorNoiseFrequency)
                    .setDefaultValue(defaults.colorNoiseFrequency)
                    .setMin(0.01f).setMax(5.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.colorNoiseFrequency"))
                    .setSaveConsumer(val -> cfg.colorNoiseFrequency = val)
                    .build());

            color.addEntry(entryBuilder.startIntField(
                    Component.translatable("option.grasslands.colorNoiseSeed"),
                    cfg.colorNoiseSeed)
                    .setDefaultValue(defaults.colorNoiseSeed)
                    .setTooltip(Component.translatable("tooltip.grasslands.colorNoiseSeed"))
                    .setSaveConsumer(val -> cfg.colorNoiseSeed = val)
                    .build());

            ConfigCategory jitter = builder.getOrCreateCategory(
                    Component.translatable("category.grasslands.jitter"));

            jitter.addEntry(entryBuilder.startDoubleField(
                    Component.translatable("option.grasslands.jitterRangePixels"),
                    cfg.jitterRangePixels)
                    .setDefaultValue(defaults.jitterRangePixels)
                    .setMin(0.0).setMax(8.0)
                    .setTooltip(Component.translatable("tooltip.grasslands.jitterRangePixels"))
                    .setSaveConsumer(val -> cfg.jitterRangePixels = val)
                    .build());

            jitter.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.jitterNoiseFrequency"),
                    cfg.jitterNoiseFrequency)
                    .setDefaultValue(defaults.jitterNoiseFrequency)
                    .setMin(0.01f).setMax(10.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.jitterNoiseFrequency"))
                    .setSaveConsumer(val -> cfg.jitterNoiseFrequency = val)
                    .build());

            jitter.addEntry(entryBuilder.startIntField(
                    Component.translatable("option.grasslands.jitterNoiseSeed"),
                    cfg.jitterNoiseSeed)
                    .setDefaultValue(defaults.jitterNoiseSeed)
                    .setTooltip(Component.translatable("tooltip.grasslands.jitterNoiseSeed"))
                    .setSaveConsumer(val -> cfg.jitterNoiseSeed = val)
                    .build());

            ConfigCategory wind = builder.getOrCreateCategory(
                    Component.translatable("category.grasslands.wind"));

            wind.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.windStrengthPixels"),
                    cfg.windStrengthPixels)
                    .setDefaultValue(defaults.windStrengthPixels)
                    .setMin(0.0f).setMax(16.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.windStrengthPixels"))
                    .setSaveConsumer(val -> cfg.windStrengthPixels = val)
                    .build());

            wind.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.windSpeed"),
                    cfg.windSpeed)
                    .setDefaultValue(defaults.windSpeed)
                    .setMin(0.0f).setMax(5.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.windSpeed"))
                    .setSaveConsumer(val -> cfg.windSpeed = val)
                    .build());

            wind.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.windNoiseFrequency"),
                    cfg.windNoiseFrequency)
                    .setDefaultValue(defaults.windNoiseFrequency)
                    .setMin(0.01f).setMax(5.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.windNoiseFrequency"))
                    .setSaveConsumer(val -> cfg.windNoiseFrequency = val)
                    .build());

            wind.addEntry(entryBuilder.startIntField(
                    Component.translatable("option.grasslands.windNoiseSeed"),
                    cfg.windNoiseSeed)
                    .setDefaultValue(defaults.windNoiseSeed)
                    .setTooltip(Component.translatable("tooltip.grasslands.windNoiseSeed"))
                    .setSaveConsumer(val -> cfg.windNoiseSeed = val)
                    .build());

            ConfigCategory heights = builder.getOrCreateCategory(
                    Component.translatable("category.grasslands.heights"));

            heights.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.heightBoostNormalPixels"),
                    cfg.heightBoostNormalPixels)
                    .setDefaultValue(defaults.heightBoostNormalPixels)
                    .setMin(0.0f).setMax(16.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.heightBoostNormalPixels"))
                    .setSaveConsumer(val -> cfg.heightBoostNormalPixels = val)
                    .build());

            heights.addEntry(entryBuilder.startFloatField(
                    Component.translatable("option.grasslands.heightBoostDarkerPixels"),
                    cfg.heightBoostDarkerPixels)
                    .setDefaultValue(defaults.heightBoostDarkerPixels)
                    .setMin(0.0f).setMax(16.0f)
                    .setTooltip(Component.translatable("tooltip.grasslands.heightBoostDarkerPixels"))
                    .setSaveConsumer(val -> cfg.heightBoostDarkerPixels = val)
                    .build());

            return builder.build();
        };
    }
}
