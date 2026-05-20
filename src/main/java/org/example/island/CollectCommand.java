package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.google.gson.JsonArray;
import java.awt.Color;

public class CollectCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("collect")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasIsland(userId)) {
            event.reply("🏝️ Sul pole veel saart! Kasuta **/buyisland** saare ostmiseks.")
                    .setEphemeral(true).queue();
            return;
        }

        JsonArray bots = IslandStorage.getBots(userId);

        if (bots.isEmpty()) {
            event.reply("🤖 Sul pole ühtegi farmibotti! Osta üks käsuga **/buybot**.")
                    .setEphemeral(true).queue();
            return;
        }

        long totalCoins = 0;
        long totalXp    = 0;
        StringBuilder breakdown = new StringBuilder();

        for (var el : bots) {
            String type = el.getAsString();
            long[] stats = BuyBotCommand.BOTS.get(type);
            if (stats == null) continue;

            long coins = stats[1];
            long xp    = stats[2];
            totalCoins += coins;
            totalXp    += xp;

            breakdown.append(IslandCommand.botEmoji(type))
                    .append(" ").append(IslandCommand.capitalize(type))
                    .append(" → +").append(coins).append(" münti\n");
        }

        IslandStorage.addCoins(userId, totalCoins);
        IslandStorage.addXp(userId, totalXp);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌾 Saak kogutud!")
                .setColor(Color.decode("#F1C40F"))
                .setDescription(breakdown.toString())
                .addField("💰 Kokku teenisid", "+" + totalCoins + " münti", true)
                .addField("✨ XP teenisid", "+" + totalXp + " XP", true)
                .addField("💳 Uus summa", IslandStorage.getCoins(userId) + " münti", true)
                .setFooter("Tase " + IslandStorage.getLevel(userId) + " saar");

        event.replyEmbeds(embed.build()).queue();
    }
}