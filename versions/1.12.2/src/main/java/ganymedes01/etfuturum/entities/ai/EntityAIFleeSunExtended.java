package ganymedes01.etfuturum.entities.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.*;

import net.minecraft.world.World;

import java.util.Random;

public class EntityAIFleeSunExtended extends EntityAIBase {

    private EntityCreature theCreature;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private double movementSpeed;
    private World theWorld;

    public EntityAIFleeSunExtended(EntityCreature p_i1623_1_, double p_i1623_2_) {
        this.theCreature = p_i1623_1_;
        this.movementSpeed = p_i1623_2_;
        this.theWorld = p_i1623_1_.world;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.theWorld.isDaytime()) {
            return false;
        } else if (!this.theCreature.isBurning()) {
            return false;
        } else if (!this.theWorld.canSeeSky(new BlockPos(this.theCreature.posX,this.theCreature.getEntityBoundingBox().minY,this.theCreature.posZ))) {
            return false;
        } else {
            return this.targetShadedPos();
        }
    }

    protected boolean targetShadedPos() {
        Vec3d vec3 = this.findPossibleShelter();

        if (vec3 == null) {
            return false;
        } else {
            this.shelterX = vec3.x;
            this.shelterY = vec3.y;
            this.shelterZ = vec3.z;
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.theCreature.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
    }

    private Vec3d findPossibleShelter() {
        Random random = this.theCreature.getRNG();

        for (int i = 0; i < 10; ++i) {
            int j = MathHelper.floor(this.theCreature.posX + (double)random.nextInt(20) - 10.0D);
            int k = MathHelper.floor(this.theCreature.getEntityBoundingBox().minY + (double)random.nextInt(6) - 3.0D);
            int l = MathHelper.floor(this.theCreature.posZ + (double)random.nextInt(20) - 10.0D);

            if (this.theWorld.isBlockLoaded(new BlockPos(j,k,l))&&!this.theWorld.canSeeSky(new BlockPos(j,k,l))&&this.theCreature.getBlockPathWeight(new BlockPos(j,k,l))<0) {
                return new Vec3d(j, k, l);
            }
        }

        return null;
    }
}
