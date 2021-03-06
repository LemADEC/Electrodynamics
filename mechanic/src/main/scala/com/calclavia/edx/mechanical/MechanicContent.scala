package com.calclavia.edx.mechanical

import com.calclavia.edx.core.Reference
import com.calclavia.edx.mechanical.content.{BlockAxle, BlockGear}
import nova.core.block.BlockFactory
import nova.core.render.model.WavefrontObjectModelProvider
import nova.core.render.texture.BlockTexture
import nova.scala.modcontent.ContentLoader

object MechanicContent extends ContentLoader {
	override def id: String = Reference.mechanicID

	//Textures
	val gearTexture = new BlockTexture(Reference.domain, "wire")
	lazy val gearshaftTexture = gearTexture

	//Models
	//val modelGear = new WavefrontObjectModelProvider(Reference.domain, "gears")

	//Blocks
	var blockGearWood: BlockFactory = classOf[BlockGear.Wood]
	var blockGearStone: BlockFactory = classOf[BlockGear.Stone]
	var blockGearMetal: BlockFactory = classOf[BlockGear.Metal]
	var blockGearshaftWood: BlockFactory = classOf[BlockAxle.Wood]
	var blockGearshaftStone: BlockFactory = classOf[BlockAxle.Stone]
	var blockGearshaftMetal: BlockFactory = classOf[BlockAxle.Metal]
}
