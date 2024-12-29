package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.blocks.MotionInvokingBlock;
import ace.actually.pirates.util.ConfigUtils;
import ace.actually.pirates.util.PatternProcessor;
import ace.actually.pirates.Pirates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.eureka.block.ShipHelmBlock;
import org.valkyrienskies.eureka.ship.EurekaShipControl;
import org.valkyrienskies.eureka.util.ShipAssembler;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;
import org.valkyrienskies.mod.common.util.DimensionIdProvider;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.util.RelocationUtilKt;

import java.util.List;

import static net.minecraft.state.property.Properties.HORIZONTAL_FACING;

public class MotionInvokingBlockEntity extends BlockEntity {
    NbtList instructions = new NbtList();
    long nextInstruction = 0;

    public NbtList getInstructions() {return instructions;}
    public void setNextInstruction(long nextInstruction) {this.nextInstruction = nextInstruction;}
    public void advanceInstructionList() {instructions.add(instructions.remove(0));}

    private static int updateTicks = -1;

    public MotionInvokingBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.MOTION_INVOKING_BLOCK_ENTITY, pos, state);
    }



    public static void tick(World world, BlockPos pos, BlockState state, MotionInvokingBlockEntity be) {

        if (!(world.getBlockState(pos.up()).getBlock() instanceof ShipHelmBlock)) {
            return;
        }
        if(updateTicks==-1)
        {
            updateTicks = Integer.parseInt(ConfigUtils.config.getOrDefault("controlled-ship-updates","100"));
        }

        if (be.instructions.isEmpty() && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD)) {

            if(world.random.nextBoolean())
            {
                be.setPattern("circle.pattern");
            }
            else
            {
                be.setPattern("rcircle.pattern");
            }

        }
        if (!world.isClient && world.getGameRules().getBoolean(Pirates.PIRATES_IS_LIVE_WORLD) && world.getTime() >= be.nextInstruction) {

            if (VSGameUtilsKt.isBlockInShipyard(world, pos)) {


                ChunkPos chunkPos = world.getChunk(pos).getPos();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerWorld) world, chunkPos);

                //Pirates.LOGGER.info("scaling of ship: "+s.x()+" "+s.y()+" "+s.z());

                if (ship != null) {
                    SeatedControllingPlayer seatedControllingPlayer = ship.getAttachment(SeatedControllingPlayer.class);
                    if (seatedControllingPlayer == null) {
                        if (world.getBlockState(pos.up()).getBlock() instanceof ShipHelmBlock) {
                            seatedControllingPlayer = new SeatedControllingPlayer(world.getBlockState(pos.up()).get(HORIZONTAL_FACING).getOpposite());
                        } else {
                            return;
                        }
                        ship.setAttachment(SeatedControllingPlayer.class, seatedControllingPlayer);
                    }


                    //Pirates.LOGGER.info(be.instructions.getString(0));
                    //be.utiliseInternalPattern(seatedControllingPlayer, be);
                    //be.moveShipForward(ship);
                    if(world.getTimeOfDay()%updateTicks==0)
                    {
                        List<Ship> ships = VSGameUtilsKt.getAllShips(world).stream().filter(a->
                        {
                            if(a.getId()==ship.getId()) return false;
                            Vector3dc f1 = ship.getTransform().getPositionInWorld();
                            Vector3dc f2 = a.getTransform().getPositionInWorld();
                            return f1.distanceSquared(f2)<10000;
                        }).toList();
                        if(!ships.isEmpty())
                        {
                            Vector3dc o = ships.get(0).getTransform().getPositionInWorld();
                            be.setTarget(new int[]{(int) o.x(), (int) o.y(), (int) o.z()});
                        }
                    }

                    be.moveTowards(seatedControllingPlayer,ship);

                }
            }
        }

    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putLong("nextInstruction", nextInstruction);
        nbt.put("instructions", instructions);
        nbt.putIntArray("target",target);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        instructions = (NbtList) nbt.get("instructions");
        nextInstruction = nbt.getLong("nextInstruction");
        if(nbt.contains("target"))
        {
            target = nbt.getIntArray("target");
        }

    }


    public void setPattern(String loc) {
        instructions = PatternProcessor.loadPattern(loc);
        nextInstruction = world.getTime() + 10;
        markDirty();
    }

    public void setTarget(int[] target) {
        this.target = target;
        markDirty();
    }

    int[] target = new int[3];
    double ldx = -1;
    double ldz = -1;
    int flipflop = 1;

    private void moveTowards(SeatedControllingPlayer power, LoadedServerShip ship)
    {
        if(target.length!=3) return;

        power.setForwardImpulse(1);
        Vector3dc v3d = ship.getTransform().getPositionInWorld();
        if(ldx==-1)
        {
            //lastDistance = v3d.distanceSquared(target[0],target[1],target[2]);
            ldx = vdis(target[0],v3d.x());
            ldz = vdis(target[2],v3d.z());
        }
        else
        {
            //double currentDistance = v3d.distanceSquared(target[0],target[1],target[2]);
            double cdx = vdis(target[0],v3d.x());
            double cdz = vdis(target[2],v3d.z());
            //System.out.println(lastDistance+" -> "+currentDistance);
            if(cdx>=ldx || cdz>=ldz)
            {
                power.setLeftImpulse(flipflop);

            }
            else
            {
                power.setLeftImpulse(0);
                flipflop = -flipflop;
            }
            ldx=cdx;
            ldz=cdz;
        }

    }

    private double vdis(double x, double xto) {
        return Math.abs(x-xto);
    }
}
