package ganymedes01.etfuturum.inventory;
import ganymedes01.etfuturum.entities.EntityLlama;
import net.minecraft.inventory.*;
public class ContainerLlama extends ContainerChestGeneric {
	public final EntityLlama llama;
	public ContainerLlama(IInventory player, EntityLlama llama) { super(player, llama, llama.getStrength(), false); this.llama = llama; for (Object object : inventorySlots) ((Slot) object).xDisplayPosition += (9 - llama.getStrength()) * 9; }
}
