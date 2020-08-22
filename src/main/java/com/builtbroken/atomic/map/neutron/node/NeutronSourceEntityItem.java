package com.builtbroken.atomic.map.neutron.node;

import com.builtbroken.atomic.api.neutron.INeutronItem;
import com.builtbroken.atomic.config.logic.ConfigRadiation;
import com.builtbroken.atomic.lib.NeutronItemHandler;
import net.minecraft.entity.item.EntityItem;

/**
 * Wrappers an entity item as a source
 *
 *
 * Created by Pu-238 on 8/22/2020.
 */
public class NeutronSourceEntityItem extends NeutronSourceEntity<EntityItem>
{
    public NeutronSourceEntityItem(EntityItem entity)
    {
        super(entity);
    }

    @Override
    public int getNeutronStrength()
    {
        return NeutronItemHandler.getNeutronsForItem(getHost().getItem());
    }

    public static NeutronSourceEntityItem build(EntityItem entityItem)
    {
        if (ConfigRadiation.ENABLE_ENTITY_ITEMS)
        {
            if (entityItem.getItem() != null && entityItem.getItem().getItem() instanceof INeutronItem) //TODO change over to capability
            {
                return new NeutronSourceEntityItem(entityItem);
            }
        }
        return null;
    }
}
