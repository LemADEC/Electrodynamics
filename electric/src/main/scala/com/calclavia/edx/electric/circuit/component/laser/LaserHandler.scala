package com.calclavia.edx.electric.circuit.component.laser

import com.calclavia.edx.electric.circuit.component.laser.LaserGrid.Laser
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.UnloadEvent
import nova.core.component.Component
import nova.core.event.{Event, EventBus}
import nova.core.util.RayTracer.RayTraceBlockResult

import scala.collection.convert.wrapAll._

/**
 * Handles laser interaction
 * @author Calclavia
 */
class LaserHandler(block: Block) extends Component {

	/**
	 * Called when the total energy due to incident lasers changes
	 */
	var onPowerChange = new EventBus[Event]

	/**
	 * Called when the laser handler receives another laser
	 */
	var onReceive = new EventBus[Event]

	private var emittingLaser: Laser = _

	private var prevEnergy = -1d

	//Hook block events.
	block.unloadEvent.add(
		(evt: UnloadEvent) => {
			//Destroy laser
			if (emittingLaser != null) {
				LaserGrid(block.world).destroy(emittingLaser)
			}
		}
	)

	/**
	 * The current power being received
	 */
	def receivingPower =
		LaserGrid(block.world)
			.laserGraph
			.vertexSet()
			.filter(_.hit.isInstanceOf[RayTraceBlockResult])
			.filter(_.hit.asInstanceOf[RayTraceBlockResult].block == block)
			.map(_.hitPower)
			.sum

	def receive(incident: Laser) {
		onReceive.publish(new Event)

		if (prevEnergy != receivingPower) {
			onPowerChange.publish(new Event)
		}
	}

	def emit(laser: Laser) {
		if (emittingLaser != null) {
			LaserGrid(block.world).destroy(emittingLaser)
		}

		LaserGrid(block.world).create(laser)
		emittingLaser = laser
	}
}
