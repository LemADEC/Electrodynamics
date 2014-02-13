package resonantinduction.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public abstract class RecipeResource
{
	public final boolean hasChance;
	public final float chance;

	protected RecipeResource()
	{
		this.hasChance = false;
		this.chance = 100;
	}

	protected RecipeResource(float chance)
	{
		this.hasChance = true;
		this.chance = chance;
	}

	public boolean hasChance()
	{
		return this.hasChance;
	}

	public float getChance()
	{
		return this.chance;
	}

	public abstract ItemStack getItemStack();

	public static class ItemStackResource extends RecipeResource
	{
		public final ItemStack itemStack;

		public ItemStackResource(ItemStack is)
		{
			super();
			this.itemStack = is;
		}

		public ItemStackResource(ItemStack is, float chance)
		{
			super(chance);
			this.itemStack = is;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof ItemStackResource)
			{
				return this.itemStack.isItemEqual(((ItemStackResource) obj).itemStack);
			}

			return false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return itemStack.copy();
		}
	}

	public static class OreDictResource extends RecipeResource
	{
		public final String name;

		public OreDictResource(String s)
		{
			super();
			this.name = s;

			if (OreDictionary.getOres(name).size() <= 0)
			{
				throw new RuntimeException("Added invalid OreDictResource recipe: " + name);
			}
		}

		public OreDictResource(String s, float chance)
		{
			super(chance);
			this.name = s;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof OreDictResource)
			{
				return this.name.equals(((OreDictResource) obj).name);
			}

			if (obj instanceof ItemStackResource)
			{
				return this.name.equals(OreDictionary.getOreName(OreDictionary.getOreID(((ItemStackResource) obj).itemStack)));
			}

			return false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return OreDictionary.getOres(name).get(0).copy();
		}
	}

	public static class FluidStackResource extends RecipeResource
	{
		public final FluidStack fluidStack;

		public FluidStackResource(FluidStack fs)
		{
			super();
			this.fluidStack = fs;
		}

		public FluidStackResource(FluidStack fs, float chance)
		{
			super(chance);
			this.fluidStack = fs;
		}

		@Override
		public boolean equals(Object obj)
		{
			return (obj instanceof FluidStack) ? ((FluidStack) obj).equals(obj) : false;
		}

		@Override
		public ItemStack getItemStack()
		{
			return null;
		}
	}
}