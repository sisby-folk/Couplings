package dev.sapphic.couplings;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class CouplingsConfig extends ReflectiveConfig {
    @Comment("Couple regardless of whether the player is sneaking")
    public final TrackedValue<Boolean> ignoreSneaking = value(true);
    @Comment("Couple doors with opposing hinges")
    public final TrackedValue<Boolean>  coupleDoors = value(true);
    @Comment("Couple fence gates above and below on the same axis")
    public final TrackedValue<Boolean>  coupleFenceGates = value(true);
    @Comment("Couple trapdoors along either sides and opposing")
    public final TrackedValue<Boolean>  coupleTrapdoors = value(true);
}
