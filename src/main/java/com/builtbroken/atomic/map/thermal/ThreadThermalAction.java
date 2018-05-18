package com.builtbroken.atomic.map.thermal;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.lib.thermal.HeatSpreadDirection;
import com.builtbroken.atomic.lib.thermal.ThermalHandler;
import com.builtbroken.atomic.map.MapHandler;
import com.builtbroken.atomic.map.data.DataChange;
import com.builtbroken.atomic.map.data.DataMap;
import com.builtbroken.atomic.map.data.DataPos;
import com.builtbroken.atomic.map.data.ThreadDataChange;
import com.builtbroken.jlib.lang.StringHelpers;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

/**
 * Handles updating the radiation map
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/28/2018.
 */
public class ThreadThermalAction extends ThreadDataChange
{
    public ThreadThermalAction()
    {
        super("ThreadThermalAction");
    }

    @Override
    protected void updateLocation(DataChange change)
    {
        //Get radiation exposure map
        DataMap map;
        synchronized (MapHandler.THERMAL_MAP)
        {
            map = MapHandler.THERMAL_MAP.getMap(change.dim, true);
        }

        final int cx = change.xi();
        final int cy = change.yi();
        final int cz = change.zi();

        HashMap<DataPos, DataPos> old_data = calculateHeatSpread(map, cx, cy, cz, change.old_value);
        HashMap<DataPos, DataPos> new_data = calculateHeatSpread(map, cx, cy, cz, change.new_value);

        ThermalMapChange mapChange = new ThermalMapChange(map, old_data, new_data);
        MapHandler.THERMAL_MAP.dataFromThread.add(mapChange);
    }

    /**
     * Calculates spread of heat from source.
     * <p>
     * Works by pathing all tiles and calculating heat movement towards current tile.
     * In other words: Pulls heat towards tile instead of pushing heat.
     * <p>
     * Heat is not consumed for movement as would be expected with real world movement. Instead
     * its assumed heat will constantly be generated. Thus migrating heat is not needed beyond
     * estimating how much heat would be moved.
     *
     * @param map  - map to pull data from
     * @param cx   - center of heat
     * @param cy   - center of heat
     * @param cz   - center of heat
     * @param heat - amount of heat to move
     * @return positions and changes (first pos is position, second is data (x -> heat, y -> heat used))
     */
    protected HashMap<DataPos, DataPos> calculateHeatSpread(final DataMap map, final int cx, final int cy, final int cz, final int heat)
    {
        final int range = 50;

        //Track data, also used to prevent looping same tiles (first pos is location, second stores data)
        final HashMap<DataPos, DataPos> heatSpreadData = new HashMap();

        long time = System.nanoTime();
        if (heat > 6)
        {
            //Track tiles to path
            final Queue<DataPos> pathNext = new LinkedList();

            //Add center point
            final DataPos centerPos = DataPos.get(cx, cy, cz);
            heatSpreadData.put(centerPos, DataPos.get(heat, 0, 0));

            //Add connected tiles
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
            {
                DataPos pos = DataPos.get(cx, cy, cz, direction);
                pathNext.add(pos);
                heatSpreadData.put(pos, DataPos.get(0, 0, 0));
            }

            //Temp list of  node to path next for current position
            ArrayList<DataPos> tempHold = new ArrayList(6);


            //Breadth first pathfinder
            while (!pathNext.isEmpty())
            {
                final DataPos currentPos = pathNext.poll();

                //Calculate heat pushed from all sides & look for new tiles to path
                int heatAsPosition = 0;

                double heatRateTotal = 0;
                for (HeatSpreadDirection direction : HeatSpreadDirection.values())
                {
                    int x = currentPos.x + direction.offsetX;
                    int y = currentPos.y + direction.offsetY;
                    int z = currentPos.z + direction.offsetZ;
                    if (inRange(cx, cy, cz, x, y, z, range) && y >= 0 && y < 256)
                    {
                        heatRateTotal += ThermalHandler.getHeatTransferRate(map.getWorld(), x, y, z);
                    }
                }

                for (HeatSpreadDirection direction : HeatSpreadDirection.values())
                {
                    final DataPos pos = DataPos.get(currentPos.x + direction.offsetX, currentPos.y + direction.offsetY, currentPos.z + direction.offsetZ);

                    //Only path tiles in map and in range of source
                    if (inRange(cx, cy, cz, pos.x, pos.y, pos.z, range) && pos.y >= 0 && pos.y < 256)
                    {
                        if (!heatSpreadData.containsKey(pos))
                        {
                            if (direction.ordinal() < 6)
                            {
                                tempHold.add(pos);
                            }
                        }
                        else
                        {
                            int heatAtNext = heatSpreadData.get(pos).x;
                            double transferRate = ThermalHandler.getHeatTransferRate(map.getWorld(), pos.x, pos.y, pos.z);
                            double percentage = transferRate / heatRateTotal;
                            int heatMoved = getHeatToSpread(map, pos, currentPos, heatAtNext, percentage * direction.percentage, heatSpreadData);
                            heatSpreadData.get(pos).y += heatMoved;
                            heatAsPosition += heatMoved;
                            pos.dispose();
                        }
                    }
                    else
                    {
                        pos.dispose();
                    }
                }

                //Only add positions from temp if there is heat to move from current
                if (heatAsPosition > 0)
                {
                    pathNext.addAll(tempHold);
                    tempHold.forEach(e -> heatSpreadData.put(e, DataPos.get(0, 0, 0))); //Prevents loop over tiles already in queue
                }
                else
                {
                    tempHold.forEach(e -> e.dispose());
                }
                tempHold.clear();

                //Keep track of value
                heatSpreadData.get(currentPos).x = heatAsPosition;
            }
        }

        if (AtomicScience.runningAsDev)
        {
            time = System.nanoTime() - time;
            AtomicScience.logger.info(String.format("%s: Spread heat %s | %s tiles | %s %s %s | in %s",
                    name,
                    heat,
                    heatSpreadData.size(),
                    cx, cy, cz,
                    StringHelpers.formatNanoTime(time)));
        }
        return heatSpreadData;
    }


    /**
     * Called to get the heat to spread to the target tile
     *
     * @param map            - map (do not add or remove data from, tbh don't even use)
     * @param heatSource     - source of heat
     * @param heatTarget     - target of heat
     * @param heatToMove     - total heat to move
     * @param heatSpreadData - data of current heat movement
     * @return heat moved
     */
    protected int getHeatToSpread(DataMap map, DataPos heatSource, DataPos heatTarget, final int heatToMove, final double percentage, HashMap<DataPos, DataPos> heatSpreadData)
    {
        if (map.blockExists(heatTarget.x, heatTarget.y, heatTarget.z))
        {
            //Get heat actual movement (only move 25% of heat)
            return getHeatSpread(map.getWorld(), heatSource, heatTarget, (int) Math.floor(heatToMove * percentage), heatSpreadData);
        }
        return heatToMove;
    }

    /**
     * Checks how much heat should spread from one block to the next.
     * <p>
     * In theory each block should have a different spread value. As
     * heat does not transfer evenly between sources.
     * <p>
     * As well heat travels differently between different types of blocks.
     * Air blocks will use convection while solid blocks direct heat transfer.
     *
     * @param heatSource - source of heat
     * @param heatTarget - where to move heat
     * @param heat       - heat to transfer (some % of total heat), in kilo-joules
     * @return heat to actually transfer, in kilo-joules
     */
    public int getHeatSpread(World world, DataPos heatSource, DataPos heatTarget, int heat, HashMap<DataPos, DataPos> heatSpreadData)
    {
        double deltaTemp = getTemperature(world, heatSource, heatSpreadData); //We assume target is zero relative to source
        if (deltaTemp > 0)
        {
            double heatMovementRate1 = ThermalHandler.getHeatTransferRate(world, heatTarget.x, heatTarget.y, heatTarget.z) * deltaTemp;
            //double heatMovementRate2 = ThermalHandler.getHeatTransferRate(world, heatSource.x, heatSource.y, heatSource.z) * deltaTemp;
            //double heatMovementRate = (heatMovementRate1 + heatMovementRate2) / 2;
            return (int) Math.min(heatMovementRate1 * 20 * 60, heat);
        }
        return 0;
    }

    public double getTemperature(World world, DataPos pos, HashMap<DataPos, DataPos> heatSpreadData)
    {
        if (heatSpreadData.containsKey(pos))
        {
            return MapHandler.THERMAL_MAP.getTemperature(world, pos.x, pos.y, pos.z, heatSpreadData.get(pos).x * 1000L); //kj -> j
        }
        return 0;
    }

}
