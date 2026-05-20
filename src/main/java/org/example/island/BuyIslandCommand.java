package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class BuyIslandCommand extends ListenerAdapter {

    private static final long ISLAND_PRICE = 5000;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("buyisland")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (IslandStorage.hasIsland(userId)) {
            event.reply("🏝️ Sul on juba saar! Kasuta **/island** et seda vaadata.")
                    .setEphemeral(true).queue();
            return;
        }

        if (!IslandStorage.hasStorageEntry(userId)) {
            IslandStorage.createIsland(userId);
        }

        long coins = IslandStorage.getCoins(userId);

        if (coins < ISLAND_PRICE) {
            event.reply("❌ Sul pole piisavalt münte! Saare hind on **" + ISLAND_PRICE
                            + " münti** ja sul on **" + coins + " münti**.\n"
                            + "Teeni münte käskudega **/work** ja **/newdaily**!")
                    .setEphemeral(true).queue();
            return;
        }

        IslandStorage.removeCoins(userId, ISLAND_PRICE);
        IslandStorage.markIslandBought(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏝️ Tere tulemast oma saarele!")
                .setColor(Color.decode("#2ECC71"))
                .setDescription("Sa ostsid endale saare **" + ISLAND_PRICE + " müntide** eest!")
                .addField("🚀 Alustamiseks", "Osta farmibott käsuga **/buybot**", false)
                .addField("💰 Kogu münte", "Kasuta **/collect** pärast bottide ostmist", false)
                .addField("😴 AFK boonus", "Kasuta **/isafk** kord iga 10 päeva tagant", false)
                .addField("💳 Järelejäänud mündid", String.format("%,d", IslandStorage.getCoins(userId)) + " münti", true)
                .setFooter("Palju edu oma saarel! 🌴");

        event.replyEmbeds(embed.build()).queue();
    }
}