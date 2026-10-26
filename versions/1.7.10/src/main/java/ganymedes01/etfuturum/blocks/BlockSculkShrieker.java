package ganymedes01.etfuturum.blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
public class BlockSculkShrieker extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	@Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[]{new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "sculk_shrieker"), new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "sculk_shrieker_can_summon")};
		blockIcon = models[0].particle;
	}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[(meta & 8) == 0 ? 0 : 1]; }
	public BlockSculkShrieker() { super(Material.rock); setNames("sculk_shrieker"); setBlockTextureName("sculk_shrieker_side"); setHardness(3); setLightOpacity(0); setBlockBounds(0, 0, 0, 1, .5F, 1); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundSculkShrieker); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	@Override public boolean isSideSolid(net.minecraft.world.IBlockAccess world, int x, int y, int z, net.minecraftforge.common.util.ForgeDirection side) { return side == net.minecraftforge.common.util.ForgeDirection.DOWN; }
	@Override public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		if (entity instanceof EntityPlayer player) shriek(world, x, y, z, player);
	}
	public void shriek(World world, int x, int y, int z, EntityPlayer player) {
		if (world.isRemote || player.capabilities.disableDamage || player.isSneaking() || (world.getBlockMetadata(x, y, z) & 8) == 0 || !ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableModernHostileSpawning) return;
		net.minecraft.nbt.NBTTagCompound data = player.getEntityData(); long time = world.getTotalWorldTime(); if (data.getLong("EFRShriekCooldown") > time) return; data.setLong("EFRShriekCooldown", time + 200); long last = data.getLong("EFRShriekLast"); int decayed = last > 0 ? (int) Math.min(4, Math.max(0, time - last) / 12000) : 0; int warnings = Math.min(4, Math.max(0, data.getInteger("EFRShriekWarnings") - decayed) + 1); data.setLong("EFRShriekLast", time); data.setInteger("EFRShriekWarnings", warnings); world.playSoundEffect(x + .5, y + .5, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.sculk_shrieker.shriek", 2, 1);
		if (ganymedes01.etfuturum.potion.ModPotions.darkness != null) for (Object object : world.playerEntities) { EntityPlayer observer = (EntityPlayer) object; if (!observer.capabilities.disableDamage && observer.getDistanceSq(x + .5, y + .5, z + .5) <= 1600) observer.addPotionEffect(new net.minecraft.potion.PotionEffect(ganymedes01.etfuturum.potion.ModPotions.darkness.id, 260, 0)); }
		if (warnings < 4 || world.difficultySetting == net.minecraft.world.EnumDifficulty.PEACEFUL || !world.getGameRules().getGameRuleBooleanValue("doMobSpawning") || !world.getEntitiesWithinAABB(ganymedes01.etfuturum.entities.EntityWarden.class, player.boundingBox.expand(48, 24, 48)).isEmpty()) return;
		for (int attempt = 0; attempt < 12; attempt++) { int px = x + world.rand.nextInt(11) - 5, pz = z + world.rand.nextInt(11) - 5; if (!world.blockExists(px, y + 3, pz) || !world.getBlock(px, y - 1, pz).isSideSolid(world, px, y - 1, pz, net.minecraftforge.common.util.ForgeDirection.UP)) continue; ganymedes01.etfuturum.entities.EntityWarden warden = new ganymedes01.etfuturum.entities.EntityWarden(world); warden.setLocationAndAngles(px + .5, y, pz + .5, 0, 0); if (world.getCollidingBoundingBoxes(warden, warden.boundingBox).isEmpty() && world.checkNoEntityCollision(warden.boundingBox)) { world.spawnEntityInWorld(warden); break; } }
	}
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public net.minecraft.tileentity.TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntitySculkShrieker(); }
	@Override public int damageDropped(int meta) { return 0; }
}
