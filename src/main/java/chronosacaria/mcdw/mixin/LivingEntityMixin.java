package chronosacaria.mcdw.mixin;

import chronosacaria.mcdw.enchants.EnchantsRegistry;
import chronosacaria.mcdw.enchants.summons.entity.SummonedBeeEntity;
import chronosacaria.mcdw.enchants.summons.registry.SummonedEntityRegistry;
import chronosacaria.mcdw.enchants.util.AOECloudHelper;
import chronosacaria.mcdw.enchants.util.AOEHelper;
import chronosacaria.mcdw.items.ItemRegistry;
import chronosacaria.mcdw.sounds.McdwSoundEvents;
import chronosacaria.mcdw.statuseffects.StatusEffectsRegistry;
import chronosacaria.mcdw.weapons.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    @Final
    private DefaultedList<ItemStack> equippedHand;

    @Shadow
    public abstract ItemStack getOffHandStack();

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);


    @Shadow
    public abstract boolean removeStatusEffect(StatusEffect type);

    @Shadow
    public abstract void onDeath(DamageSource source);

    @Shadow private @Nullable LivingEntity attacker;
    @Shadow protected float lastDamageTaken;
    @Shadow @Nullable protected PlayerEntity attackingPlayer;

    @Shadow public abstract float getHealth();

    @Shadow public abstract float getMaxHealth();

    @Shadow protected abstract void spawnItemParticles(ItemStack stack, int count);

    @Shadow protected abstract int getCurrentExperience(PlayerEntity player);


    /* * * * * * * * * * * * * * * * * * * *|
    |**** ENCHANTMENTS -- ANIMA CONDUIT ****|
    |* * * * * * * * * * * * * * * * * * * */

    @Inject(
            at = @At("HEAD"),
            method = "onDeath",
            cancellable = true)

    private void onAnimaConduitKill(DamageSource source, CallbackInfo ci) {
        LivingEntity user = (LivingEntity)source.getAttacker();

        ItemStack mainHandStack = null;
        if (user != null) {
            mainHandStack = user.getMainHandStack();
        }

        if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.ANIMA_CONDUIT, mainHandStack) >= 1)) {
            int level = EnchantmentHelper.getLevel(EnchantsRegistry.ANIMA_CONDUIT, mainHandStack);
            float healthRegained;

            //ANIMA CONDUIT AS PER KILL
            if (user.getHealth() < user.getMaxHealth()) {
                healthRegained = (float)(getCurrentExperience((PlayerEntity)user)*(0.2*level));
                user.heal(healthRegained);
                ((PlayerEntity)user).addExperienceLevels(-999999999);
            }
        }
    }


    /* * * * * * * * * * * * * * * * * |
    |**** ENCHANTMENTS -- BUSY BEE ****|
    | * * * * * * * * * * * * * * * * */

    public EntityType<SummonedBeeEntity> s_bee =
            SummonedEntityRegistry.SUMMONED_BEE_ENTITY;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // -- For summoning Bee with Bee Stinger Item
    @Inject(
            at = @At("HEAD"),
            method = "swingHand(Lnet/minecraft/util/Hand;)V",
            cancellable = true)

    private void swingHand(Hand hand, CallbackInfo ci) {
        ItemStack mainHandStack = equippedHand.get(0);
        ItemStack offHandStack = getOffHandStack();
        if (mainHandStack.getItem() == Rapiers.SWORD_BEESTINGER && offHandStack.getItem() == ItemRegistry.BEE_STINGER_ITEM) {
            SummonedBeeEntity summonedBeeEntity_1 = s_bee.create(world);
            summonedBeeEntity_1.setSummoner(this);
            summonedBeeEntity_1.refreshPositionAndAngles(this.getX(), this.getY() + 1, this.getZ(), 0, 0);
            world.spawnEntity(summonedBeeEntity_1);
        }
        if ((offHandStack.getItem() == ItemRegistry.BEE_STINGER_ITEM && (mainHandStack.getItem() == Rapiers.SWORD_BEESTINGER))) {
            offHandStack.decrement(1);
        }
    } //END BUSY BEE ENCHANTMENT

    /* * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- CHAINS *****|
    | * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- COMMITTED *****|
    |* * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- CRITICAL HIT *****|
    | * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- ECHO *****|
    | * * * * * * * * * * * * * * * */
    @Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("RETURN"))
    public void applyDamage(DamageSource source, float damage, CallbackInfo info) {
        LivingEntity user = (LivingEntity)source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;

        if (source.getSource() instanceof LivingEntity && target.getHealth() < damage) {
            if (!target.isInvulnerableTo(source)) {
                ItemStack mainHandStack = null;
                if (user != null) {
                    mainHandStack = user.getMainHandStack();
                }
                boolean uniqueWeaponFlag =
                        false;
                if (mainHandStack != null) {
                    uniqueWeaponFlag = mainHandStack.getItem() == Spears.SPEAR_WHISPERING_SPEAR.asItem();
                }

                if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.ECHO, mainHandStack) >= 1 || uniqueWeaponFlag)) {
                    int level = EnchantmentHelper.getLevel(EnchantsRegistry.ECHO, mainHandStack);

                    float attackDamage = (float)user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    float cooledAttackStrength = 0.5F;
                    attackDamage *= 0.2F + cooledAttackStrength * cooledAttackStrength * 0.8F;
                    float chance = user.getRandom().nextFloat();
                    if (chance <= 1/* * level*/) {
                        AOEHelper.causeEchoAttack(user, target, attackDamage, 3.0f, level);
                        user.world.playSound(
                                (PlayerEntity) null,
                                user.getX(),
                                user.getY(),
                                user.getZ(),
                                McdwSoundEvents.ECHO_SOUND_EVENT,
                                SoundCategory.PLAYERS,
                                0.5F,
                                1.0F);
                    }
                }
            }
        }
    }


    /* * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- EXPLODING *****|
    |* * * * * * * * * * * * * * * * * * */

    @Inject(
            at = @At("HEAD"),
            method = "onDeath",
            cancellable = true)

    private void onExplodingKill(DamageSource source, CallbackInfo ci) {
        LivingEntity user = (LivingEntity)source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;
        ItemStack mainHandStack = null;
        if (user != null) {
            mainHandStack = user.getMainHandStack();
        }
        boolean uniqueWeaponFlag =
                false;
        if (mainHandStack != null) {
            uniqueWeaponFlag = mainHandStack.getItem() == DoubleAxes.AXE_CURSED.asItem()
                    || mainHandStack.getItem() == Staves.STAFF_BATTLESTAFF_OF_TERROR.asItem();
        }

        if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.EXPLODING, mainHandStack) >= 1 || uniqueWeaponFlag)) {
            int level = EnchantmentHelper.getLevel(EnchantsRegistry.EXPLODING, mainHandStack);
            float explodingDamage;
            explodingDamage = target.getMaxHealth() * 0.2f * level;
            float chance = user.getRandom().nextFloat();
            if (chance <= 0.2) {
                if (uniqueWeaponFlag) explodingDamage += (target.getMaxHealth() * 0.2F);
                target.world.playSound(
                        (PlayerEntity) null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F);
                AOECloudHelper.spawnExplosionCloud(user, target, 3.0F);
                AOEHelper.causeExplosionAttack(user, target, explodingDamage, 3.0F);
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- FREEZING *****|
    | * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- GRAVITY *****|
    |* * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- LEECHING *****|
    | * * * * * * * * * * * * * * * * * */

    @Inject(
            at = @At("HEAD"),
            method = "onDeath",
            cancellable = true)

    private void onLeechingKill(DamageSource source, CallbackInfo ci) {
        LivingEntity user = (LivingEntity)source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;

        ItemStack mainHandStack = null;
        if (user != null) {
            mainHandStack = user.getMainHandStack();
        }
        boolean uniqueWeaponFlag =
                false;
        if (mainHandStack != null) {
            uniqueWeaponFlag = mainHandStack.getItem() == Claymores.SWORD_HEARTSTEALER.asItem();
        }

        if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.LEECHING, mainHandStack) >= 1 || uniqueWeaponFlag)) {
            int level = EnchantmentHelper.getLevel(EnchantsRegistry.LEECHING, mainHandStack);
            float healthRegained;
            assert target != null;
            float targetMaxHealth = target.getMaxHealth();

            //LEECHING AS PER KILL
            if (user.getHealth() < user.getMaxHealth()) {
                healthRegained = (0.2F + 0.2F * level) * targetMaxHealth;
                if (uniqueWeaponFlag) healthRegained += 0.04F * targetMaxHealth;
                user.heal(healthRegained);
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- POISON CLOUD *****|
    | * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- RADIANCE *****|
    | * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- RAMPAGING *****|
    |* * * * * * * * * * * * * * * * * * */
    @Inject(
            at = @At("HEAD"),
            method = "onDeath",
            cancellable = true)

    private void onRampagingKill(DamageSource source, CallbackInfo ci) {
        LivingEntity user = (LivingEntity)source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;
        ItemStack mainHandStack = null;
        if (user != null) {
            mainHandStack = user.getMainHandStack();
        }
        boolean uniqueWeaponFlag =
                false;
        if (mainHandStack != null) {
            uniqueWeaponFlag = mainHandStack.getItem() == Curves.SWORD_DANCERS_SWORD.asItem();
        }

        if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.RAMPAGING, mainHandStack) >= 1)) {
            int level = EnchantmentHelper.getLevel(EnchantsRegistry.RAMPAGING, mainHandStack);
            float rampagingRand = user.getRandom().nextFloat();
            if (rampagingRand <= 0.1F){
                StatusEffectInstance rampage = new StatusEffectInstance(StatusEffects.HASTE, level * 100, 4);
                user.addStatusEffect(rampage);
            }
        }
        if (uniqueWeaponFlag){
            float rampagingRand = user.getRandom().nextFloat();
            if (rampagingRand <= 0.1F){
                StatusEffectInstance rampage = new StatusEffectInstance(StatusEffects.HASTE, 100, 4);
                user.addStatusEffect(rampage);
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- SHOCKWAVE *****|
    |* * * * * * * * * * * * * * * * * * */


    /* * * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- SOUL SIPHON *****|
    |* * * * * * * * * * * * * * * * * * * */

    @Inject(
            at = @At("HEAD"),
            method = "onDeath",
            cancellable = true)

    private void onSoulSiphonKill(DamageSource source, CallbackInfo ci) {
        LivingEntity user = (LivingEntity)source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;
        ItemStack mainHandStack = null;
        if (user != null) {
            mainHandStack = user.getMainHandStack();
        }
        boolean uniqueWeaponFlag =
                false;
        if (mainHandStack != null) {
            uniqueWeaponFlag = mainHandStack.getItem() == SoulDaggers.DAGGER_ETERNAL_KNIFE.asItem();
        }

        if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.SOUL_SIPHON, mainHandStack) >= 1)) {
            int level = EnchantmentHelper.getLevel(EnchantsRegistry.SOUL_SIPHON, mainHandStack);
            float siphonRand = user.getRandom().nextFloat();
            if (siphonRand <= 0.1F){
                if (attackingPlayer != null) {
                    attackingPlayer.addExperience(level * 3);
                }
            }
        }
        if (uniqueWeaponFlag) {
            float siphonRand = user.getRandom().nextFloat();
            if (siphonRand <= 0.1F) {
                if (attackingPlayer != null) {
                    attackingPlayer.addExperience(3);
                }
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- STUNNING *****|
    | * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- SWIRLING *****|
    | * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * |
    |***** ENCHANTMENTS -- THUNDERING *****|
    | * * * * * * * * * * * * * * * * * * */


    /* * * * * * * * * * * * * * * * * * *|
    |***** ENCHANTMENTS -- WEAKENING *****|
    |* * * * * * * * * * * * * * * * * * */


    /* * * * * * * * * * * |
    |****STATUS REMOVAL****|
    | * * * * * * * * * * */
// Remove Poison Effect if Player has weapon with Poison Cloud Enchantment
    @Inject(
            at = @At("HEAD"),
            method = "tick",
            cancellable = true)

    private void removePoisonIfPCEnchant(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity entity = (PlayerEntity) (Object) this;
            ItemStack mainHand = getMainHandStack();

            if (EnchantmentHelper.getLevel(EnchantsRegistry.POISON_CLOUD, mainHand) >= 1) {
                this.removeStatusEffect(StatusEffects.POISON);
            }
        }
    }
    // Remove Stunned Effects if Player has weapon with Stunning Enchantment

    @Inject(
            at = @At("HEAD"),
            method = "tick",
            cancellable = true)

    private void removeStunnedIfPCEnchant(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity entity = (PlayerEntity) (Object) this;
            ItemStack mainHand = getMainHandStack();

            if (EnchantmentHelper.getLevel(EnchantsRegistry.STUNNING, mainHand) >= 1) {
                this.removeStatusEffect(StatusEffectsRegistry.STUNNED);
                this.removeStatusEffect(StatusEffects.NAUSEA);
                this.removeStatusEffect(StatusEffects.SLOWNESS);
            }
        }
    }

    // Remove Weakness Effect if Player has weapon with Weakening Enchantment

    @Inject(
            at = @At("HEAD"),
            method = "tick",
            cancellable = true)

    private void removeWeakenedIfPCEnchant(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity entity = (PlayerEntity) (Object) this;
            ItemStack mainHand = getMainHandStack();

            if (EnchantmentHelper.getLevel(EnchantsRegistry.WEAKENING, mainHand) >= 1) {
                this.removeStatusEffect(StatusEffects.WEAKNESS);
            }
        }
    }
}