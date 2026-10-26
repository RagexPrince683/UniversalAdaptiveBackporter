package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModernSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.*;
import net.minecraft.world.World;

public class EntityWanderingTrader extends EntityVillager {
    private int despawnDelay=48000;
    private MerchantRecipeList offers;
    public EntityWanderingTrader(World world){super(world,0);}
    @Override public boolean isMating(){return false;}
    @Override public EntityVillager createChild(EntityAgeable mate){return null;}
    @Override public MerchantRecipeList getRecipes(EntityPlayer player){if(offers==null){offers=new MerchantRecipeList();Item[] items={Item.getItemFromBlock(Blocks.SAPLING),Item.getItemFromBlock(Blocks.RED_FLOWER),Item.getItemFromBlock(Blocks.SAND),Items.DYE,Items.REEDS,Items.SLIME_BALL};for(int i=0;i<5;i++){int choice=rand.nextInt(items.length);offers.add(new MerchantRecipe(new ItemStack(Items.EMERALD,1+rand.nextInt(4)),new ItemStack(items[choice],choice==2?8:1,choice==0?rand.nextInt(6):choice==3?rand.nextInt(16):0)));}offers.add(new MerchantRecipe(new ItemStack(Items.EMERALD,5),new ItemStack(Items.WATER_BUCKET)));}return offers;}
    @Override public void useRecipe(MerchantRecipe recipe){recipe.incrementToolUses();}
    @Override public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote)return;if(!isTrading()&&--despawnDelay<=0)setDead();if(!world.isDaytime()){if(!isPotionActive(MobEffects.INVISIBILITY))addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,24000));}else removePotionEffect(MobEffects.INVISIBILITY);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DespawnDelay",despawnDelay);if(offers!=null)tag.setTag("WanderingOffers",offers.getRecipiesAsTags());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);despawnDelay=tag.hasKey("DespawnDelay")?MathHelper.clamp(tag.getInteger("DespawnDelay"),0,48000):48000;offers=tag.hasKey("WanderingOffers",10)?new MerchantRecipeList(tag.getCompoundTag("WanderingOffers")):null;}
    @Override protected SoundEvent getAmbientSound(){return ModernSounds.get("entity.wandering_trader.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModernSounds.get("entity.wandering_trader.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModernSounds.get("entity.wandering_trader.death");}
}
