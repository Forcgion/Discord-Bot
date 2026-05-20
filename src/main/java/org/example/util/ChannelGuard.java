package org.example.util;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ChannelGuard {

    /**
     * Returns true if the command was used in the bot channel.
     * If not, replies with an ephemeral error and returns false.
     */
    public static boolean requireBotChannel(SlashCommandInteractionEvent event) {
        if (!event.getChannel().getId().equals(ChannelConfig.BOT_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.BOT_CHANNEL_ID + ">.")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }
}
