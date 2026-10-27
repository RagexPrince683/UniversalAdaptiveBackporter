package ganymedes01.etfuturum;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Adds the maintained acquisition weights to native pools without replacing their rolls or other mods' loot. */
@Mod.EventBusSubscriber(modid=UAB.ID)
public final class NativeMobLoot {
    private NativeMobLoot(){}
    @SubscribeEvent public static void load(LootTableLoadEvent event){if(!event.getName().getNamespace().equals("minecraft"))return;String table=event.getName().getPath();templates(event,table);LootPool pool=event.getTable().getPool("main");if(pool==null)return;if(table.equals("chests/simple_dungeon")||table.equals("chests/stronghold_corridor"))for(String disc:new String[]{"otherside_record","precipice_record","creator_music_box_record","creator_record","disc_fragment_5"})add(pool,Content.stack(disc),1);if(table.equals("chests/nether_bridge"))add(pool,Content.stack("pigstep_record"),5);if(table.equals("chests/simple_dungeon")){add(pool,Content.stack("sniffer_egg"),2);add(pool,new ItemStack(Content.item("nautilus_armor"),1,0),5);add(pool,new ItemStack(Content.item("nautilus_armor"),1,2),3);add(pool,Content.stack("dried_ghast"),2);}else if(table.equals("chests/desert_pyramid")){add(pool,new ItemStack(Content.item("nautilus_armor"),1,1),4);add(pool,new ItemStack(Content.item("nautilus_armor"),1,3),2);}else if(table.equals("chests/stronghold_corridor"))add(pool,new ItemStack(Content.item("nautilus_armor"),1,3),2);}
    private static void templates(LootTableLoadEvent event,String table){
        if(table.equals("chests/desert_pyramid"))template(event,"dune",2,1F/7);
        else if(table.equals("chests/jungle_temple"))template(event,"wild",2,1F/3);
        else if(table.equals("chests/stronghold_library"))template(event,"eye",1,1);
        else if(table.equals("chests/stronghold_corridor"))template(event,"eye",1,.1F);
        else if(table.equals("chests/nether_bridge"))template(event,"rib",1,1F/15);
        else if(table.equals("chests/end_city_treasure"))template(event,"spire",1,1F/15);
        else if(table.equals("chests/woodland_mansion"))template(event,"vex",1,.5F);
        else if(table.equals("entities/elder_guardian"))template(event,"tide",1,.2F);
    }
    private static void template(LootTableLoadEvent event,String pattern,int count,float chance){String name=pattern+"_armor_trim_smithing_template",id=UAB.ID+"_"+pattern+"_template";if(!Content.available(name)||event.getTable().getPool(id)!=null)return;LootCondition[] conditions={new net.minecraft.world.storage.loot.conditions.RandomChance(chance)};LootEntry entry=new LootEntryItem(Content.item(name),1,0,new LootFunction[]{new SetCount(new LootCondition[0],new RandomValueRange(count))},new LootCondition[0],UAB.ID+":"+name);event.getTable().addPool(new LootPool(new LootEntry[]{entry},conditions,new RandomValueRange(1),new RandomValueRange(0),id));}
    private static void add(LootPool pool,ItemStack stack,int weight){String name=stack.getItem().getRegistryName().getPath();if(!Content.available(name)||stack.getItem() instanceof VariantItem&&!((VariantItem)stack.getItem()).available(stack.getMetadata()))return;String id=UAB.ID+":"+name+"_"+stack.getMetadata();if(pool.getEntry(id)==null)pool.addEntry(new LootEntryItem(stack.getItem(),weight,0,new LootFunction[]{new SetMetadata(new LootCondition[0],new RandomValueRange(stack.getMetadata()))},new LootCondition[0],id));}
}
