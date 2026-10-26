package ganymedes01.etfuturum.mixins;

import java.util.Set;
import ganymedes01.etfuturum.AquaticPopulation;
import net.minecraft.entity.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** Native natural spawning only; commands, buckets, conversions and spawners keep their normal paths. */
@Mixin(value=WorldEntitySpawner.class,remap=false)
public class AquaticSpawningMixin {
    @Shadow(aliases="field_77193_b",remap=false) @Final private Set<ChunkPos> eligibleChunksForSpawning;
    @Redirect(method={"findChunksForSpawning","func_77192_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;countEntities(Lnet/minecraft/entity/EnumCreatureType;Z)I",remap=false),remap=false)
    private int etfu$count(WorldServer world,EnumCreatureType type,boolean spawning){AquaticPopulation index=AquaticPopulation.get(world);index.prepare(eligibleChunksForSpawning);return AquaticPopulation.indexed(type)?index.nativeCount(type):world.countEntities(type,spawning);}
    private boolean etfu$spawn(WorldServer world,Entity entity){if(!(entity instanceof EntityLiving)||!AquaticPopulation.managed(entity))return world.spawnEntity(entity);AquaticPopulation index=AquaticPopulation.get(world);EntityLiving living=(EntityLiving)entity;if(!index.admit(living))return false;boolean success=world.spawnEntity(entity);index.spawned(living,success);return success;}
    @Group(name="etfu$naturalJoin",min=1,max=1)
    @Redirect(method={"findChunksForSpawning","func_77192_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;spawnEntity(Lnet/minecraft/entity/Entity;)Z",remap=false),require=0,remap=false)
    private boolean etfu$development(WorldServer world,Entity entity){return etfu$spawn(world,entity);}
    @Group(name="etfu$naturalJoin",min=1,max=1)
    @Redirect(method={"findChunksForSpawning","func_77192_a"},at=@At(value="INVOKE",target="Lnet/minecraft/world/WorldServer;func_72838_d(Lnet/minecraft/entity/Entity;)Z",remap=false),require=0,remap=false)
    private boolean etfu$production(WorldServer world,Entity entity){return etfu$spawn(world,entity);}
}
