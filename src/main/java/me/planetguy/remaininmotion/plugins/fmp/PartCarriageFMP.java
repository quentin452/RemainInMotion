package me.planetguy.remaininmotion.plugins.fmp;

import java.util.Iterator;

import me.planetguy.lib.util.Debug;
import me.planetguy.lib.util.EmptyIterator;
import me.planetguy.lib.util.SingleIterator;
import me.planetguy.remaininmotion.base.ToolItemSet;
import me.planetguy.remaininmotion.api.ConnectabilityState;
import me.planetguy.remaininmotion.api.ICloseable;
import me.planetguy.remaininmotion.core.RIMBlocks;
import me.planetguy.remaininmotion.core.RiMConfiguration;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.CommonMicroblock;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McBlockPart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "JNormalOcclusion", modid = "ForgeMultipart")
public class PartCarriageFMP extends McBlockPart implements JNormalOcclusion, ICloseable {

	private boolean[]				sidesClosed	= new boolean[6];

	public static PartCarriageFMP	instance;

	@SideOnly(Side.CLIENT)
	FMPRenderer						renderer;

	public PartCarriageFMP() {
		if (FMLCommonHandler.instance().getSide().isClient()) {
			renderer = new FMPRenderer();
		}
	}

	static final double				l					= 1.5 / 8;

	// The amount the cuboid extends outside
	static final double				e					= 0.01;

	public static final Cuboid6[]	cubeOutsideEdges	= new Cuboid6[] { // not
			// most
			// efficient,
			// but
			// understandable,
			// system;
			new Cuboid6(0 - e, 0 - e, 0 - e, 1 + e, l, l), new Cuboid6(0 - e, 0 - e, 0 - e, l, 1 + e - l, l),
			new Cuboid6(0 - e, 0 - e, 0 - e, l, l, 1 + e),

			new Cuboid6(1 + e, 1 + e, 0 - e, 0 - e, 1 + e - l, l), new Cuboid6(1 + e, 1 + e, 0 - e, 1 + e - l, l, l),
			new Cuboid6(1 + e, 1 + e, 0 - e, 1 + e - l, 1 + e - l, 1 + e - l),

			new Cuboid6(1 + e, 0 - e, 1 + e, 1 + e - l, l, l),
			new Cuboid6(1 + e, 0 - e, 1 + e, 1 + e - l, 1 + e - l, 1 + e - l),
			new Cuboid6(1 + e, 0 - e, 1 + e, l, l, 1 + e - l),

			new Cuboid6(0 - e, 1 + e, 1 + e, 1 + e, 1 + e - l, 1 + e - l),
			new Cuboid6(0 - e, 1 + e, 1 + e, l, l, 1 + e - l), new Cuboid6(0 - e, 1 + e, 1 + e, l, 1 + e - l, l),

														};

	@Optional.Method(modid = "ForgeMultipart")
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() {
		return (Iterable<Cuboid6>) EmptyIterator.instance;
	}

	@Override
	@Optional.Method(modid = "ForgeMultipart")
	public Iterable<Cuboid6> getCollisionBoxes() {
		return (Iterable<Cuboid6>) new SingleIterator(Cuboid6.full);
	}

	@Optional.Method(modid = "ForgeMultipart")
	@Override
	public String getType() {
		return "FMPCarriage";
	}

	@Override
	@Optional.Method(modid = "ForgeMultipart")
	public Cuboid6 getBounds() {
		return Cuboid6.full;
	}

	@Optional.Method(modid = "ForgeMultipart")
	@Override
	public Block getBlock() {
		return RIMBlocks.Carriage;
	}

	@Override
	@Optional.Method(modid = "ForgeMultipart")
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) {
		renderer.renderCovers(world(), pos, pass, this);
		return pass == 0;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack held) {
		if (ToolItemSet.IsScrewdriverOrEquivalent(held)) {
			sidesClosed[hit.sideHit] = !sidesClosed[hit.sideHit];
			tile().markDirty();
			tile().markRender();
			return true;
		}
		return false;
	}

	@Override
	public ConnectabilityState isSideClosed(int side) {
		return treatSideAsClosed(side) ? ConnectabilityState.CLOSED : ConnectabilityState.OPEN;
	}

	public boolean treatSideAsClosed(int side) {
		return sidesClosed[side] || isSideCovered(side);
	}

	public boolean drawSideClosedJAKJ(int side) {
		return sidesClosed[side];
	}

	private boolean isSideCovered(int side) {
		if (RiMConfiguration.Debug.verbose) {
			Debug.dbg(side);
		}
		for (TMultiPart part : tile().jPartList()) {
			if (RiMConfiguration.Debug.verbose) {
				Debug.dbg(part.getClass());
			}
			if (part instanceof CommonMicroblock) {
				if (RiMConfiguration.Debug.verbose) {
					Debug.dbg(part.getClass());
				}
				CommonMicroblock mb = (CommonMicroblock) part;
				if (mb.getSlot() == side) {
					int size = mb.getSize();
					return size == 1;
				}
			} else if (!(part instanceof PartCarriageFMP)) {

			}
		}
		return false;
	}

	@Override
	public void writeDesc(MCDataOutput packet) {
		super.writeDesc(packet);
		packet.writeByte((byte) toInt());
	}

	@Override
	public void readDesc(MCDataInput packet) {
		super.readDesc(packet);
		fromInt(packet.readByte());
	}

	@Override
	public void save(NBTTagCompound tag) {
		super.save(tag);
		tag.setByte("sideFlags", (byte) toInt());
	}

	/**
	 * Load part from NBT (only called serverside)
	 */
	@Override
	public void load(NBTTagCompound tag) {
		super.load(tag);
		fromInt(tag.getByte("sideFlags"));
	}

	public int toInt() {
		int i = 0;
		int pos = 1;
		for (int index = 0; index < sidesClosed.length; index++) {
			if (sidesClosed[index]) {
				i |= pos;
			}
			pos = pos << 1;
		}
		return i;
	}

	public void fromInt(int i) {
		sidesClosed = new boolean[6];
		int pos = 1;
		for (int index = 0; index < sidesClosed.length; index++) {
			sidesClosed[index] = (i & pos) != 0;
			pos = pos << 1;
		}
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit) {
		return new ItemStack(FMPCarriagePlugin.hollowCarriage);
	}

	public Iterable<ItemStack> getDrops() {
		return (Iterable<ItemStack>) new SingleIterator(pickItem(null));
	}

}
