package ganymedes01.etfuturum.entities;
import ganymedes01.etfuturum.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
public class EntityBrownMooshroom extends EntityMooshroom {
    private NBTTagList effects=new NBTTagList();
    public EntityBrownMooshroom(World world){super(world);}
    @Override public boolean processInteract(EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand),stew=Content.available("suspicious_stew")?StewRecipe.flower(held):ItemStack.EMPTY;if(!stew.isEmpty()&&effects.tagCount()==0){if(!world.isRemote){effects=stew.getTagCompound().getTagList("Effects",10).copy();if(!player.capabilities.isCreativeMode)held.shrink(1);world.setEntityState(this,(byte)18);}return true;}if(held.getItem()==Items.BOWL&&!isChild()&&effects.tagCount()>0&&Content.available("suspicious_stew")){if(!world.isRemote){ItemStack result=Content.stack("suspicious_stew");NBTTagCompound tag=new NBTTagCompound();tag.setTag("Effects",effects.copy());result.setTagCompound(tag);if(!player.capabilities.isCreativeMode)held.shrink(1);if(held.isEmpty())player.setHeldItem(hand,result);else if(!player.inventory.addItemStackToInventory(result))player.dropItem(result,false);effects=new NBTTagList();}return true;}return super.processInteract(player,hand);}
    @Override public EntityMooshroom createChild(EntityAgeable mate){return new EntityBrownMooshroom(world);}
    @Override public java.util.List<ItemStack> onSheared(ItemStack item,IBlockAccess access,BlockPos pos,int fortune){java.util.List<ItemStack> drops=super.onSheared(item,access,pos,fortune);java.util.List<ItemStack> result=new java.util.ArrayList<>();for(ItemStack stack:drops)result.add(stack.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.RED_MUSHROOM)?new ItemStack(Blocks.BROWN_MUSHROOM,stack.getCount()):stack);return result;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){if(effects.tagCount()>0){NBTTagCompound effect=effects.getCompoundTagAt(0);getEntityData().setInteger("EffectId",effect.getInteger("EffectId"));getEntityData().setInteger("EffectDuration",effect.getInteger("EffectDuration"));}else{getEntityData().removeTag("EffectId");getEntityData().removeTag("EffectDuration");}super.writeEntityToNBT(tag);tag.setTag("StewEffects",effects.copy());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);effects=tag.getTagList("StewEffects",10).copy();if(effects.tagCount()==0&&getEntityData().getInteger("EffectId")>0){NBTTagCompound old=new NBTTagCompound();old.setInteger("EffectId",getEntityData().getInteger("EffectId"));old.setInteger("EffectDuration",Math.max(1,getEntityData().getInteger("EffectDuration")));effects.appendTag(old);}}
}
