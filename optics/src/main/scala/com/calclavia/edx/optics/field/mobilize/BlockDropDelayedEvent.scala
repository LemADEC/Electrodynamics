package com.calclavia.edx.optics.field.mobilize

import com.calclavia.edx.core.EDX
import nova.core.block.Block
import nova.core.block.Block.DropEvent
import nova.core.world.World
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import scala.collection.convert.wrapAll._

@deprecated
class BlockDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3D) extends DelayedEvent(ticks) {
	protected override def onEvent {
		if (EDX.network.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val dropEvt = new DropEvent(block)
				val drops = block.events.publish(dropEvt)
				dropEvt.drops.foreach(drop => world.addEntity(position + 0.5, drop))
				world.removeBlock(position)
			}
		}
	}
}