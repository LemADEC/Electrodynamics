package resonantinduction.core.prefab.part

import java.lang.{Iterable => JIterable}
import java.util.{ArrayList, List}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.vec.{Cuboid6, Rotation, Vector3}
import codechicken.microblock.FaceMicroClass
import codechicken.multipart._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.prefab.part.connector.PartAbstract

import scala.collection.convert.wrapAll._

/**
 * A part that acts as a face
 * @author Calclavia
 */
object PartFace
{
  val bounds = Array.ofDim[Cuboid6](6, 2)

  bounds(0)(0) = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1)
  bounds(0)(1) = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D)

  for (s <- 1 until 6)
  {
    val t = Rotation.sideRotations(s).at(Vector3.center)
    bounds(s)(0) = bounds(0)(0).copy().apply(t)
    bounds(s)(1) = bounds(0)(1).copy().apply(t)
  }
}

abstract class PartFace extends PartAbstract with TCuboidPart with JNormalOcclusion with TFacePart
{
  /**
   * Side of the block this is placed on.
   */
  var placementSide: ForgeDirection = null
  /**
   * The relative direction this block faces.
   */
  var facing: Byte = 0x00

  def preparePlacement(side: Int, facing: Int)
  {
    this.placementSide = ForgeDirection.getOrientation(side)
    this.facing = (facing - 2).asInstanceOf[Byte]
  }

  override def readDesc(packet: MCDataInput)
  {
    placementSide = ForgeDirection.getOrientation(packet.readByte)
    facing = packet.readByte
  }

  override def writeDesc(packet: MCDataOutput)
  {
    packet.writeByte(placementSide.ordinal)
    packet.writeByte(facing)
  }

  override def getSlotMask: Int = 1 << this.placementSide.ordinal

  override def getBounds: Cuboid6 = FaceMicroClass.aBounds(0x10 | this.placementSide.ordinal)

  override def redstoneConductionMap: Int = 0

  override def solid(arg0: Int): Boolean = true

  override def getOcclusionBoxes: JIterable[Cuboid6] = PartFace.bounds(placementSide.ordinal).toList

  override def occlusionTest(npart: TMultiPart): Boolean = NormalOcclusionTest.apply(this, npart)

  override def getDrops: JIterable[ItemStack] =
  {
    val drops: List[ItemStack] = new ArrayList[ItemStack]
    drops.add(getItem)
    return drops
  }

  override def pickItem(hit: MovingObjectPosition): ItemStack = getItem

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    placementSide = ForgeDirection.getOrientation(nbt.getByte("side"))
    facing = nbt.getByte("facing")
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setByte("side", placementSide.ordinal.asInstanceOf[Byte])
    nbt.setByte("facing", facing)
  }

  /**
   * Gets the relative direction of this block relative to the face it is on.
   */
  def getFacing: ForgeDirection = ForgeDirection.getOrientation(this.facing + 2)

  def getAbsoluteFacing: ForgeDirection =
  {
    var s: Int = 0
    facing match
    {
      case 0 =>
        s = 2
      case 1 =>
        s = 0
      case 2 =>
        s = 1
      case 3 =>
        s = 3
    }

    val absDir = Rotation.rotateSide(placementSide.ordinal, s)
    return ForgeDirection.getOrientation(absDir)
  }
}