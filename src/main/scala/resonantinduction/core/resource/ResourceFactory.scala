package resonantinduction.core.resource

import java.awt._
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.List
import javax.imageio.ImageIO

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.LanguageRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fluids.{FluidContainerRegistry, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.OreDictionary
import resonant.api.recipe.MachineRecipes
import resonant.lib.factory.resources.RecipeType
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.StringWrapper._
import resonantinduction.archaic.ArchaicContent
import resonantinduction.core.resource.content._
import resonantinduction.core.{Reference, Settings}

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * @author Calclavia
 */
object ResourceFactory
{
  /**
   * A list of materials
   */
  val materials = mutable.Set.empty[String]

  val blackList = Array("uranium")

  /**
   * Reference to color of material
   */
  val materialColorCache = mutable.Map.empty[String, Integer]
  /**
   * Reference to computed color tint of auto-generated ores
   */
  val iconColorCache = mutable.Map.empty[IIcon, Integer]
  val moltenFluidMap = mutable.Map.empty[String, Block]
  val mixtureFluidMap = mutable.Map.empty[String, Block]
  val moltenBucketMap = mutable.Map.empty[String, Item]
  val mixtureBucketMap = mutable.Map.empty[String, Item]
  val rubbleMap = mutable.Map.empty[String, Item]
  val dustMap = mutable.Map.empty[String, Item]
  val refinedDustMap = mutable.Map.empty[String, Item]

  def generate(materialName: String)
  {
    val nameCaps: String = LanguageUtility.capitalizeFirst(materialName)
    var localizedName: String = materialName
    val list: List[ItemStack] = OreDictionary.getOres("ingot" + nameCaps)

    //Fix the material name
    if (list.size > 0)
    {
      val firstOreItemStack = list(0)
      localizedName = firstOreItemStack.getDisplayName.trim

      if (LanguageUtility.getLocal(localizedName) != null && LanguageUtility.getLocal(localizedName) != "")
        localizedName = LanguageUtility.getLocal(localizedName)

      localizedName.replace("misc.resonantinduction.ingot".getLocal, "").replaceAll("^ ", "").replaceAll(" $", "")
    }

    //Generate molten fluid
    val fluidMolten = new FluidColored(ResourceFactory.materialNameToMolten(materialName)).setDensity(7).setViscosity(5000).setTemperature(273 + 1538)
    FluidRegistry.registerFluid(fluidMolten)
    LanguageRegistry.instance.addStringLocalization(fluidMolten.getUnlocalizedName, LanguageUtility.getLocal("tooltip.molten") + " " + localizedName)
    val blockFluidMaterial = new BlockFluidMaterial(fluidMolten)
    ArchaicContent.manager.newBlock("molten" + nameCaps, blockFluidMaterial)
    moltenFluidMap += (materialName -> blockFluidMaterial)

    //Generate molten bucket
    val moltenBucket = ArchaicContent.manager.newItem("bucketMolten" + materialName.capitalizeFirst, new ItemMoltenBucket(materialName))
    LanguageRegistry.instance.addStringLocalization(moltenBucket.getUnlocalizedName + ".name", "tooltip.molten".getLocal + " " + localizedName + " " + "tooltip.bucket".getLocal)
    FluidContainerRegistry.registerFluidContainer(fluidMolten, new ItemStack(moltenBucket))
    moltenBucketMap += materialName -> moltenBucket

    //Generate mixture fluid
    val fluidMixture = new FluidColored(ResourceFactory.materialNameToMixture(materialName))
    FluidRegistry.registerFluid(fluidMixture)
    val blockFluidMixture: BlockFluidMixture = new BlockFluidMixture(fluidMixture)
    LanguageRegistry.instance.addStringLocalization(fluidMixture.getUnlocalizedName, localizedName + " " + LanguageUtility.getLocal("tooltip.mixture"))
    ArchaicContent.manager.newBlock("mixture" + nameCaps, blockFluidMixture)
    mixtureFluidMap += materialName -> blockFluidMixture

    //Generate mixture bucket
    val mixtureBucket = ArchaicContent.manager.newItem("bucketMixture" + materialName.capitalizeFirst, new ItemMixtureBucket(materialName))
    LanguageRegistry.instance.addStringLocalization(mixtureBucket.getUnlocalizedName + ".name", "tooltip.mixture".getLocal + " " + localizedName + " " + "tooltip.bucket".getLocal)
    FluidContainerRegistry.registerFluidContainer(fluidMixture, new ItemStack(mixtureBucket))
    mixtureBucketMap += materialName -> mixtureBucket

    //Generate rubble, dust and refined dust
    val rubble = new ItemStack(ArchaicContent.manager.newItem("rubble" + materialName.capitalizeFirst, new ItemRubble(materialName)))
    LanguageRegistry.instance.addStringLocalization(rubble.getUnlocalizedName + ".name", localizedName + " " + "tooltip.rubble".getLocal)

    val dust = new ItemStack(ArchaicContent.manager.newItem("dust" + materialName.capitalizeFirst, new ItemDust(materialName)))
    LanguageRegistry.instance.addStringLocalization(dust.getUnlocalizedName + ".name", localizedName + " " + "tooltip.dust".getLocal)

    val refinedDust = new ItemStack(ArchaicContent.manager.newItem("refinedDust" + materialName.capitalizeFirst, new ItemRefinedDust(materialName)))
    LanguageRegistry.instance.addStringLocalization(refinedDust.getUnlocalizedName + ".name", localizedName + " " + "tooltip.refinedDust".getLocal)

    //Register rubble, dust and refined dust to OreDictionary
    OreDictionary.registerOre("rubble" + nameCaps, rubble)
    OreDictionary.registerOre("dirtyDust" + nameCaps, dust)
    OreDictionary.registerOre("dust" + nameCaps, refinedDust)

    //Add recipes
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, "rubble" + nameCaps, dust, dust)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER.name, "dirtyDust" + nameCaps, refinedDust)
    FurnaceRecipes.smelting.func_151394_a(dust, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)
    FurnaceRecipes.smelting.func_151394_a(refinedDust, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)

    if (OreDictionary.getOres("ore" + nameCaps).size > 0)
      MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, "ore" + nameCaps, "rubble" + nameCaps)
  }

  def init()
  {
    //Add vanilla ores
    registerMaterial("gold")
    registerMaterial("iron")

    //Add vanilla ore processing recipes
    OreDictionary.initVanillaEntries()
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Blocks.stone))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.cobblestone, Blocks.gravel)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.stone, Blocks.cobblestone)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SAWMILL.name, Blocks.log, new ItemStack(Blocks.planks, 7, 0))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.gravel, Blocks.sand)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.glass, Blocks.sand)
  }

  def generateAll()
  {
    //Call generate() on all materials
    materials.foreach(generate)
    Reference.logger.fine("Resource Factory generated " + materials.size + " resources.")
  }

  @SideOnly(Side.CLIENT)
  def computeColors()
  {
    for (material <- materials)
    {
      for (ingotStack <- OreDictionary.getOres("ingot" + LanguageUtility.capitalizeFirst(material)))
      {
        materialColorCache += material -> getAverageColor(ingotStack)
      }

      if (!materialColorCache.contains(material))
      {
        materialColorCache += material -> 0xFFFFFF
      }
    }
  }

  /**
   * Gets the average color of this item by looking at each pixel of the texture.
   *
   * @param itemStack - The itemStack
   * @return The RGB hexadecimal color code.
   */
  @SideOnly(Side.CLIENT)
  def getAverageColor(itemStack: ItemStack): Int =
  {
    var totalR: Int = 0
    var totalG: Int = 0
    var totalB: Int = 0
    var colorCount: Int = 0
    val item: Item = itemStack.getItem

    try
    {
      val icon: IIcon = item.getIconIndex(itemStack)

      if (iconColorCache.containsKey(icon))
      {
        return iconColorCache(icon)
      }
      var iconString: String = icon.getIconName
      if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
      {
        iconString = (if (iconString.contains(":")) iconString.replace(":", ":" + Reference.itemTextureDirectory) else Reference.itemTextureDirectory + iconString) + ".png"
        val textureLocation: ResourceLocation = new ResourceLocation(iconString)
        val inputstream: InputStream = Minecraft.getMinecraft.getResourceManager.getResource(textureLocation).getInputStream
        val bufferedimage: BufferedImage = ImageIO.read(inputstream)
        val width: Int = bufferedimage.getWidth
        val height: Int = bufferedimage.getWidth

        for (x <- 0 until width; y <- 0 until height)
        {
          val rgb: Color = new Color(bufferedimage.getRGB(x, y))
          val luma: Double = 0.2126 * rgb.getRed + 0.7152 * rgb.getGreen + 0.0722 * rgb.getBlue
          if (luma > 40)
          {
            totalR += rgb.getRed
            totalG += rgb.getGreen
            totalB += rgb.getBlue
            colorCount += 1
          }
        }
      }
      if (colorCount > 0)
      {
        totalR /= colorCount
        totalG /= colorCount
        totalB /= colorCount
        val averageColor: Int = new Color(totalR, totalG, totalB).brighter.getRGB
        iconColorCache.put(icon, averageColor)
        return averageColor
      }
    }
    catch
      {
        case e: Exception =>
        {
          Reference.logger.fine("Failed to compute colors for: " + item)
        }
      }
    return 0xFFFFFF
  }

  def registerMaterial(material: String)
  {
    if (!materials.contains(material) && OreDictionary.getOres("ore" + material.capitalizeFirst).size > 0)
    {
      Settings.config.load()
      val allowMaterial = Settings.config.get("Resource-Generator", "Enable " + material, true).getBoolean(true)
      Settings.config.save()

      if (!allowMaterial && !blackList.contains(material))
      {
        return
      }
      materials += material
    }
  }

  @SubscribeEvent
  def oreRegisterEvent(evt: OreDictionary.OreRegisterEvent)
  {
    if (evt.Name.startsWith("ingot"))
    {
      val oreDictName = evt.Name.replace("ingot", "")
      val materialName = oreDictName.decapitalizeFirst
      registerMaterial(materialName)
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def reloadTextures(e: TextureStitchEvent.Post)
  {
    computeColors()
  }

  def moltenToMaterial(fluidName: String): String =
  {
    return fluidNameToMaterial(fluidName, "molten")
  }

  def materialNameToMolten(fluidName: String): String =
  {
    return materialNameToFluid(fluidName, "molten")
  }

  def mixtureToMaterial(fluidName: String): String =
  {
    return fluidNameToMaterial(fluidName, "mixture")
  }

  def materialNameToMixture(fluidName: String): String =
  {
    return materialNameToFluid(fluidName, "mixture")
  }

  def fluidNameToMaterial(fluidName: String, `type`: String): String =
  {
    return LanguageUtility.decapitalizeFirst(LanguageUtility.underscoreToCamel(fluidName).replace(`type`, ""))
  }

  def materialNameToFluid(materialName: String, `type`: String): String =
  {
    return `type` + "_" + LanguageUtility.camelToLowerUnderscore(materialName)
  }

  def getName(itemStack: ItemStack): String =
  {
    return LanguageUtility.decapitalizeFirst(OreDictionary.getOreName(OreDictionary.getOreID(itemStack)).replace("dirtyDust", "").replace("dust", "").replace("ore", "").replace("ingot", ""))
  }

  def getColor(name: String): Int =
  {
    if (name != null && materialColorCache.containsKey(name))
    {
      return materialColorCache(name)
    }

    return 0xFFFFFF
  }
}