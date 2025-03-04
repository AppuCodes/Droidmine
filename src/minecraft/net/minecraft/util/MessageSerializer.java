package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageSerializer extends MessageToByteEncoder<Packet>
{
    private static final Logger logger = LogManager.getLogger();
    private final EnumPacketDirection direction;

    public MessageSerializer(EnumPacketDirection direction)
    {
        this.direction = direction;
    }

    protected void encode(ChannelHandlerContext p_encode_1_, Packet p_encode_2_, ByteBuf p_encode_3_) throws IOException, Exception
    {
        Integer integer = ((EnumConnectionState)p_encode_1_.channel().attr(NetworkManager.attrKeyConnectionState).get()).getPacketId(this.direction, p_encode_2_);

        if (integer == null)
        {
            throw new IOException("Can\'t serialize unregistered packet");
        }
        else
        {
            PacketBuffer packetbuffer = new PacketBuffer(p_encode_3_);
            packetbuffer.writeVarIntToBuffer(integer.intValue());

            try
            {
                p_encode_2_.writePacketData(packetbuffer);
            }
            catch (Throwable throwable)
            {
                logger.error((Object)throwable);
            }
        }
    }
}
