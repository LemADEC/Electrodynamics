package com.calclavia.edx.mechanical.content

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.MechanicContent
import com.calclavia.edx.mechanical.physic.MechanicalMaterial
import com.calclavia.edx.mechanical.physic.grid.MechanicalNodeGear
import nova.core.block.Block
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.component.renderer.{DynamicRenderer, ItemRenderer}
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.{BlockRenderStream, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.math.{RotationUtil, MatrixStack, Vector3DUtil}
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import nova.scala.wrapper.OptionWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}
import org.apache.commons.math3.linear.RealMatrix

object BlockGear {
	val thickness = 2 / 16d
	val occlusionBounds = Array.ofDim[Cuboid](6)
	val matrixes = Array.ofDim[RealMatrix](6)

	{
		val oneByRoot2 = 1 / Math.sqrt(2)
		val center = new Cuboid(0, 0, 0, 1, thickness, 1) - 0.5

		for (dir <- Direction.DIRECTIONS) {
			val bySideRotation = dir match {
				case Direction.DOWN => Rotation.IDENTITY
				case Direction.UP => new Rotation(Vector3D.PLUS_I, Math.PI)
				case Direction.NORTH => new Rotation(Vector3D.PLUS_I, Math.PI / 2)
				case Direction.SOUTH => new Rotation(Vector3D.PLUS_I, -Math.PI / 2)
				case Direction.WEST => new Rotation(Vector3DUtil.FORWARD, Math.PI / 2)
				case Direction.EAST => new Rotation(Vector3DUtil.FORWARD, -Math.PI / 2)
				case Direction.UNKNOWN => throw new IllegalStateException()
			}

			occlusionBounds(dir.ordinal) = center transform bySideRotation add 0.5

			val matrix = new MatrixStack()
			// For some reason rotation of model works differently than in case of cuboids.
			matrix rotate new Rotation(Vector3D.PLUS_J, Math.PI)

			matrix rotate bySideRotation
			matrixes(dir.ordinal) = matrix.getMatrix

		}
	}

	val bigGearMap: Map[Direction, Seq[Vector3D]] = {
		val (templateVector, templateList) = {
			var list = List.empty[Vector3D]
			for(x <- -1 to 1; z <- -1 to 1 if x != 0 && z != 0){
				list = new Vector3D(x, 0, z) :: list
			}
			(new Vector3D(0, -1, 0), list)
		}
		var res: Map[Direction, Seq[Vector3D]] = Map.empty

		for (i <- Direction.DIRECTIONS) {
			val rot = new Rotation(templateVector, i.toVector)
			res += i -> templateList.map(rot.applyTo)
		}
		res
	}

}

object Test extends App {
	println(BlockGear.bigGearMap)
}

class BlockGear extends BlockEDX with Storable with Syncable {

	override def getID: String = "gear"

	@Sync
	@Store(key = "side")
	var _side: Byte = 0

	@Sync
	@Store(key = "size")
	var size = 1

	@Sync
	@Store(key = "master")
	var master = true

	@Sync
	@Store(key = "masterVector")
	var masterVector = Vector3D.ZERO

	def side = Direction.fromOrdinal(_side.asInstanceOf[Int])


	val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this._side = evt.side.opposite.ordinal.asInstanceOf[Byte]
				Optional.of(MicroblockContainer.sidePosition(this.side))
			}
		)

	events.on(classOf[Block.PlaceEvent]).bind((event: Block.PlaceEvent) => println(event))

	add(MechanicalMaterial.metal)

	private[this] val rotational = add(new MechanicalNodeGear(this))

	private[this] val blockRenderer = add(new DynamicRenderer())
	blockRenderer.onRender((m: Model) => {
		if (this.master) {
			m.addChild(model)

			model.matrix popMatrix()
			model.matrix pushMatrix()
			model.matrix scale(size, size, size)

			model.matrix rotate new Rotation(side.toVector, rotational.rotation)
		}
	})

	var _model: Option[Model] = None

	def model: Model = {
		_model match {
			case None => _model = Some(createModel); _model.get
			case Some(model) => model
		}
	}

	def createModel = {
		val tmp = new MeshModel()
		//val optional = MechanicContent.modelGear.getModel.stream().filter((model: Model) => "SmallGear".equals(model.name)).findFirst()

		//tmp.addChild(optional.orElseThrow(() => new IllegalStateException("Model is missing")))
		var box = collider.boundingBox()//.multiply(Math.sqrt(size))
		box = box - box.center()


		BlockRenderStream.drawCube(tmp, box, StaticCubeTextureCoordinates.instance)
		tmp.bind(MechanicContent.gearTexture)
		//tmp.matrix transform BlockGear.matrixes(_side)
		tmp.matrix translate ((side.toVector / 2d) - side.toVector *2 / 16d)
		tmp.matrix pushMatrix()
		tmp
	}

	collider.setBoundingBox(() => {
		BlockGear.occlusionBounds(_side)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	private[this] val itemRenderer = add(new ItemRenderer(this))

	itemRenderer.onRender = (m: Model) => {
		m.addChild(model)
	}

	def checkBigGear(): Unit = {
		val res = possibleSubGears().map(o => o.collect { case b: BlockGear => b }.exists(_.master)).forall(b => b)
		res match {
			case true => this.validateBigGear()
			case false => this.invalidateBigGear()
		}
	}
	private[this] def possibleSubGears() = {
		for(offset <- BlockGear.bigGearMap(side)) yield {
			val pos = this.position + offset
			val blockOp = this.world.getBlock(pos).toOption
			val part = blockOp.flatMap(_.getOp(classOf[MicroblockContainer])).flatMap(_.get(side))
			part.map(_.block)
		}
	}

	private[this] def validateBigGear(): Unit = {
		this.master = true
		this.masterVector = Vector3D.ZERO
		possibleSubGears().flatten.map(_.asInstanceOf[BlockGear]).foreach {
			gear =>
				gear.masterVector = this.position - gear.position
				gear.master = false
		}
	}

	private[this] def invalidateBigGear(): Unit = {
		this.master = true
		possibleSubGears().flatten.collect { case b: BlockGear => b }.foreach {
			gear =>
				gear.masterVector = Vector3D.ZERO
				gear.master = true
		}
	}

	this.events.on(classOf[Block.RightClickEvent]).bind((event: RightClickEvent) => {
			checkBigGear()
	})

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}

}

