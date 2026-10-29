package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.Tags;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.*;

/** Native tracked entity, including while perched: no client-owned shoulder or movement state. */
public final class EntityParrot extends EntityTameable {
    public static final String[] IMITATIONS = {"blaze", "creeper", "endermite", "evoker", "ghast", "guardian", "husk", "magma_cube", "phantom", "pillager", "ravager", "shulker", "silverfish", "skeleton", "slime", "spider", "stray", "vex", "vindicator", "witch", "wither", "wither_skeleton", "zombie", "zombie_villager", "drowned", "bogged", "breeze", "warden", "creaking"};
    private int perchCooldown = 100;
    private double targetX, targetY, targetZ;
    public float flap, previousFlap, flapSpeed, previousFlapSpeed;
    public EntityParrot(World world) { super(world); setSize(.5F, .9F); }
    @Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(20, (byte) 0); dataWatcher.addObject(21, 0); dataWatcher.addObject(22, (byte) 0); }
    @Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6); getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2); }
    @Override protected boolean isAIEnabled() { return true; }
    @Override protected boolean canDespawn() { return !isTamed(); }
    @Override public boolean isBreedingItem(ItemStack stack) { return false; }
    @Override public EntityAgeable createChild(EntityAgeable mate) { return null; }
    @Override protected void fall(float distance) { }
    public int getVariant() { return dataWatcher.getWatchableObjectByte(20) & 7; }
    public void setVariant(int variant) { dataWatcher.updateObject(20, (byte) MathHelper.clamp_int(variant, 0, 4)); }
    public boolean isPartying() { return (dataWatcher.getWatchableObjectByte(22) & 4) != 0; }
    public boolean isFlying() { return !onGround && shoulderOwner() == null; }
    public EntityPlayer shoulderOwner() { Entity entity = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(21)); return entity instanceof EntityPlayer ? (EntityPlayer) entity : null; }
    @Override public boolean canBePushed() { return shoulderOwner() == null; }
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) { setVariant(rand.nextInt(5)); return super.onSpawnWithEgg(data); }
    @Override public boolean getCanSpawnHere() {
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
        Material floor = worldObj.getBlock(x, y - 1, z).getMaterial();
        return y > 0 && (floor == Material.leaves || floor == Material.grass || floor == Material.wood)
                && worldObj.getBlockLightValue(x, y, z) > 8 && worldObj.checkNoEntityCollision(boundingBox)
                && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(boundingBox);
    }
    private static boolean seed(ItemStack stack) { return stack != null && (stack.getItem() == Items.wheat_seeds || stack.getItem() == Items.melon_seeds || stack.getItem() == Items.pumpkin_seeds || stack.getItem() == ganymedes01.etfuturum.ModItems.BEETROOT_SEEDS.get()); }
    private void consume(EntityPlayer player, ItemStack stack) { if (!player.capabilities.isCreativeMode && --stack.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); }
    @Override public boolean interact(EntityPlayer player) {
        ItemStack held = player.getHeldItem();
        if (!isTamed() && seed(held)) {
            if (!worldObj.isRemote) { consume(player, held); playSound(Tags.MC_ASSET_VER + ":entity.parrot.eat", 1, 1); if (rand.nextInt(10) == 0) { setTamed(true); func_152115_b(player.getUniqueID().toString()); worldObj.setEntityState(this, (byte) 7); } else worldObj.setEntityState(this, (byte) 6); }
            return true;
        }
        if (held != null && held.getItem() == Items.cookie) { if (!worldObj.isRemote) { consume(player, held); attackEntityFrom(DamageSource.causePlayerDamage(player), Float.MAX_VALUE); } return true; }
        if (isTamed() && func_152114_e(player)) { if (!worldObj.isRemote) { leaveShoulder(); setSitting(!isSitting()); } return true; }
        return false;
    }
    public boolean perch(EntityPlayer player, int side) {
        if (worldObj.isRemote || !isTamed() || getOwner() != player || isSitting() || perchCooldown > 0 || side < 0 || side > 1 || !safeShoulder(player)) return false;
        for (Object object : worldObj.getEntitiesWithinAABB(EntityParrot.class, player.boundingBox.expand(4, 4, 4))) {
            EntityParrot other = (EntityParrot) object;
            if (other != this && other.shoulderOwner() == player && (other.dataWatcher.getWatchableObjectByte(22) & 1) == side) return false;
        }
        dataWatcher.updateObject(21, player.getEntityId()); dataWatcher.updateObject(22, (byte) side); getNavigator().clearPathEntity(); positionOnShoulder(player); return true;
    }
    private static boolean safeShoulder(EntityPlayer player) { return player.isEntityAlive() && !player.isSneaking() && !player.isInWater() && !player.handleLavaMovement() && !player.capabilities.isFlying && player.ridingEntity == null && player.fallDistance <= .5F && player.hurtTime == 0; }
    public void leaveShoulder() { if (!worldObj.isRemote) { dataWatcher.updateObject(21, 0); perchCooldown = 100; motionY = .15; } }
    private void positionOnShoulder(EntityPlayer player) {
        double angle = Math.toRadians(player.renderYawOffset), side = (dataWatcher.getWatchableObjectByte(22) & 1) == 0 ? .4 : -.4;
        setPosition(player.posX + Math.cos(angle) * side, player.boundingBox.minY + 1.35, player.posZ + Math.sin(angle) * side);
        motionX = motionY = motionZ = 0; rotationYaw = renderYawOffset = player.renderYawOffset; fallDistance = 0;
    }
    @Override public boolean attackEntityFrom(DamageSource source, float amount) { if (!worldObj.isRemote) { leaveShoulder(); setSitting(false); } return super.attackEntityFrom(source, amount); }
    @Override public void onUpdate() {
        super.onUpdate();
        EntityPlayer perched = shoulderOwner();
        if (perched != null) { if (!worldObj.isRemote && !safeShoulder(perched)) leaveShoulder(); else positionOnShoulder(perched); }
        else if (!worldObj.isRemote && dataWatcher.getWatchableObjectInt(21) != 0) leaveShoulder();
    }
    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        previousFlap = flap; previousFlapSpeed = flapSpeed;
        flapSpeed = MathHelper.clamp_float(flapSpeed + (isFlying() ? 1.2F : -.3F), 0, 1); flap += isFlying() ? 2 : .2F;
        if (worldObj.isRemote) return;
        if (perchCooldown > 0) perchCooldown--;
        if (ticksExisted % 40 == 0) {
            boolean dance = false; int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
            search: for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++) for (int dz = -3; dz <= 3; dz++) if (dx*dx+dy*dy+dz*dz <= 12 && worldObj.blockExists(x+dx,y+dy,z+dz) && worldObj.getBlock(x+dx,y+dy,z+dz) == Blocks.jukebox && worldObj.getBlockMetadata(x+dx,y+dy,z+dz) > 0) { dance = true; break search; }
            dataWatcher.updateObject(22, (byte) ((dataWatcher.getWatchableObjectByte(22) & 1) | (dance ? 4 : 0)));
        }
        if (rand.nextInt(50) == 0) {
            List<?> nearby = worldObj.getEntitiesWithinAABB(EntityLiving.class, boundingBox.expand(20,20,20));
            if (!nearby.isEmpty()) { EntityLiving mob = (EntityLiving) nearby.get(rand.nextInt(nearby.size())); String name = EntityList.getEntityString(mob); if (name != null) { name = name.replace("etfuturum.", "").toLowerCase(Locale.ROOT); if (name.equals("lavaslime")) name = "magma_cube"; if (mob instanceof net.minecraft.entity.monster.EntitySkeleton && ((net.minecraft.entity.monster.EntitySkeleton) mob).getSkeletonType() == 1) name = "wither_skeleton"; if (Arrays.asList(IMITATIONS).contains(name)) playSound(Tags.MC_ASSET_VER + ":entity.parrot.imitate." + name, .7F, 1 + (rand.nextFloat()-rand.nextFloat())*.2F); } }
        }
    }
    @Override public void moveEntityWithHeading(float strafe, float forward) {
        if (worldObj.isRemote || shoulderOwner() != null) return;
        if (isSitting() || isPartying()) { motionX *= .5; motionZ *= .5; motionY -= .08; }
        else {
            EntityLivingBase owner = isTamed() ? getOwner() : null;
            if (owner instanceof EntityPlayer && owner.onGround && getDistanceSqToEntity(owner) < 2.5 && (perch((EntityPlayer) owner, 0) || perch((EntityPlayer) owner, 1))) return;
            if (owner != null && getDistanceSqToEntity(owner) > 4) { targetX = owner.posX; targetY = owner.posY + 1; targetZ = owner.posZ; }
            else if (ticksExisted % 40 == 0 || getDistanceSq(targetX,targetY,targetZ) < 1) {
                targetX = posX + rand.nextInt(13)-6; targetZ = posZ + rand.nextInt(13)-6;
                int x = MathHelper.floor_double(targetX), z = MathHelper.floor_double(targetZ);
                targetY = worldObj.blockExists(x,MathHelper.floor_double(posY),z) ? Math.min(posY+3, worldObj.getHeightValue(x,z)+1) : posY;
            }
            double dx=targetX-posX, dy=targetY-posY, dz=targetZ-posZ, length=Math.sqrt(dx*dx+dy*dy+dz*dz);
            if (length > .1) { motionX += (dx/length*.28-motionX)*.15; motionY += (dy/length*.28-motionY)*.15; motionZ += (dz/length*.28-motionZ)*.15; rotationYaw=renderYawOffset=(float)(Math.atan2(dz,dx)*180/Math.PI)-90; }
            if (isCollidedHorizontally) motionY = .2;
        }
        moveEntity(motionX,motionY,motionZ); motionX*=.9; motionY*=.9; motionZ*=.9;
    }
    @Override protected void dropFewItems(boolean player, int looting) { int count = rand.nextInt(3)+rand.nextInt(looting+1); if (count > 0) dropItem(Items.feather,count); }
    @Override protected String getLivingSound() { return Tags.MC_ASSET_VER+":entity.parrot.ambient"; }
    @Override protected String getHurtSound() { return Tags.MC_ASSET_VER+":entity.parrot.hurt"; }
    @Override protected String getDeathSound() { return Tags.MC_ASSET_VER+":entity.parrot.death"; }
    @Override public void writeEntityToNBT(NBTTagCompound tag) { super.writeEntityToNBT(tag); tag.setInteger("Variant",getVariant()); }
    @Override public void readEntityFromNBT(NBTTagCompound tag) { super.readEntityFromNBT(tag); setVariant(tag.getInteger("Variant")); perchCooldown=100; }
}
