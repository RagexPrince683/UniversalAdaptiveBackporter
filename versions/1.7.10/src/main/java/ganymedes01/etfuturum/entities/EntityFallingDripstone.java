package ganymedes01.etfuturum.entities;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockPointedDripstone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFallingDripstone extends EntityFallingBlock {
	private int fallHurtMax = 40;
	private float fallHurtAmount = 4;
	private boolean hurtEntities = true;
	private boolean sourceRemoved;

	public EntityFallingDripstone(World world) { super(world); }

	public EntityFallingDripstone(World world, double x, double y, double z) {
		super(world, x, y, z, ModBlocks.POINTED_DRIPSTONE.get());
	}

	public EntityFallingDripstone(World world, double x, double y, double z, int meta, int count) {
		super(world, x, y, z, ModBlocks.POINTED_DRIPSTONE.get(), meta);
		dataWatcher.updateObject(20, MathHelper.clamp_int(count, 1, 256));
		sourceRemoved = true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(20, 1);
	}

	public int getCount() { return dataWatcher.getWatchableObjectInt(20); }

	@Override
	public void onUpdate() {
		// Legacy constructor/saves still remove their original source on the first tick.
		if (!worldObj.isRemote && field_145812_b == 0 && !sourceRemoved) {
			int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(posY), z = MathHelper.floor_double(posZ);
			if (worldObj.getBlock(x, y, z) != ModBlocks.POINTED_DRIPSTONE.get()) { setDead(); return; }
			field_145814_a = worldObj.getBlockMetadata(x, y, z);
			worldObj.setBlockToAir(x, y, z);
			sourceRemoved = true;
		}
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		field_145812_b++;
		motionY -= 0.04;
		moveEntity(motionX, motionY, motionZ);
		motionX *= 0.98;
		motionY *= 0.98;
		motionZ *= 0.98;
		if (!worldObj.isRemote && (onGround || field_145812_b > 600
				|| field_145812_b > 100 && (posY < 1 || posY > worldObj.getActualHeight()))) {
			setDead();
			if (field_145813_c && worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
				for (int remaining = getCount(); remaining > 0; remaining -= 64)
					entityDropItem(new ItemStack(ModBlocks.POINTED_DRIPSTONE.get(), Math.min(64, remaining), 0), 0);
			}
		}
	}

	@Override
	protected void fall(float distance) {
		if (worldObj.isRemote || !hurtEntities) return;
		int blocks = MathHelper.ceiling_float_int(distance - 1);
		if (blocks <= 0) return;
		float damage = Math.min(blocks * fallHurtAmount * getCount(), fallHurtMax);
		for (Entity entity : worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox))
			entity.attackEntityFrom(BlockPointedDripstone.STALACTITE_DAMAGE, damage);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("HurtEntities", hurtEntities);
		nbt.setFloat("FallHurtAmount", fallHurtAmount);
		nbt.setInteger("FallHurtMax", fallHurtMax);
		nbt.setInteger("Count", getCount());
		nbt.setBoolean("SourceRemoved", sourceRemoved);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		hurtEntities = !nbt.hasKey("HurtEntities") || nbt.getBoolean("HurtEntities");
		fallHurtAmount = nbt.hasKey("FallHurtAmount") ? nbt.getFloat("FallHurtAmount") : 4;
		if (!Float.isFinite(fallHurtAmount) || fallHurtAmount < 0) fallHurtAmount = 4;
		fallHurtMax = nbt.hasKey("FallHurtMax") ? Math.max(0, nbt.getInteger("FallHurtMax")) : 40;
		dataWatcher.updateObject(20, MathHelper.clamp_int(nbt.hasKey("Count") ? nbt.getInteger("Count") : 1, 1, 256));
		sourceRemoved = nbt.getBoolean("SourceRemoved");
	}
}
