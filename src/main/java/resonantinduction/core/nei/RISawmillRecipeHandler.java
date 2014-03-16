package resonantinduction.core.nei;

import resonantinduction.core.ResonantInduction.RecipeType;
import calclavia.lib.utility.LanguageUtility;

public class RISawmillRecipeHandler extends RITemplateRecipeHandler
{

	@Override
	public String getRecipeName()
	{
		return LanguageUtility.getLocal("resonantinduction.machine.sawmill");
	}

	@Override
	public RecipeType getMachine()
	{
		return RecipeType.SAWMILL;
	}
}
