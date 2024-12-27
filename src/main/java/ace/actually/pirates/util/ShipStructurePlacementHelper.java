package ace.actually.pirates.util;

import kotlin.Triple;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class ShipStructurePlacementHelper {

    public static final Queue<Triple<StructureTemplate, ServerWorld, BlockPos>> shipQueue = new ArrayDeque<>();

    private static final Set<BlockPos> blacklist = new HashSet<>();

    public static void placeShipTemplate(StructureTemplate structureTemplate, ServerWorld world, BlockPos centrePos) {
        shipQueue.add(new Triple<>(structureTemplate, world, centrePos));
    }

    public static void createShip (StructureTemplate structureTemplate, ServerWorld world, BlockPos blockPos) {
        if (blacklist.contains(blockPos)) return;
        blacklist.add(blockPos);

        ServerShip newShip = VSGameUtilsKt.getShipObjectWorld(world).createNewShipAtBlock(
                VectorConversionsMCKt.toJOML(blockPos),
                false,
                1.0,
                VSGameUtilsKt.getDimensionId(world));

        BlockPos centerPos = VectorConversionsMCKt.toBlockPos(newShip.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange(world), new Vector3i()));

        StructurePlacementData structurePlacementData = new StructurePlacementData();
        structureTemplate.place(world, centerPos, centerPos, structurePlacementData, Random.create(), 2);
    }
}
