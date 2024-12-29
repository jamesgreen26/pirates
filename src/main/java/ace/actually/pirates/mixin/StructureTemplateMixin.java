package ace.actually.pirates.mixin;

import ace.actually.pirates.Pirates;
import ace.actually.pirates.structures.ShipStructurePlacementHelper;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;


@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow private String author;

    @Shadow public abstract void setAuthor(String author);

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    public void placeMixin(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (VSGameUtilsKt.isBlockInShipyard(world.toServerWorld(), pos)) return;

        boolean placed;
        if (this.author.equals("pirate-ship")) {
            if (placementData.getBoundingBox() != null) {
                ShipStructurePlacementHelper.placeShipTemplate(
                        (StructureTemplate) (Object) this,
                        world.toServerWorld(),
                        placementData.getBoundingBox().getCenter());
                placed = true;
            } else {
                ShipStructurePlacementHelper.placeShipTemplate(
                        (StructureTemplate) (Object) this,
                        world.toServerWorld(),
                        pos);
                Pirates.LOGGER.info("Template generated with null bounding box");
                placed = true;
            }
            this.setAuthor("dirty");
            cir.setReturnValue(placed);
            cir.cancel();
        } else if (this.author.equals("dirty")) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
