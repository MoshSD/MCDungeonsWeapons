package chronosacaria.mcdw.client;

import chronosacaria.mcdw.bases.*;
import chronosacaria.mcdw.blocks.BlocksInit;
import chronosacaria.mcdw.blocks.McdwTempCobwebBlock;
import chronosacaria.mcdw.enchants.summons.registry.SummonedEntityRegistry;
import chronosacaria.mcdw.enchants.summons.render.SummonedBeeRenderer;
import chronosacaria.mcdw.enums.*;
import chronosacaria.mcdw.items.ItemsInit;
import com.sun.jna.platform.unix.X11;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class McdwClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(SummonedEntityRegistry.SUMMONED_BEE_ENTITY, SummonedBeeRenderer::new);

        for (BowsID itemID : BowsID.values()) {
            registerBowPredicates(ItemsInit.bowItems.get(itemID));
        }

        for (ShortBowsID itemID : ShortBowsID.values()) {
            registerShortBowPredicates(ItemsInit.shortBowItems.get(itemID));
        }

        for (LongBowsID itemID : LongBowsID.values()) {
            registerLongBowPredicates(ItemsInit.longBowItems.get(itemID));
        }

        for (CrossbowsID itemID : CrossbowsID.values()) {
            registerCrossbowPredicates(ItemsInit.crossbowItems.get(itemID));
        }

        for (ShieldsID itemID : ShieldsID.values()){
            registerShieldPredicates(ItemsInit.shieldItems.get(itemID));
        }

        BlockRenderLayerMap.INSTANCE.putBlock(BlocksInit.TEMP_COBWEB_BLOCK, RenderLayer.getCutout());

    }

    public static void registerBowPredicates(McdwBow bow) {
        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pull"),(itemStack, clientWorld,
                                                                                   livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return livingEntity.getActiveItem() != itemStack ? 0.0F : (float)(itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / bow.getDrawSpeed();
            }
        });

        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pulling"), (itemStack, clientWorld,
                                                                                       livingEntity, seed) ->
                livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F);
    }

    public static void registerShortBowPredicates(McdwShortBow bow) {
        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pull"),(itemStack, clientWorld,
                                                                                   livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return livingEntity.getActiveItem() != itemStack ? 0.0F : (float)(itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / bow.getDrawSpeed();
            }
        });

        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pulling"), (itemStack, clientWorld,
                                                                                       livingEntity, seed) ->
                livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F);
    }

    public static void registerLongBowPredicates(McdwLongBow bow) {
        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pull"),(itemStack, clientWorld,
                                                                                   livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return livingEntity.getActiveItem() != itemStack ? 0.0F : (float)(itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / bow.getDrawSpeed();
            }
        });

        FabricModelPredicateProviderRegistry.register(bow, new Identifier("pulling"), (itemStack, clientWorld,
                                                                                       livingEntity, seed) ->
                livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F);
    }

    public static void registerCrossbowPredicates(McdwCrossbow crossbow) {
        FabricModelPredicateProviderRegistry.register(crossbow, new Identifier("pull"),(itemStack, clientWorld,
                                                                                        livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return McdwCrossbow.isCharged(itemStack) ? 0.0F :
                        (float) (itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / (float)  McdwCrossbow.getPullTime(itemStack);
            }
        });

        FabricModelPredicateProviderRegistry.register(crossbow, new Identifier("pulling"), (itemStack, clientWorld,
                                                                                            livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack && !McdwCrossbow.isCharged(itemStack) ? 1.0F : 0.0F;
            }
        });

        FabricModelPredicateProviderRegistry.register(crossbow, new Identifier("charged"), (itemStack, clientWorld,
                                                                                            livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return McdwCrossbow.isCharged(itemStack) ? 1.0F : 0.0F;
            }
        });

        FabricModelPredicateProviderRegistry.register(crossbow, new Identifier("firework"), (itemStack, clientWorld,
                                                                                             livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else {
                return McdwCrossbow.isCharged(itemStack) && McdwCrossbow.hasProjectile(itemStack,
                        Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
            }
        });
    }

    public static void registerShieldPredicates(McdwShield shield){
        FabricModelPredicateProviderRegistry.register(shield, new Identifier("blocking"), (itemStack, clientWorld,
                livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem()
                == itemStack ? 1.0F : 0.0F );
    }
}
