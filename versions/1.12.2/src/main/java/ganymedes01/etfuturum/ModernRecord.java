package ganymedes01.etfuturum;
import net.minecraft.item.ItemRecord;
/** Native jukebox insertion, comparator and playback use the registered released sound. */
public final class ModernRecord extends ItemRecord {
    public final String track;
    public final int comparator;
    ModernRecord(String track){super(track,ModernSounds.get("music_disc."+track));this.track=track;comparator=track.equals("5")?15:track.equals("relic")||track.equals("otherside")?14:track.equals("pigstep")||track.equals("precipice")?13:track.equals("creator")?12:track.equals("creator_music_box")?11:track.equals("tears")?10:9;}
}
