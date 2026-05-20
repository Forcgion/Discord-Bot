package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class IsDailyCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("isdaily")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasIsland(userId)) {
            event.reply("❌ Sul pole saart! Loo üks `/buyisland` käsuga.")
                    .setEphemeral(true).queue();
            return;
        }

        long dailyBalance = IslandStorage.getDailyCoins(userId);
        boolean canClaim  = IslandStorage.canClaimIslandDaily(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏝️ Saare Päevane Saldo — " + event.getUser().getName())
                .setColor(Color.CYAN)
                .addField("💰 Saare Päevane Summa", dailyBalance + " münte", false);

        if (canClaim) {
            embed.addField("✅ Staatus", "Sa võid nüüd oma saare päevase võtta! Kasuta `/islanddaily`", false);
        } else {
            long timeLeft = IslandStorage.getLastDailyClaim(userId) + 86400000L - System.currentTimeMillis();
            long hours   = (timeLeft / 3600000) % 24;
            long minutes = (timeLeft / 60000) % 60;
            long seconds = (timeLeft / 1000) % 60;
            embed.addField("⏰ Järgmine auhind", hours + "h " + minutes + "m " + seconds + "s", false);
        }

        event.replyEmbeds(embed.build()).queue();
    }
}