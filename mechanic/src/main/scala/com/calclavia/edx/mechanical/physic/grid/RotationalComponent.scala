package com.calclavia.edx.mechanical.physic.grid

import java.util.stream.Collectors

import com.calclavia.edx.mechanical.content.axle.BlockAxle
import com.calclavia.edx.mechanical.content.gear.BlockGear
import com.calclavia.edx.mechanical.physic.MechanicalMaterial
import nova.core.block.Block
import nova.core.block.component.Connectable
import nova.core.component.Require
import nova.core.util.Direction
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.OptionWrapper._
import nova.scala.wrapper.VectorWrapper._

import scala.collection.JavaConversions._



object MechanicalNode {

	@Require(classOf[MechanicalMaterial])
	trait Material extends MechanicalNodeConstantMassAndFriction {

		val size: Double
		lazy val material: MechanicalMaterial =  block.get(classOf[MechanicalMaterial])

		lazy val constantMass = size * material.density
		lazy val constantFriction = mass * material.friction

	}

	trait MechanicalNodeConstantFriction extends MechanicalNode {
		final def friction: Double = constantFriction
		val constantFriction: Double
	}

	trait MechanicalNodeConstantMass extends MechanicalNode {
		final def mass: Double = constantMass
		val constantMass: Double
	}

	trait MechanicalNodeConstantMassAndFriction extends MechanicalNodeConstantFriction with MechanicalNodeConstantMass

}

case class RotationalEdge(src: AnyRef, tar: AnyRef, forward: Boolean)

abstract class MechanicalNode(val block: Block) extends Connectable[MechanicalNode] {
	def mass: Double
	def friction: Double
	var grid: Option[MechanicalGrid] = None

	private[grid] var relativeSpeed: Option[Double] = None

	val connectionFilter = (other: MechanicalNode) => this.canConnect(other) && other.canConnect(this)

	connections = supplier(() => {
		var res = Set.empty[MechanicalNode]
		adjacentBlocks.foreach {
			case (dir, Some(aBlock)) =>
				aBlock.getOp(classOf[MechanicalNode]).toOption.filter(connectionFilter).foreach(res += _)
				aBlock.getOp(classOf[MicroblockContainer]).toOption
						.map(_.microblocks(classOf[MechanicalNode]).collect(Collectors.toSet()))
						.map(_.filter(connectionFilter)).foreach(res ++= _)
			case _ =>
		}
		res
	})


	protected def adjacentBlocks = Direction.DIRECTIONS.map(dir => (dir, block.world.getBlock(block.transform.position + dir.toVector).toOption)).toMap
}

@Require(classOf[MechanicalMaterial])
class MechanicalNodeGear(block: BlockGear) extends MechanicalNode(block) with MechanicalNode.MechanicalNodeConstantMassAndFriction with MechanicalNode.Material {
	val size: Double = block.size

	canConnect = func {
		(other: MechanicalNode) => {
			val diff = other.block.position - this.block.position
			if (diff.getNormSq != 0)
				Direction.fromVector(diff) == this.block.side
			else
				// If two unit vecotrs are perpendiclular to each other lenght between hem has to be equal sqrt(2)
				this.block.microblock.position.distanceSq(other.block.get(classOf[Microblock]).position) == 2
		}
	}

}
@Require(classOf[MechanicalMaterial])
class MechanicalNodeAxle(block: BlockAxle) extends MechanicalNode(block) with MechanicalNode.MechanicalNodeConstantMassAndFriction with MechanicalNode.Material {
	val size = 0.25D

	canConnect = func {
		(other: MechanicalNode) => {
			var diff = this.block.position - other.block.position
			if (diff.getNormSq == 0)
					diff = other.block.get(classOf[Microblock]).position - block.microblock.position

			BlockAxle.normalizeDir(Direction.fromVector(diff)) == this.block.dir
		}
	}

}
