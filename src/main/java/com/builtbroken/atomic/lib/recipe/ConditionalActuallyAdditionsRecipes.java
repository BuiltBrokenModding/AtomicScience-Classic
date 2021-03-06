package com.builtbroken.atomic.lib.recipe;

import com.builtbroken.atomic.config.mods.ConfigMod;
import com.builtbroken.atomic.proxy.Mods;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
public class ConditionalActuallyAdditionsRecipes implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        final boolean condition = Boolean.parseBoolean(JsonUtils.getString(json, "condition").toLowerCase());
        return () -> ((Mods.ACTUALLY_ADDITIONS.isLoaded() && ConfigMod.ACTUALLY_ADDITIONS.ENABLE_RECIPES) == condition);
    }
}
