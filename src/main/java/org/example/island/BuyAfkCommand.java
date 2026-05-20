package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class BuyAfkCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("buyafk")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasIsland(userId)) {
            event.reply("🏝️ Sul pole veel saart! Kasuta **/island** alustamiseks.")
                    .setEphemeral(true).queue();
            return;
        }

        long balance = IslandStorage.getCoins(userId);

        if (balance < IsAfkCommand.AFK_BUY_COST) {
            event.reply("❌ Sul pole piisavalt saare münte! Vajad **250,000 münti**, sul on **"
                            + String.format("%,d", balance) + " münti**.")
                    .setEphemeral(true).queue();
            return;
        }

        IslandStorage.removeCoins(userId, IsAfkCommand.AFK_BUY_COST);
        IslandStorage.addAfkDays(userId, IsAfkCommand.AFK_DAYS);

        int totalDays = IslandStorage.getAfkDays(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("😴 AFK päevad ostetud!")
                .setColor(Color.decode("#9B59B6"))
                .setDescription("Sa ostsid **10 AFK päeva** hinnaga **250,000 münti**!")
                .addField("📅 AFK päevi kokku", totalDays + " päeva", true)
                .addField("💰 Preemia kasutuse kohta", String.format("%,d", IsAfkCommand.AFK_REWARD) + " münti", true)
                .addField("💳 Uus summa", String.format("%,d", IslandStorage.getCoins(userId)) + " münti", true)
                .setFooter("Kasuta /isafk iga päev preemia saamiseks!");

        event.replyEmbeds(embed.build()).queue();
    }
}