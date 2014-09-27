package resonantinduction.archaic.fluid.gutter

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import resonantinduction.core.prefab.node.NodePressure
import resonantinduction.core.prefab.node.TileTankNode

class FluidGravityNode(parent: TileTankNode) extends NodePressure(parent)
{

    override def getPressure(dir: ForgeDirection): Int =
    {
        if (dir eq ForgeDirection.UP)
        {
            return -2
        }
        if (dir eq ForgeDirection.DOWN)
        {
            return 2
        }
        return 0
    }

    override def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return from != ForgeDirection.UP && !fluid.isGaseous
    }

    override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
        return from != ForgeDirection.UP && !fluid.isGaseous
    }

    override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
        if (!resource.getFluid.isGaseous)
        {
            return super.fill(from, resource, doFill)
        }
        return 0
    }
}