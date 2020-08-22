package com.builtbroken.atomic.content.effects.client;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.client.ClientProxy;
import com.builtbroken.atomic.config.client.ConfigClient;
import com.builtbroken.atomic.config.logic.ConfigRadiation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/22/2018.
 */
public class RenderRadOverlay
{
    public static final Color startColor = new Color(8453920);
    public static final Color endColor = new Color(0xFF390B);
    public static final RenderRadOverlay INSTANCE = new RenderRadOverlay();

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event)
    {
        if(ConfigClient.RADIATION_DISPLAY.ENABLE && !Minecraft.getMinecraft().gameSettings.showDebugInfo && !Minecraft.getMinecraft().isReducedDebug())
        {
            int width = event.getResolution().getScaledWidth();
            int height = event.getResolution().getScaledHeight();
            Minecraft mc = Minecraft.getMinecraft();

            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
            {
                //Start
                GL11.glPushMatrix();
                //GL11.glTranslatef(0, 0, 0.0F);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                //Position TODO config TODO fire event
                int left = 5;
                int top = 5;

                //Get data
                final float rad_player = interpolate(ClientProxy.PREV_RAD_PLAYER, ClientProxy.RAD_PLAYER, event.getPartialTicks());
                final float rad_area = interpolate(ClientProxy.PREV_RAD_EXPOSURE, ClientProxy.RAD_EXPOSURE, event.getPartialTicks());
                final float rad_dead_min = ConfigRadiation.RADIATION_DEATH_POINT / (60 * 20); //Radiation needed to die in 1 min
                final float neutron_area = interpolate(ClientProxy.PREV_NEUT_EXPOSURE, ClientProxy.NEUT_EXPOSURE, event.getPartialTicks());

                //Format
                String remDisplay = formatDisplay("PER:", rad_player, "rem");
                String radDisplay = formatDisplay("ENV: ", rad_area * 20, "rem/s");
                String neuDisplay = formatDisplay("NEUT: ", neutron_area * 20, "neu/s");

                //Render
                renderTextWithShadow(remDisplay, left, top, interpolate(startColor, endColor, rad_player / ConfigRadiation.RADIATION_DEATH_POINT).getRGB());
                renderTextWithShadow(radDisplay, left, top + 10, interpolate(startColor, endColor, rad_area / rad_dead_min).getRGB());
                renderTextWithShadow(neuDisplay, left, top + 20, interpolate(startColor, endColor, neutron_area / rad_dead_min).getRGB());

                if (AtomicScience.runningAsDev)
                {
                    renderTextWithShadow("" + ClientProxy.RAD_REMOVE_TIMER, left, top + 20, endColor.getRGB());
                }

                //Set prev
                ClientProxy.PREV_RAD_PLAYER = rad_player;
                ClientProxy.PREV_RAD_EXPOSURE = rad_area;
                ClientProxy.PREV_NEUT_EXPOSURE = neutron_area;

                //End
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glPopMatrix();
            }
        }
    }

    /**
     * Used by the overlay to render text with shadow behind the text
     *
     * @param text
     * @param x
     * @param y
     * @param colorRGB
     */
    public static void renderTextWithShadow(String text, int x, int y, int colorRGB)
    {
        GL11.glPushMatrix();
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
        fontrenderer.drawString(text, x + 1, y, 0);
        fontrenderer.drawString(text, x - 1, y, 0);
        fontrenderer.drawString(text, x, y + 1, 0);
        fontrenderer.drawString(text, x, y - 1, 0);
        fontrenderer.drawString(text, x, y, colorRGB);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    protected Color interpolate(Color color, Color color2, float per)
    {
        //Keep in range to prevent crash
        per = Math.max(0, Math.min(1, per));

        //Move numbers
        float r = interpolate(color.getRed() / 255f, color2.getRed() / 255f, per);
        float g = interpolate(color.getGreen() / 255f, color2.getGreen() / 255f, per);
        float b = interpolate(color.getBlue() / 255f, color2.getBlue() / 255f, per);

        //New color
        return new Color(r, g, b);
    }

    private float interpolate(float a, float b, float proportion)
    {
        if (Float.isNaN(a))
        {
            return Float.isNaN(b) ? 0 : b;
        }
        else if (Float.isNaN(b))
        {
            return Float.isNaN(a) ? 0 : a;
        }
        return (a + ((b - a) * proportion));
    }

    protected String formatDisplay(String prefix, float number, String sufix)
    {
        return String.format("%s %6s%s", prefix, String.format("%.2f", number), sufix); //likely a better way but it works
    }

}
