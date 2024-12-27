package ace.actually.pirates.util;

import net.minecraft.state.property.EnumProperty;

public interface CrewTypes {
    EnumProperty<CrewSpawnType> CREW_SPAWN_TYPE = EnumProperty.of("crew_spawn_type", CrewSpawnType.class);
}
