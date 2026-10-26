package ganymedes01.etfuturum;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
final class BrushItem extends Item {
    BrushItem(){setMaxStackSize(1);setMaxDamage(64);}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.BOW;}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 72000;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){if(Content.available("brush")&&Features.enabled("content.archaeology"))player.setActiveHand(hand);return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){if(!(world.getTileEntity(pos) instanceof BrushableTile)||!Content.available("brush")||!Features.enabled("content.archaeology")||!player.canPlayerEdit(pos,face,player.getHeldItem(hand)))return EnumActionResult.PASS;player.setActiveHand(hand);return EnumActionResult.SUCCESS;}
    @Override public void onUsingTick(ItemStack stack,EntityLivingBase living,int remaining){if(living.world.isRemote||!(living instanceof EntityPlayer)||(getMaxItemUseDuration(stack)-remaining)%10!=5)return;EntityPlayer player=(EntityPlayer)living;if(!Content.available("brush")||!Features.enabled("content.archaeology")){player.stopActiveHand();return;}double reach=player instanceof EntityPlayerMP?((EntityPlayerMP)player).interactionManager.getBlockReachDistance():5;Vec3d eyes=player.getPositionEyes(1);RayTraceResult hit=player.world.rayTraceBlocks(eyes,eyes.add(player.getLookVec().scale(reach)),false,true,false);if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK||!player.world.isBlockModifiable(player,hit.getBlockPos())||!player.canPlayerEdit(hit.getBlockPos(),hit.sideHit,stack))return;if(player.world.getTileEntity(hit.getBlockPos()) instanceof BrushableTile&&((BrushableTile)player.world.getTileEntity(hit.getBlockPos())).brush(hit.sideHit)){stack.damageItem(1,player);player.stopActiveHand();}}
}
