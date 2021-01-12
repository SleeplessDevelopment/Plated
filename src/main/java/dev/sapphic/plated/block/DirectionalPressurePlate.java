package dev.sapphic.plated.block;

import dev.sapphic.plated.PressurePlates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static dev.sapphic.plated.PressurePlates.AABBS;
import static dev.sapphic.plated.PressurePlates.FACING;
import static dev.sapphic.plated.PressurePlates.PRESSED_AABBS;
import static dev.sapphic.plated.PressurePlates.TOUCH_AABBS;

public final class DirectionalPressurePlate extends BlockPressurePlate {
  public static final Predicate<Block> PLACEMENT_EXCEPTIONS = Block::isExceptionBlockForAttaching;

  private BiFunction<World, AxisAlignedBB, Iterable<Entity>> collidingEntities;

  public DirectionalPressurePlate(final Material material, final Sensitivity sensitivity) {
    this(material, (world, aabb) -> {
      switch (sensitivity) {
        case EVERYTHING:
          return world.getEntitiesWithinAABBExcludingEntity(null, aabb);
        case MOBS:
          return world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        default:
          return Collections.emptyList();
      }
    });
  }

  public DirectionalPressurePlate(final Material material, final Class<? extends Entity> entityType) {
    this(material, (world, aabb) -> world.getEntitiesWithinAABB(entityType, aabb));
  }

  private DirectionalPressurePlate(final Material material, final BiFunction<World, AxisAlignedBB, Iterable<Entity>> collidingEntities) {
    super(material, Sensitivity.EVERYTHING);
    this.collidingEntities = collidingEntities;
    this.setDefaultState(this.getDefaultState().withProperty(FACING, EnumFacing.DOWN));
  }

  @Override
  @Deprecated
  public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
    return ((this.getRedstoneStrength(state) > 0) ? PRESSED_AABBS : AABBS).get(state.getValue(FACING));
  }

  @Override
  public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
    for (final EnumFacing face : EnumFacing.values()) {
      if (PressurePlates.canSurvive(face, world, pos)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
    if (PressurePlates.shouldBreak(state, world, pos)) {
      this.dropBlockAsItem(world, pos, state, 0);
      world.setBlockToAir(pos);
    }
  }

  @Override
  protected void updateState(final World world, final BlockPos pos, final IBlockState state, final int oldSignal) {
    final int signal = this.computeRedstoneStrength(world, pos);
    final boolean wasPressed = oldSignal > 0;
    final boolean isPressed = signal > 0;

    if (oldSignal != signal) {
      final IBlockState newState = this.setRedstoneStrength(state, signal);

      world.setBlockState(pos, newState, 2);
      PressurePlates.updateNeighbors(this, newState, world, pos);
      world.markBlockRangeForRenderUpdate(pos, pos);
    }

    if (!isPressed && wasPressed) {
      this.playClickOffSound(world, pos);
    } else if (isPressed && !wasPressed) {
      this.playClickOnSound(world, pos);
    }

    if (isPressed) {
      world.scheduleUpdate(new BlockPos(pos), this, this.tickRate(world));
    }
  }

  @Override
  public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
    if (this.getRedstoneStrength(state) > 0) {
      PressurePlates.updateNeighbors(this, state, world, pos);
    }
  }

  @Override
  @Deprecated
  protected void updateNeighbors(final World world, final BlockPos pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public int getStrongPower(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
    return (face == state.getValue(FACING).getOpposite()) ? this.getRedstoneStrength(state) : 0;
  }

  @Override
  @Deprecated
  public IBlockState withRotation(final IBlockState state, final Rotation rotation) {
    return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  @Deprecated
  public IBlockState withMirror(final IBlockState state, final Mirror mirror) {
    return state.withRotation(mirror.toRotation(state.getValue(FACING)));
  }

  @Override
  public Block setSoundType(final SoundType sound) {
    return super.setSoundType(sound);
  }

  @Override
  public boolean canPlaceBlockOnSide(final World world, final BlockPos pos, final EnumFacing face) {
    return this.canPlaceBlockAt(world, pos);
  }

  @Override
  public boolean hasTileEntity(final IBlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(final World world, final IBlockState state) {
    return new FacingDataBlockEntity(state.getValue(FACING));
  }

  @Override
  public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing axis) {
    final boolean ccw = axis.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
    final Rotation rotation = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
    return world.setBlockState(pos, world.getBlockState(pos).withRotation(rotation));
  }

  @Override
  public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing face, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer, final EnumHand hand) {
    final IBlockState state = super.getStateForPlacement(world, pos, face, hitX, hitY, hitZ, meta, placer, hand);

    if (PressurePlates.canSurvive(face, world, pos)) {
      return state.withProperty(FACING, face.getOpposite());
    }

    for (final EnumFacing facing : EnumFacing.values()) {
      if (face == facing) {
        continue;
      }

      if (PressurePlates.canSurvive(facing, world, pos)) {
        return state.withProperty(FACING, facing.getOpposite());
      }
    }

    return state;
  }

  @Override
  protected int computeRedstoneStrength(final World world, final BlockPos pos) {
    final AxisAlignedBB aabb = TOUCH_AABBS.get(world.getBlockState(pos).getValue(FACING)).offset(pos);

    for (final Entity entity : this.collidingEntities.apply(world, aabb)) {
      if (!entity.doesEntityNotTriggerPressurePlate()) {
        return 15;
      }
    }

    return 0;
  }

  @Override
  public IBlockState getStateFromMeta(final int meta) {
    return this.getDefaultState()
      .withProperty(FACING, EnumFacing.byIndex(meta >> 1))
      .withProperty(BlockPressurePlate.POWERED, (meta & 1) == 1);
  }

  @Override
  public int getMetaFromState(final IBlockState state) {
    return (state.getValue(FACING).getIndex() << 1) | (state.getValue(BlockPressurePlate.POWERED) ? 1 : 0);
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, FACING, POWERED);
  }
}
