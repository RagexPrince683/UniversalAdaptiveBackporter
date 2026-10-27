package ganymedes01.etfuturum.core.utils;

import net.minecraft.util.math.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;



import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class EntityVectorUtils {

	private static ToDoubleFunction<BlockPos> getDoubleFunc(EntityCreature entity) {
		return pos -> entity.getBlockPathWeight(pos);
	}

	
	public static Vec3d findRandomTarget(EntityCreature entitycreatureIn, int xz, int y) {
		return func_226339_a_(entitycreatureIn, xz, y, 0, null, true, (float) Math.PI / 2F, getDoubleFunc(entitycreatureIn), false, 0, 0, true);
	}

	
	public static Vec3d findGroundTarget(EntityCreature p_226338_0_, int p_226338_1_, int p_226338_2_, int p_226338_3_, Vec3d p_226338_4_, double p_226338_5_) {
		return func_226339_a_(p_226338_0_, p_226338_1_, p_226338_2_, p_226338_3_, p_226338_4_, true, p_226338_5_, getDoubleFunc(p_226338_0_), true, 0, 0, false);
	}

	
	public static Vec3d getLandPos(EntityCreature creature, int maxXZ, int maxY) {
		return func_221024_a(creature, maxXZ, maxY, getDoubleFunc(creature));
	}

	
	public static Vec3d func_221024_a(EntityCreature p_221024_0_, int p_221024_1_, int p_221024_2_, ToDoubleFunction<BlockPos> p_221024_3_) {
		return func_226339_a_(p_221024_0_, p_221024_1_, p_221024_2_, 0, null, false, 0.0D, p_221024_3_, true, 0, 0, true);
	}

	
	public static Vec3d findAirTarget(EntityCreature p_226340_0_, int p_226340_1_, int p_226340_2_, Vec3d p_226340_3_, float p_226340_4_, int p_226340_5_, int p_226340_6_) {
		return func_226339_a_(p_226340_0_, p_226340_1_, p_226340_2_, 0, p_226340_3_, false, p_226340_4_, getDoubleFunc(p_226340_0_), true, p_226340_5_, p_226340_6_, true);
	}

	
	public static Vec3d findRandomTargetBlockTowards(EntityCreature entitycreatureIn, int xz, int y, Vec3d targetVec3d) {
		Vec3d vec3d = targetVec3d.subtract(entitycreatureIn.getPositionVector());
		return func_226339_a_(entitycreatureIn, xz, y, 0, vec3d, true, (float) Math.PI / 2F, getDoubleFunc(entitycreatureIn), false, 0, 0, true);
	}

	
	public static Vec3d findRandomTargetTowardsScaled(EntityCreature p_203155_0_, int xz, int p_203155_2_, Vec3d p_203155_3_, double p_203155_4_) {
		Vec3d vec3d = p_203155_3_.subtract(p_203155_0_.getPositionVector());
		return func_226339_a_(p_203155_0_, xz, p_203155_2_, 0, vec3d, true, p_203155_4_, getDoubleFunc(p_203155_0_), false, 0, 0, true);
	}

	
	public static Vec3d func_226344_b_(EntityCreature p_226344_0_, int p_226344_1_, int p_226344_2_, int p_226344_3_, Vec3d p_226344_4_, double p_226344_5_) {
		Vec3d vec3d = p_226344_4_.subtract(p_226344_0_.getPositionVector());
		return func_226339_a_(p_226344_0_, p_226344_1_, p_226344_2_, p_226344_3_, vec3d, false, p_226344_5_, getDoubleFunc(p_226344_0_), true, 0, 0, false);
	}

	
	public static Vec3d findRandomTargetBlockAwayFrom(EntityCreature entitycreatureIn, int xz, int y, Vec3d targetVec3d) {
		Vec3d vec3d = entitycreatureIn.getPositionVector().subtract(targetVec3d);
		return func_226339_a_(entitycreatureIn, xz, y, 0, vec3d, true, (float) Math.PI / 2F, getDoubleFunc(entitycreatureIn), false, 0, 0, true);
	}

	
	public static Vec3d func_223548_b(EntityCreature p_223548_0_, int p_223548_1_, int p_223548_2_, Vec3d p_223548_3_) {
		Vec3d vec3d = p_223548_0_.getPositionVector().subtract(p_223548_3_);
		return func_226339_a_(p_223548_0_, p_223548_1_, p_223548_2_, 0, vec3d, false, (float) Math.PI / 2F, getDoubleFunc(p_223548_0_), true, 0, 0, true);
	}

	
	private static Vec3d func_226339_a_(EntityCreature p_226339_0_, int p_226339_1_, int p_226339_2_, int p_226339_3_, Vec3d p_226339_4_, boolean p_226339_5_, double p_226339_6_, ToDoubleFunction<BlockPos> p_226339_8_, boolean p_226339_9_, int p_226339_10_, int p_226339_11_, boolean p_226339_12_) {
		Random random = p_226339_0_.getRNG();
		boolean flag;
		if (p_226339_0_.hasHome()) {
			double distCompare = (double) (p_226339_0_.getMaximumHomeDistance() + (float) p_226339_1_) + 1.0D;
			Vec3d vec = new Vec3d(p_226339_0_.getHomePosition());
			flag = p_226339_0_.getPositionVector().squareDistanceTo(vec) < distCompare * distCompare;
		} else {
			flag = false;
		}

		boolean flag1 = false;
		double d0 = Double.NEGATIVE_INFINITY;
		BlockPos blockpos = new BlockPos(p_226339_0_);

		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos1 = func_226343_a_(random, p_226339_1_, p_226339_2_, p_226339_3_, p_226339_4_, p_226339_6_);
			if (blockpos1 != null) {
				int j = blockpos1.getX();
				int k = blockpos1.getY();
				int l = blockpos1.getZ();
				if (p_226339_0_.hasHome() && p_226339_1_ > 1) {
					BlockPos blockpos2 = p_226339_0_.getHomePosition();
					if (p_226339_0_.posX > (double) blockpos2.getX()) {
						j -= random.nextInt(p_226339_1_ / 2);
					} else {
						j += random.nextInt(p_226339_1_ / 2);
					}

					if (p_226339_0_.posZ > (double) blockpos2.getZ()) {
						l -= random.nextInt(p_226339_1_ / 2);
					} else {
						l += random.nextInt(p_226339_1_ / 2);
					}
				}

				BlockPos blockpos3 = new BlockPos((double) j + p_226339_0_.posX, (double) k + p_226339_0_.posY, (double) l + p_226339_0_.posZ);/* pathnavigator.canEntityStandOnPos(blockpos3) replaced with path weight calc I THINK this might be the same, this change could be dangerous, potentially */
				if (p_226339_0_.world.isBlockLoaded(blockpos3)&&blockpos3.getY() >= 0 && blockpos3.getY() <= p_226339_0_.world.getHeight() && (!flag || p_226339_0_.isWithinHomeDistanceFromPosition(blockpos3)) && (!p_226339_12_ || getDoubleFunc(p_226339_0_).applyAsDouble(blockpos3) > 0)) {
					if (p_226339_9_) {
						blockpos3 = func_226342_a_(blockpos3, random.nextInt(p_226339_10_ + 1) + p_226339_11_, p_226339_0_.world.getHeight(),
								(p_226341_1_) -> !p_226339_0_.world.isBlockLoaded(p_226341_1_)||p_226339_0_.world.getBlockState(p_226341_1_).getMaterial().isSolid());
					}

					if (p_226339_5_ || p_226339_0_.world.getBlockState(blockpos3).getMaterial() != Material.WATER) {
						double d1 = p_226339_8_.applyAsDouble(blockpos3);
						if (d1 > d0) {
							d0 = d1;
							blockpos = blockpos3;
							flag1 = true;
						}
					}
				}
			}
		}

		return flag1 ? new Vec3d(blockpos) : null;
	}

	
	private static BlockPos func_226343_a_(Random p_226343_0_, int p_226343_1_, int p_226343_2_, int p_226343_3_, Vec3d p_226343_4_, double p_226343_5_) {
		if (p_226343_4_ != null && !(p_226343_5_ >= Math.PI)) {
			double d3 = Math.atan2(p_226343_4_.z, p_226343_4_.x) - (double) ((float) Math.PI / 2F);
			double d4 = d3 + (double) (2.0F * p_226343_0_.nextFloat() - 1.0F) * p_226343_5_;
			double d0 = Math.sqrt(p_226343_0_.nextDouble()) * Math.sqrt(2) * (double) p_226343_1_;
			double d1 = -d0 * Math.sin(d4);
			double d2 = d0 * Math.cos(d4);
			if (!(Math.abs(d1) > (double) p_226343_1_) && !(Math.abs(d2) > (double) p_226343_1_)) {
				int l = p_226343_0_.nextInt(2 * p_226343_2_ + 1) - p_226343_2_ + p_226343_3_;
				return new BlockPos(d1, l, d2);
			} else {
				return null;
			}
		} else {
			int i = p_226343_0_.nextInt(2 * p_226343_1_ + 1) - p_226343_1_;
			int j = p_226343_0_.nextInt(2 * p_226343_2_ + 1) - p_226343_2_ + p_226343_3_;
			int k = p_226343_0_.nextInt(2 * p_226343_1_ + 1) - p_226343_1_;
			return new BlockPos(i, j, k);
		}
	}

	static BlockPos func_226342_a_(BlockPos p_226342_0_, int p_226342_1_, int p_226342_2_, Predicate<BlockPos> p_226342_3_) {
		if (p_226342_1_ < 0) {
			throw new IllegalArgumentException("aboveSolidAmount was " + p_226342_1_ + ", expected >= 0");
		} else if (!p_226342_3_.test(p_226342_0_)) {
			return p_226342_0_;
		} else {
			BlockPos blockpos;
			for (blockpos = p_226342_0_.up(); blockpos.getY() < p_226342_2_ && p_226342_3_.test(blockpos); blockpos = blockpos.up()) {
			}

			BlockPos blockpos1;
			BlockPos blockpos2;
			for (blockpos1 = blockpos; blockpos1.getY() < p_226342_2_ && blockpos1.getY() - blockpos.getY() < p_226342_1_; blockpos1 = blockpos2) {
				blockpos2 = blockpos1.up();
				if (p_226342_3_.test(blockpos2)) {
					break;
				}
			}

			return blockpos1;
		}
	}
}