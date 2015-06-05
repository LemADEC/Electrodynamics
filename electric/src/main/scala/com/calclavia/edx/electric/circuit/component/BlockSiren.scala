package com.calclavia.edx.electric.circuit.component

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.minecraft.redstone.Redstone
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.renderer.ItemRenderer
import nova.core.game.Game
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.scala.{ExtendedUpdater, IO}

/**
 * Siren block
 */
class BlockSiren extends BlockEDX with ExtendedUpdater with Stateful {
	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val redstone = add(Game.components().make(classOf[Redstone], this))
	private val renderer = add(new StaticBlockRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))

	@Stored
	private var metadata = 0

	renderer.setTexture(func(dir => Optional.of(ElectricContent.sirenTexture)))

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.outputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])

	rightClickEvent.add((evt: RightClickEvent) => metadata = (metadata + 1) % 10)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (ticks % 30 == 0) {
			if (world != null) {
				if (redstone.getOutputWeakPower > 0) {
					var volume: Float = 0.5f
					for (i <- 0 to 6) {
						val check = position + Direction.fromOrdinal(i).toVector
						if (world.getBlock(check).get().sameType(this)) {
							volume *= 1.5f
						}
					}
					//TODO: Add sound
					//world.playSoundAtPosition(position(), Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
				}
			}

			/*
			if (!world.isRemote) {
				val volume = electricNode.power.toFloat / 1000f
				world.playSoundEffect(x, y, z, Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
			}*/
		}
	}

	override def getID: String = "siren"
}