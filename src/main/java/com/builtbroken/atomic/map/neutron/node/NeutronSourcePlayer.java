package com.builtbroken.atomic.map.neutron.node;

import com.builtbroken.atomic.lib.NeutronItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Wrappers an entity item as a source
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronSourcePlayer extends NeutronSourceEntity<EntityPlayer>
{
    public NeutronSourcePlayer(EntityPlayer player)
    {
        super(player);
    }

    @Override
    public int getNeutronStrength()
    {
        if (getHost().isEntityAlive())
        {
            int neutrons = 0;

            for (int slot = 0; slot < getHost().inventory.getSizeInventory(); slot++)
            {
                ItemStack slotStack = getHost().inventory.getStackInSlot(slot);
                if (slotStack != null)
                {
                    neutrons += NeutronItemHandler.getNeutronsForItem(slotStack);
                }
            }

            return neutrons;
        }
        return 0;
    }
}
