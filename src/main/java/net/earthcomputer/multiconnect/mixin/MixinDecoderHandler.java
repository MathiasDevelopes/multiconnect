package net.earthcomputer.multiconnect.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecoderHandler.class)
public class MixinDecoderHandler {

    @Shadow @Final private NetworkSide side;

    @Unique private ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();

    @Inject(method = "decode", at = @At("HEAD"))
    private void onDecodeHead(ChannelHandlerContext context, ByteBuf buf, List<Object> output, CallbackInfo ci) {
        this.context.set(context);
    }

    @ModifyVariable(method = "decode", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private PacketByteBuf transformPacketByteBuf(PacketByteBuf buf) {
        if (side == NetworkSide.CLIENTBOUND)
            buf = new TransformerByteBuf(buf, context.get());
        context.set(null);
        return buf;
    }

}
