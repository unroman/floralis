package com.luxtracon.floralis.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import java.util.Random;

@SuppressWarnings("deprecation")
public class CactusCropBlock extends Block implements BonemealableBlock, IPlantable {
	private static final VoxelShape[] SHAPES = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D), Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D)};
	private static final IntegerProperty AGE = IntegerProperty.create("age", 0, 5);
	private static final int MAX_AGE = 5;

	public CactusCropBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
	}

	public boolean canSurvive(@Nonnull BlockState pState, @Nonnull LevelReader pLevel, @Nonnull BlockPos pPos) {
		BlockPos posBelow = pPos.below();
		return (pLevel.getRawBrightness(pPos, 0) >= 8 || pLevel.canSeeSky(pPos)) && this.mayPlaceOn(pLevel.getBlockState(posBelow));
	}

	public boolean isBonemealSuccess(@Nonnull Level pLevel, @Nonnull Random pRandom, @Nonnull BlockPos pPos, @Nonnull BlockState pState) {
		return this.isNotMaxAge(pState);
	}

	public boolean isNotMaxAge(@Nonnull BlockState pState) {
		return pState.getValue(this.getAgeProperty()) < this.getMaxAge();
	}

	public boolean isRandomlyTicking(@Nonnull BlockState pState) {
		return this.isNotMaxAge(pState);
	}

	public boolean isValidBonemealTarget(@Nonnull BlockGetter pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pState, boolean pIsClient) {
		return this.isNotMaxAge(pState);
	}

	public boolean mayPlaceOn(@Nonnull BlockState pState) {
		return pState.is(BlockTags.SAND);
	}

	public static float getGrowthSpeed(Block pBlock, BlockGetter pLevel, @Nonnull BlockPos pPos) {
		BlockPos posBelow = pPos.below();
		float floatFirst = 1.0F;
		for(int i = -1; i <= 1; ++i) {
			for(int j = -1; j <= 1; ++j) {
				float floatSecond = 0.0F;
				if (pLevel.getBlockState(posBelow.offset(i, 0, j)).canSustainPlant(pLevel, posBelow.offset(i, 0, j), Direction.UP, (IPlantable) pBlock)) {
					floatSecond = 1.0F;
					if (pLevel.getBlockState(posBelow.offset(i, 0, j)).isFertile(pLevel, pPos.offset(i, 0, j))) {
						floatSecond = 3.0F;
					}
				}

				if (i != 0 || j != 0) {
					floatSecond /= 4.0F;
				}

				floatFirst += floatSecond;
			}
		}

		BlockPos posNorth = pPos.north();
		BlockPos posSouth = pPos.south();
		BlockPos posWest = pPos.west();
		BlockPos posEast = pPos.east();

		boolean flagFirst = pLevel.getBlockState(posWest).is(pBlock) || pLevel.getBlockState(posEast).is(pBlock);
		boolean flagSecond = pLevel.getBlockState(posNorth).is(pBlock) || pLevel.getBlockState(posSouth).is(pBlock);

		if (flagFirst && flagSecond) {
			floatFirst /= 2.0F;
		} else {
			boolean flagThird = pLevel.getBlockState(posWest.north()).is(pBlock) || pLevel.getBlockState(posEast.north()).is(pBlock) || pLevel.getBlockState(posEast.south()).is(pBlock) || pLevel.getBlockState(posWest.south()).is(pBlock);
			if (flagThird) {
				floatFirst /= 2.0F;
			}
		}

		return floatFirst;
	}

	public int getAge(@Nonnull BlockState pState) {
		return pState.getValue(this.getAgeProperty());
	}

	public int getBonemealAgeIncrease(@Nonnull Level pLevel) {
		return Mth.nextInt(pLevel.random, 1, 3);
	}

	public int getMaxAge() {
		return MAX_AGE;
	}

	public void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AGE);
	}

	public void entityInside(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull Entity pEntity) {
		if (pEntity instanceof Ravager && ForgeEventFactory.getMobGriefingEvent(pLevel, pEntity)) {
			pLevel.destroyBlock(pPos, true, pEntity);
		}

		if (pEntity instanceof Villager) {
			pEntity.setInvulnerable(true);
		}

		if (this.getAge(pState) >= 3) {
			pEntity.hurt(DamageSource.CACTUS, 1.0F);
		}
	}

	public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
		int i = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
		if (i > this.getMaxAge()) {
			i = this.getMaxAge();
		}

		pLevel.setBlock(pPos, this.getStateForAge(i), 2);
	}

	public void performBonemeal(@Nonnull ServerLevel pLevel, @Nonnull Random pRandom, @Nonnull BlockPos pPos, @Nonnull BlockState pState) {
		this.growCrops(pLevel, pPos, pState);
	}

	public void randomTick(@Nonnull BlockState pState, @Nonnull ServerLevel pLevel, @Nonnull BlockPos pPos, @Nonnull Random pRandom) {
		if (!pLevel.isAreaLoaded(pPos, 1)) {
			return;
		}

		if (pLevel.getRawBrightness(pPos, 0) >= 8) {
			if (this.getAge(pState) < this.getMaxAge()) {
				if (ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / getGrowthSpeed(this, pLevel, pPos)) + 1) == 0)) {
					pLevel.setBlock(pPos, this.getStateForAge(this.getAge(pState) + 1), 2);
					ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
				}
			}
		}
	}

	public BlockState getPlant(@Nonnull BlockGetter world, BlockPos pos) {
		return world.getBlockState(pos);
	}

	public BlockState getStateForAge(int pAge) {
		return this.defaultBlockState().setValue(this.getAgeProperty(), pAge);
	}

	public @Nonnull BlockState updateShape(@Nonnull BlockState pState, @Nonnull Direction pFacing, @Nonnull BlockState pFacingState, @Nonnull LevelAccessor pLevel, @Nonnull BlockPos pCurrentPos, @Nonnull BlockPos pFacingPos) {
		return !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	public @Nonnull IntegerProperty getAgeProperty() {
		return AGE;
	}

	public PlantType getPlantType(BlockGetter world, BlockPos pos) {
		return PlantType.CROP;
	}

	public @Nonnull VoxelShape getCollisionShape(@Nonnull BlockState pState, @Nonnull BlockGetter pLevel, @Nonnull BlockPos pPos, @Nonnull CollisionContext pContext) {
		return SHAPES[pState.getValue(this.getAgeProperty())];
	}

	public @Nonnull VoxelShape getShape(@Nonnull BlockState pState, @Nonnull BlockGetter pLevel, @Nonnull BlockPos pPos, @Nonnull CollisionContext pContext) {
		return SHAPES[pState.getValue(this.getAgeProperty())];
	}
}
