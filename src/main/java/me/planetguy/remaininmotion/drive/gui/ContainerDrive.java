package me.planetguy.remaininmotion.drive.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import me.planetguy.lib.prefab.ContainerPrefab;
import me.planetguy.remaininmotion.drive.TileEntityCarriageDrive;

public class ContainerDrive extends ContainerPrefab {

	public ContainerDrive(InventoryPlayer inventoryPlayer, TileEntity te) {
		super(inventoryPlayer, te);
	}

	@Override
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer){
    	for (int i = 0; i < 36; i++) {
    		addSlotToContainer(new Slot(inventoryPlayer, i,
    				100000000, 100000000));
    		//Player inventory slots still get added, but they're rendered in the stratosphere
    	}
	}

	@Override
	public void makeSlots(TileEntity teGeneral) {

	}

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return false;
    }
}
