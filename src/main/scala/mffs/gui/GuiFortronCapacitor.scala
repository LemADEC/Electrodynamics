package mffs.gui

import com.resonant.lib.wrapper.WrapFunctions._
import com.resonant.wrapper.lib.utility.science.UnitDisplay
import mffs.production.BlockFortronCapacitor
import nova.core.game.Game
import nova.core.gui.ComponentEvent.ActionEvent
import nova.core.gui.components.inventory.{PlayerInventory, Slot}
import nova.core.gui.components.{Button, Container, Label}
import nova.core.gui.layout.{Anchor, FlowLayout}
import nova.core.gui.{ComponentEvent, GuiEvent}
import nova.core.network.NetworkTarget.Side

import scala.collection.convert.wrapAll._

/**
 * Fortron Capacitor Gui
 * @author Calclavia
 */
class GuiFortronCapacitor extends GuiMFFS("fortronCapacitor") {

	var block: BlockFortronCapacitor = null

	/*
		drawTextWithTooltip("range", "%1: " + tile.getTransmissionRange, 8, 44, x, y)
		drawTextWithTooltip("input", EnumColor.DARK_GREEN + "%1", 12, 62, x, y)
		drawTextWithTooltip("output", EnumColor.RED + "%1", 92, 62, x, y)
		 drawFrequencyGui()
		 drawFortron()
		 Toggle button
	 */

	onGuiEvent((evt: GuiEvent.BindEvent) => reset(evt), classOf[GuiEvent.BindEvent])

	def reset(evt: GuiEvent.BindEvent) {
		block = evt.block.get().asInstanceOf[BlockFortronCapacitor]
		addInventory("main", evt.block.get().asInstanceOf[BlockFortronCapacitor].inventory)

		setPreferredSize(180, 200)

		val upgrades = new Container("upgrades").setLayout(new FlowLayout)
		//Upgrades
		(0 to 2) foreach (i => upgrades.add(new Slot("main", i)))

		//Input slots
		val inputs = new Container("inputs").setLayout(new FlowLayout)
		for (x <- 0 to 1; y <- 0 to 1)
			inputs.add(new Slot("main", x + y))

		//Output slots
		val outputs = new Container("outputs").setLayout(new FlowLayout)
		for (x <- 0 to 1; y <- 0 to 1)
			outputs.add(new Slot("main", x + y))

		/**
		 * Layout
		 */
		add(new Label("title", "Fortron Capacitor"), Anchor.NORTH)

		add(
			new Container()
				.add(
					new Container()
						.add(new Label(Game.instance.languageManager.translate("gui.linkedDevice", Map("%1" -> (block.getDeviceCount + "")))))
						.add(new
							Label(Game.instance.languageManager.translate("gui.transmissionRate", Map("%1" -> (new
									UnitDisplay(UnitDisplay.Unit.LITER, block.getTransmissionRate * 20).symbol() + "/s")))))

					, Anchor.NORTH
				)
				.add(
					new Container()
						.add(upgrades)
						//.add(inputs)
						//.add(outputs)
						//Toggle button
						.add(
							new Button("toggle", "Toggle Mode")
								.setPreferredSize(80, 20)
								.onEvent((evt: ActionEvent, component: Button) => block.toggleTransferMode(), classOf[ComponentEvent.ActionEvent], Side.SERVER)
						)
					, Anchor.SOUTH
				)
			, Anchor.CENTER
		)

		add(new PlayerInventory("inventory"), Anchor.SOUTH)
	}
}
