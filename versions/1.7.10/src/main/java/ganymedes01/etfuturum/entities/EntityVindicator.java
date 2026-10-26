package ganymedes01.etfuturum.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityVindicator extends EntityIllager {
	public EntityVindicator(World world) {
		super(world); setCurrentItemOrArmor(0, new ItemStack(Items.iron_axe)); getNavigator().setBreakDoors(true);
		tasks.addTask(1, new EntityAIBreakDoor(this)); tasks.addTask(2, new EntityAIAttackOnCollide(this, 1, false));
		targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityLiving.class, 20, true, false, entity -> "Johnny".equals(getCustomNameTag()) && !(entity instanceof EntityIllager) && !(entity instanceof EntityVex)));
	}
	@Override protected void applyEntityAttributes() { super.applyEntityAttributes(); getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7); }

	@Override protected String getLivingSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vindicator.ambient"; }
	@Override protected String getHurtSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vindicator.hurt"; }
	@Override protected String getDeathSound() { return ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":entity.vindicator.death"; }
}
