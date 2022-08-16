package com.acikek.theprinter.client.render;

import com.acikek.theprinter.ThePrinter;
import com.acikek.theprinter.block.PrinterBlock;
import com.acikek.theprinter.block.PrinterBlockEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class PrinterBlockEntityRenderer implements BlockEntityRenderer<PrinterBlockEntity> {

	public static final Identifier PROGRESS_BAR = ThePrinter.id("textures/block/progress_bar.png");

	public ItemRenderer itemRenderer;

	public PrinterBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		itemRenderer = ctx.getItemRenderer();
	}

	public void renderProgressBar(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int lightAbove, float progress) {
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(PROGRESS_BAR));
		matrices.push();
		matrices.translate(0.4875, -1.25, 0.0);
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
		Matrix4f matrix = matrices.peek().getPosition();
		float progressX = 0.8125f - 0.6520f * progress;
		buffer.vertex(matrix, progressX, 0.875f, 0).color(0xFF_FFFFFF).uv(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(lightAbove).normal(0, 0, 1).next();
		buffer.vertex(matrix, progressX, 0.9375f, 0).color(0xFF_FFFFFF).uv(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(lightAbove).normal(0, 0, 1).next();
		buffer.vertex(matrix, 0.8125f, 0.9375f, 0).color(0xFF_FFFFFF).uv(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(lightAbove).normal(0, 0, 1).next();
		buffer.vertex(matrix, 0.8125f, 0.875f, 0).color(0xFF_FFFFFF).uv(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(lightAbove).normal(0, 0, 1).next();
		matrices.pop();
	}

	public void renderScreenStack(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int lightAbove, int overlay, int seed) {
		matrices.push();
		matrices.translate(0.0, -0.06, 0.0);
		matrices.scale(0.4f, 0.4f, 1);
		matrices.multiplyMatrix(Matrix4f.scale(1, 1, 0.01f));
		itemRenderer.renderItem(stack, ModelTransformation.Mode.GUI, lightAbove, overlay, matrices, vertexConsumers, seed);
		matrices.pop();
	}

	@Override
	public void render(PrinterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (entity.isEmpty()) {
			return;
		}
		matrices.push();
		int lightAbove = entity.getWorld() != null ? WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()) : light;
		BlockState state = entity.getCachedState();
		Direction facing = state.get(PrinterBlock.FACING);
		Vec3f vec = facing.getUnitVector();
		matrices.translate(vec.getX() / 2.0f + 0.5f, vec.getY() / 2.0f + 0.5f, vec.getZ() / 2.0f + 0.5f);
		matrices.multiply(facing.getRotationQuaternion());
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90));
		matrices.translate(0, 0, 0.005);
		if (state.get(PrinterBlock.ON)) {
			if (entity.xp > 0) {
				renderProgressBar(matrices, vertexConsumers, lightAbove, (float) entity.xp / entity.requiredXP);
			}
			if (!entity.getStack(0).isEmpty()) {
				renderScreenStack(entity.getStack(0), matrices, vertexConsumers, lightAbove, overlay, (int) entity.getPos().asLong());
			}
		}
		matrices.pop();
	}

	@Override
	public boolean rendersOutsideBoundingBox(PrinterBlockEntity blockEntity) {
		return true;
	}

	public static void register() {
		BlockEntityRendererRegistry.register(PrinterBlockEntity.BLOCK_ENTITY_TYPE, PrinterBlockEntityRenderer::new);
	}
}
