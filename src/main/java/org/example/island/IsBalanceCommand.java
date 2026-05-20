package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class IsBalanceCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("isbalance")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasStorageEntry(userId)) {
            event.reply("❌ Sul pole veel ühtegi saare münti! Teeni münte käskudega **/work** ja **/newdaily**.")
                    .setEphemeral(true).queue();
            return;
        }

        long coins = IslandStorage.getCoins(userId);
        int  level = IslandStorage.getLevel(userId);
        long xp    = IslandStorage.getXp(userId);
        boolean hasIsland = IslandStorage.hasIsland(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💰 " + event.getUser().getName() + " saare saldo")
                .setColor(Color.decode("#F1C40F"))
                .addField("💰 Saare mündid", String.format("%,d", coins) + " münti", true)
                .addField("🏝️ Saar", hasIsland ? "✅ Ostetud" : "❌ Ostmata", true);

        if (hasIsland) {
            embed.addField("⭐ Tase", String.valueOf(level), true)
                    .addField("✨ XP", String.format("%,d", xp), true)
                    .setFooter("Teeni rohkem /collect, /isafk ja /iscoinflip käskudega!");
        } else {
            embed.setFooter("Osta saar /buyisland käsuga! Hind: 5000 münti.");
        }

        event.replyEmbeds(embed.build()).queue();
    }
}