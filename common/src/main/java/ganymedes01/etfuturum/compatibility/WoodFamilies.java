package ganymedes01.etfuturum.compatibility;

import java.util.*;

/** Source materials for the existing wood derivative systems; identities remain target-native. */
public final class WoodFamilies {
    public static final class Family {
        public final String provider, name, planks, log, texture;
        public final int plankMeta, logMeta;
        Family(String provider, String name, String planks, int plankMeta, String log, int logMeta) {
            this.provider=provider; this.name=name; this.planks=planks; this.plankMeta=plankMeta; this.log=log; this.logMeta=logMeta;
            this.texture=name.equals("sacred_oak")?"sacredoak":name.equals("big_flower_stem")?"bigflowerstem":name.equals("umbran")?"dark":name;
        }
    }
    private WoodFamilies() { }
    public static List<Family> forTarget(String target) {
        List<Family> result = new ArrayList<>();
        if (target.equals("1.7.10")) {
            String[] names={"sacred_oak","cherry","dark","fir","ethereal","magic","mangrove","palm","redwood","willow","bamboo","pine","hellbark","jacaranda","mahogany"};
            for (int meta=0;meta<names.length;meta++) result.add(new Family("bop",names[meta],"BiomesOPlenty:planks",meta,
                    meta==10?null:"BiomesOPlenty:logs"+(meta<10?meta/4+1:4),meta<10?meta%4:meta-11));
            result.add(new Family("bop","dead",null,-1,"BiomesOPlenty:logs3",2));
            result.add(new Family("bop","big_flower_stem",null,-1,"BiomesOPlenty:logs3",3));
            String[] witches={"rowan","alder","hawthorn"};
            for(int meta=0;meta<witches.length;meta++) result.add(new Family("witchery",witches[meta],"witchery:witchwood",meta,"witchery:witchlog",meta));
        } else if (target.equals("1.12.2")) {
            String[] names={"sacred_oak","cherry","umbran","fir","ethereal","magic","mangrove","palm","redwood","willow","pine","hellbark","jacaranda","mahogany","ebony","eucalyptus"};
            for(int meta=0;meta<names.length;meta++) result.add(new Family("bop",names[meta],"biomesoplenty:planks_0",meta,"biomesoplenty:log_"+(meta/4),meta%4));
            // Witchery has no maintained native 1.12.2 release. Do not guess another mod's registries.
        } else throw new IllegalArgumentException(target);
        return Collections.unmodifiableList(result);
    }
}
