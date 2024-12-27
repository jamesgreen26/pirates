package ace.actually.pirates.mixin;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.util.ShipStructurePlacementHelper;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/*
*    VS Crumbles compatibility :)
*/



@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow private String author;

    @Shadow public abstract void setAuthor(String author);

    @Shadow public abstract boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags);

    @Shadow
    protected static BlockBox createBox(BlockPos pos, BlockRotation rotation, BlockPos pivot, BlockMirror mirror, Vec3i dimensions) {
        return null;
    }

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    public void placeMixin(ServerWorldAccess world, BlockPos oldPos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags, CallbackInfoReturnable<Boolean> cir) {
        boolean placed;
        if (this.author.equals("pirate-ship")) {
            if (placementData.getBoundingBox() != null) {
                ShipStructurePlacementHelper.placeShipTemplate(
                        (StructureTemplate) (Object) this,
                        world.toServerWorld(),
                        placementData.getBoundingBox().getCenter());
                placed = true;
            } else {
                placed = false;
                Pirates.LOGGER.info("Template attempted to generate with null bounding box");
            }
            this.setAuthor("?");
            cir.setReturnValue(placed);
            cir.cancel();
        }
    }
}
