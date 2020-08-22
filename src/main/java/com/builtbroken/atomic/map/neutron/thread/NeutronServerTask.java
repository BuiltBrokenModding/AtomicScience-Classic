package com.builtbroken.atomic.map.neutron.thread;

import com.builtbroken.atomic.api.neutron.INeutronNode;
import com.builtbroken.atomic.api.neutron.INeutronSource;
import com.builtbroken.atomic.map.neutron.node.NeutronNode;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pu-238 on 08/22/2020.
 */
public class NeutronServerTask implements Runnable
{
    public final INeutronSource source;
    public final HashMap<BlockPos, Integer> collectedData;

    public NeutronServerTask(INeutronSource source, HashMap<BlockPos, Integer> collectedData)
    {
        this.source = source;
        this.collectedData = collectedData;
    }

    @Override
    public void run()
    {
        //Get data
        final HashMap<BlockPos, INeutronNode> oldMap = source.getCurrentNodes();
        final HashMap<BlockPos, INeutronNode> newMap = new HashMap();

        //Remove old data from map
        source.disconnectMapData();

        //Add new data, recycle old nodes to reduce memory churn
        for (Map.Entry<BlockPos, Integer> entry : collectedData.entrySet()) //TODO move this to source to give full control over data structure
        {
            final int value = entry.getValue();
            final BlockPos pos = entry.getKey();

            if (oldMap != null && oldMap.containsKey(pos))
            {
                final INeutronNode node = oldMap.get(pos);
                if (node != null)
                {
                    //Update value
                    node.setNeutronValue(value);

                    //Store in new map
                    newMap.put(pos, node);
                }

                //Remove from old map
                oldMap.remove(pos);
            }
            else
            {
                newMap.put(pos, NeutronNode.get(source, value));
            }
        }

        //Clear old data
        source.disconnectMapData();
        source.clearMapData();

        //Set new data
        source.setCurrentNodes(newMap);

        //Tell the source to connect to the map
        source.connectMapData();

        //Trigger source update
        source.initMapData();
    }
}
