package ganymedes01.etfuturum.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/** Released dry-foliage palette sampled with each target's native biome climate. */
public final class DryFoliageColors {
    private static final int[] COLORS=load();
    private DryFoliageColors(){}
    private static int[] load(){
        try(InputStream stream=DryFoliageColors.class.getResourceAsStream("/assets/etfuturum/textures/colormap/dry_foliage.png")){
            if(stream==null)throw new IOException("Missing dry foliage palette");
            BufferedImage image=ImageIO.read(stream);
            if(image==null||image.getWidth()!=256||image.getHeight()!=256)throw new IOException("Invalid dry foliage palette");
            return image.getRGB(0,0,256,256,null,0,256);
        }catch(IOException failure){throw new IllegalStateException("Cannot load dry foliage colors",failure);}
    }
    public static int color(double temperature,double rainfall){
        double heat=Math.max(0,Math.min(1,temperature)),moisture=Math.max(0,Math.min(1,rainfall))*heat;
        return COLORS[(int)((1-moisture)*255)*256+(int)((1-heat)*255)]&0xffffff;
    }
}
