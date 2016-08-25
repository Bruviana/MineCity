package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S23PacketBlockChange;

public interface SevenEntityPlayerMP extends IEntityPlayerMP
{
    @Override
    default void sendPacket(Packet packet)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.sendPacket(packet);
    }

    @Override
    default void kick(String reason)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.kickPlayerFromServer(reason);
    }

    @Override
    default void sendBlock(int x, int y, int z)
    {
        sendPacket(new S23PacketBlockChange(x, y, z, getWorld()));
    }

    @Override
    default void sendFakeBlock(int x, int y, int z, IState state)
    {
        sendFakeBlock(x, y, z, state.getIBlock().getId(), state.getStateId());
    }

    default void sendFakeBlock(int x, int y, int z, int block, int metadata)
    {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer(4 + 1 + 4 + 4 + 1));
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeVarIntToBuffer(block);
        buf.writeByte(metadata);

        S23PacketBlockChange empty = new S23PacketBlockChange();
        try
        {
            empty.readPacketData(buf);
            sendPacket(empty);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
