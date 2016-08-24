package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCityForge7;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

public class Forge7CommandSender<S extends ICommandSender> extends ForgeCommandSender<S, MineCityForge7>
{
    public Forge7CommandSender(MineCityForge7 mod, S sender)
    {
        super(mod, sender);
    }

    @Override
    public void send(Message[] message)
    {
        /*  /tellraw ["a/nb"] does not work in 1.7.10
        ChatComponentText merge = new ChatComponentText("");
        for(int i = 0;; i++)
        {
            merge.appendSibling(ForgeUtil.chatComponentFromLegacyText(mod.mineCity.messageTransformer.toLegacy(message[i])));
            if(i+1 < message.length)
                merge.appendText("\n");
            else
                break;
        }

        sender.addChatMessage(merge);
        */

        for(Message msg : message)
            send(msg);
    }

    @Override
    public void send(Message message)
    {
        for(IChatComponent msg: ((Forge7Transformer) mod.transformer).toMultilineForge(message))
            sender.addChatMessage(msg);
    }
}
