
package chronosacaria.mcdw.bases;

import chronosacaria.mcdw.Mcdw;
import chronosacaria.mcdw.api.util.RarityHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class McdwShortBow extends BowItem {

    public final ToolMaterial material;
    public final float drawSpeed;
    public static float maxBowRange;
    private final ParticleEffect type;

    public McdwShortBow(ToolMaterial material, float drawSpeed, float maxBowRangePar) {
        this(material, drawSpeed, maxBowRangePar, null);
    }

    public McdwShortBow(ToolMaterial material, float drawSpeed, float maxBowRangePar, ParticleEffect particles) {
        super(new Item.Settings().group(Mcdw.RANGED)
                .maxCount(1)
                .maxDamage(material.getDurability())
                .rarity(RarityHelper.fromToolMaterial(material))
        );
        this.material = material;
        this.drawSpeed = drawSpeed;
        maxBowRange = maxBowRangePar;
        type = particles;
    }

    public float getDrawSpeed() {
        return Math.max(0, drawSpeed);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        boolean bl = !user.getArrowType(itemStack).isEmpty();
        if (!user.getAbilities().creativeMode && !bl) {
            return TypedActionResult.fail(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public int getRange() {
        return (int) maxBowRange;
    }

    @Override
    public int getEnchantability() {
        return material.getEnchantability();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.material.getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        int i = 1;
        String str = stack.getItem().getTranslationKey().toLowerCase(Locale.ROOT).substring(14);
        String translationKey = String.format("tooltip_info_item.mcdw.%s_", str);
        while (I18n.hasTranslation(translationKey + i)) {
            tooltip.add(Text.translatable(translationKey + i).formatted(Formatting.ITALIC));
            i++;
        }
        tooltip.add(Text.translatable("tooltip_info_item.mcdw.gap").formatted(Formatting.ITALIC));
        tooltip.add(Text.translatable("tooltip_note_item.mcdw.shortbow").formatted(Formatting.GREEN));
    }
}