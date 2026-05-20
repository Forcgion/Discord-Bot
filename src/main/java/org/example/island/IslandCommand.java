package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.google.gson.JsonArray;
import java.awt.Color;

public class IslandCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("island")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasIsland(userId)) {
            event.reply("🏝️ Sul pole veel saart! Osta üks **/buyisland** käsuga.")
                    .setEphemeral(true).queue();
            return;
        }

        long coins  = IslandStorage.getCoins(userId);
        int  level  = IslandStorage.getLevel(userId);
        long xp     = IslandStorage.getXp(userId);
        long needed = IslandStorage.xpRequired(level);

        JsonArray bots = IslandStorage.getBots(userId);
        StringBuilder botList = new StringBuilder();
        if (bots.isEmpty()) {
            botList.append("Ühtegi botti pole — osta **/buybot** käsuga");
        } else {
            for (var el : bots) {
                String name = el.getAsString();
                botList.append(botEmoji(name)).append(" ").append(capitalize(name)).append("\n");
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏝️ " + event.getUser().getName() + " saar")
                .setColor(Color.decode("#2ECC71"))
                .addField("💰 Saare Mündid", String.valueOf(coins) + " münti", true)
                .addField("⭐ Tase", level + " / 9999", true)
                .addField("✨ KP", xp + " / " + needed, true)
                .addField("🤖 Talubotid", botList.toString(), false)
                .setFooter("Kasuta /collect müntide teenimiseks • /islevel taseme tõstmiseks");

        event.replyEmbeds(embed.build()).queue();
    }

    public static String botEmoji(String type) {
        return switch (type.toLowerCase()) {
            case "kartul"     -> "🥔";
            case "porgand"    -> "🥕";
            case "punapeet"   -> "🫛";
            case "nisu"       -> "🌾";
            case "suhkruroog" -> "🎋";
            case "bambus"     -> "🪵";
            default           -> "🤖";
        };
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}