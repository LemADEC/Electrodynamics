package dark.core.common.machines;

import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.UniversalElectricity;
import dark.core.client.renders.BlockRenderingHandler;
import dark.core.common.DMCreativeTab;
import dark.core.prefab.helpers.Pair;
import dark.core.prefab.machine.BlockMachine;
import dark.core.registration.ModObjectRegistry.BlockBuildData;

public class BlockSolarPanel extends BlockMachine
{
    public static int tickRate = 10;
    public static float wattDay = 0.120f;
    public static float wattNight = 0.001f;
    public static float wattStorm = 0.005f;

    public BlockSolarPanel()
    {
        super(new BlockBuildData(BlockSolarPanel.class, "BlockSolarPanel", UniversalElectricity.machine));
        this.setBlockBounds(0, 0, 0, 1f, .6f, 1f);
        this.setCreativeTab(DMCreativeTab.tabIndustrial);
    }

    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntitySolarPanel();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return BlockRenderingHandler.BLOCK_RENDER_ID;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("DMSolarCell", TileEntitySolarPanel.class));

    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        super.loadExtraConfigs(config);
        tickRate = config.get("settings", "PanelUpdateRate", tickRate).getInt();
        wattDay = (float) (config.get("settings", "WattDayLight", 120).getDouble(120) / 1000);
        wattNight = (float) (config.get("settings", "WattMoonLight", 1).getDouble(1) / 1000);
        wattStorm = (float) (config.get("settings", "WattStorm", 6).getDouble(6) / 1000);
    }

    @Override
    public void loadOreNames()
    {
        OreDictionary.registerOre("SolarPanel", new ItemStack(this, 1, 0));

    }
}
