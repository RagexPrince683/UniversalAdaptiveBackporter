package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.*;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import java.util.*;

/** Canonical smithing recipes; the public crafting adapter retains base/material/template order. */
public final class SmithingTableRecipes {
    private static final SmithingTableRecipes INSTANCE=new SmithingTableRecipes();
    private final List<SmithingTableRecipe> recipes=new ArrayList<>();
    public static SmithingTableRecipes getInstance(){return INSTANCE;}
    public static void init(){
        if(!Content.available("netherite_ingot"))return;
        for(String gear:new String[]{"sword","pickaxe","axe","shovel","hoe","helmet","chestplate","leggings","boots"})if(Content.available("netherite_"+gear))INSTANCE.addUpgrade(Content.stack("netherite_"+gear),new ItemStack(Item.getByNameOrId("minecraft:diamond_"+gear),1,OreDictionary.WILDCARD_VALUE));
        if(Content.available("netherite_spear")&&Content.available("diamond_spear"))INSTANCE.addUpgrade(Content.stack("netherite_spear"),new ItemStack(Content.item("diamond_spear"),1,OreDictionary.WILDCARD_VALUE));
        if(Content.available("nautilus_armor"))INSTANCE.addUpgrade(new ItemStack(Content.item("nautilus_armor"),1,4),new ItemStack(Content.item("nautilus_armor"),1,3));
    }
    private void addUpgrade(ItemStack result,Object base){recipes.add(new SmithingTableRecipe(base,"ingotNetherite",result,true,Features.enabled("content.template_smithing")?Content.stack("netherite_upgrade_smithing_template"):null));}
    public void addRecipe(ItemStack result,Object materialSlot,Object toolSlot){addRecipe(new SmithingTableRecipe(toolSlot,materialSlot,result,true));}
    public void addRecipeNoNBT(ItemStack result,Object materialSlot,Object toolSlot){addRecipe(new SmithingTableRecipe(toolSlot,materialSlot,result,false));}
    public void addRecipe(SmithingTableRecipe recipe){recipes.add(Objects.requireNonNull(recipe));}
    public List<SmithingTableRecipe> getRecipes(){return recipes;}
    public boolean isBase(ItemStack stack){if(!stack.isEmpty()&&stack.getItem() instanceof ItemArmor)return true;for(SmithingTableRecipe recipe:recipes)if(ingredientMatches(recipe.input,stack))return true;return false;}
    public boolean isMaterial(ItemStack stack){if(ArmorTrims.material(stack)!=null)return true;for(SmithingTableRecipe recipe:recipes)if(ingredientMatches(recipe.material,stack))return true;return false;}
    public boolean isTemplate(ItemStack stack){if(!stack.isEmpty()&&stack.getItem() instanceof ganymedes01.etfuturum.items.ItemSmithingTemplate)return true;for(SmithingTableRecipe recipe:recipes)if(recipe.template!=null&&ingredientMatches(recipe.template,stack))return true;return false;}
    public static boolean ingredientMatches(Object ingredient,ItemStack stack){if(ingredient==null)return stack.isEmpty();if(stack.isEmpty())return false;if(ingredient instanceof ItemStack)return OreDictionary.itemMatches((ItemStack)ingredient,stack,false);if(ingredient instanceof List<?>)for(Object candidate:(List<?>)ingredient)if(candidate instanceof ItemStack&&OreDictionary.itemMatches((ItemStack)candidate,stack,false))return true;return false;}
    public ItemStack findMatchingRecipe(ItemStack template,ItemStack base,ItemStack material,World world){ItemStack trim=ArmorTrims.apply(template,base,material);if(!trim.isEmpty())return trim;for(SmithingTableRecipe recipe:recipes)if(recipe.matches(template,base,material))return recipe.result(base);return ItemStack.EMPTY;}
    public ItemStack findMatchingRecipe(InventoryCrafting inventory,World world){return inventory.getSizeInventory()<2?ItemStack.EMPTY:findMatchingRecipe(inventory.getSizeInventory()>=3?inventory.getStackInSlot(2):ItemStack.EMPTY,inventory.getStackInSlot(0),inventory.getStackInSlot(1),world);}
    public static class SmithingTableRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
        private final boolean copyNBT;
        private final Object input,material,template;
        private final ItemStack output;
        public SmithingTableRecipe(Object toolSlot,Object materialSlot,ItemStack result,boolean copy){this(toolSlot,materialSlot,result,copy,null);}
        public SmithingTableRecipe(Object toolSlot,Object materialSlot,ItemStack result,boolean copy,Object template){input=ingredient(toolSlot);material=ingredient(materialSlot);this.template=template==null?null:ingredient(template);output=result.copy();copyNBT=copy;if(output.isEmpty())throw new IllegalArgumentException("Empty smithing result");}
        private static Object ingredient(Object value){if(value instanceof ItemStack)return ((ItemStack)value).copy();if(value instanceof Item)return new ItemStack((Item)value);if(value instanceof Block)return new ItemStack((Block)value,1,OreDictionary.WILDCARD_VALUE);if(value instanceof String)return OreDictionary.getOres((String)value);if(value instanceof List<?>)return value;throw new IllegalArgumentException("Invalid smithing ingredient: "+value);}
        public Object getTemplate(){return template;}
        public Object getInputItem(){return input;}
        public Object getMaterial(){return material;}
        private boolean matches(ItemStack template,ItemStack base,ItemStack material){return ingredientMatches(this.template,template)&&ingredientMatches(input,base)&&ingredientMatches(this.material,material);}
        private ItemStack result(ItemStack base){ItemStack result=output.copy();if(copyNBT){if(base.hasTagCompound())result.setTagCompound(base.getTagCompound().copy());if(base.isItemStackDamageable()&&result.isItemStackDamageable())result.setItemDamage(Math.min(base.getItemDamage(),result.getMaxDamage()));}return result;}
        @Override public boolean matches(InventoryCrafting inventory,World world){return inventory.getSizeInventory()>=2&&matches(inventory.getSizeInventory()>=3?inventory.getStackInSlot(2):ItemStack.EMPTY,inventory.getStackInSlot(0),inventory.getStackInSlot(1));}
        @Override public ItemStack getCraftingResult(InventoryCrafting inventory){return result(inventory.getStackInSlot(0));}
        @Override public ItemStack getRecipeOutput(){return output;}
        public int getRecipeSize(){return template==null?2:3;}
        @Override public boolean canFit(int width,int height){return width*height>=getRecipeSize();}
        @Override public boolean isDynamic(){return true;}
        @Override public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory){return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inventory);}
    }
}
