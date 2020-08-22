package com.builtbroken.atomic.lib;

import com.builtbroken.atomic.api.neutron.INeutronItem;
import net.minecraft.item.ItemStack;

/**
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronItemHandler
{
    public static int getNeutronsForItem(ItemStack stack)
    {
        int neutrons = 0;
        if (stack != null && stack.getItem() instanceof INeutronItem)
        {
            return ((INeutronItem) stack.getItem()).getNeutronStrength(stack);
        }
        //TODO read NBT for rad material
        return neutrons;
    }

}
