package dev.sapphic.couplings;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class CouplingsConfig extends WrappedConfig {
    @Comment("Couple regardless of whether the player is sneaking")
    boolean ignoreSneaking = false;
    @Comment("Couple doors with opposing hinges")
    boolean coupleDoors = true;
    @Comment("Couple fence gates above and below on the same axis")
    boolean coupleFenceGates = true;
    @Comment("Couple trapdoors along either sides and opposing")
    boolean coupleTrapdoors = true;
}
