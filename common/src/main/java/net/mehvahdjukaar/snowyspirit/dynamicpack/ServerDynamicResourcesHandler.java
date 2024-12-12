package net.mehvahdjukaar.snowyspirit.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;

public class ServerDynamicResourcesHandler extends DynServerResourcesGenerator {

    public static final ServerDynamicResourcesHandler INSTANCE = new ServerDynamicResourcesHandler();

    public ServerDynamicResourcesHandler() {
        super(new DynamicDataPack(SnowySpirit.res("generated_pack")));
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || CommonConfigs.DEBUG_RESOURCES.get());
    }

    @Override
    public Collection<String> additionalNamespaces() {
        return List.of("snowyspirit");
    }

    @Override
    public Logger getLogger() {
        return SnowySpirit.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager resourceManager) {

        SimpleTagBuilder builder = SimpleTagBuilder.of(SnowySpirit.res("sleds"));
        builder.addEntries(ModRegistry.SLED_ITEMS.values());
        dynamicPack.addTag(builder, Registries.ITEM);

        ResourceLocation id = SnowySpirit.res("sled_oak");
        Recipe<?> recipeTemplate = RPUtils.readRecipe(resourceManager, id);

        ModRegistry.SLED_ITEMS.forEach((w, b) -> {
            if (w != WoodTypeRegistry.OAK_TYPE) {
                var newR = RPUtils.makeSimilarRecipe(recipeTemplate, WoodTypeRegistry.OAK_TYPE, w, id);
                this.dynamicPack.addRecipe(newR);
            }
        });
    }


}
