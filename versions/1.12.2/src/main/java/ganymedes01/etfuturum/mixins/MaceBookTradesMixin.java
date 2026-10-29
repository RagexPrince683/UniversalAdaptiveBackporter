package ganymedes01.etfuturum.mixins;
import ganymedes01.etfuturum.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.*;

/** Native librarian offers must honor the book policy independently of enchanting-table selection. */
@Mixin(targets="net.minecraft.entity.passive.EntityVillager$ListEnchantedBookForEmeralds",remap=false)
public class MaceBookTradesMixin {
    private static Enchantment choose(RegistryNamespaced<ResourceLocation,Enchantment> registry,Random random){
        List<Enchantment> permitted=new ArrayList<>();
        for(Enchantment enchantment:registry)if(enchantment.isAllowedOnBooks()&&ModEnchantments.available(enchantment))permitted.add(enchantment);
        return permitted.get(random.nextInt(permitted.size()));
    }
    @Group(name="maceBookTrade",min=1) @Redirect(method={"addMerchantRecipe","func_190888_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/registry/RegistryNamespaced;getRandomObject(Ljava/util/Random;)Ljava/lang/Object;"),require=0,expect=0,remap=false)
    private Object etfu$mcp(RegistryNamespaced<ResourceLocation,Enchantment> registry,Random random){return choose(registry,random);}
    @Group(name="maceBookTrade",min=1) @Redirect(method={"addMerchantRecipe","func_190888_a"},at=@At(value="INVOKE",target="Lnet/minecraft/util/registry/RegistryNamespaced;func_186801_a(Ljava/util/Random;)Ljava/lang/Object;"),require=0,expect=0,remap=false)
    private Object etfu$srg(RegistryNamespaced<ResourceLocation,Enchantment> registry,Random random){return choose(registry,random);}
}
