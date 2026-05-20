package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class IsLevelCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("islevel")) return;

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

        int  level  = IslandStorage.getLevel(userId);
        long xp     = IslandStorage.getXp(userId);
        long needed = IslandStorage.xpRequired(level);
        long coins  = IslandStorage.getCoins(userId);

        String progressBar = buildProgressBar(xp, needed);
        String tier        = getTier(level);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⭐ Saare tase — " + event.getUser().getName())
                .setColor(levelColor(level))
                .addField("🏅 Tase", level + " / 9999", true)
                .addField("🌟 Järk", tier, true)
                .addField("💰 Saare mündid", String.format("%,d", coins) + " münti", true)
                .addField("✨ XP areng", progressBar + "\n" + String.format("%,d", xp) + " / " + String.format("%,d", needed) + " XP", false)
                .setFooter("Teeni XP-d kogudes, AFK-ga ja mündiviskega!");

        event.replyEmbeds(embed.build()).queue();
    }

    private String buildProgressBar(long current, long max) {
        int filled = (int) Math.round((double) current / max * 20);
        filled = Math.max(0, Math.min(20, filled));
        return "█".repeat(filled) + "░".repeat(20 - filled);
    }

    private String getTier(int level) {
        if (level < 10)   return "🪨 Kivi";
        if (level < 50)   return "🪵 Puit";
        if (level < 100)  return "⚙️ Raud";
        if (level < 250)  return "💎 Teemant";
        if (level < 500)  return "🔮 Ametüst";
        if (level < 1000) return "🌟 Müütiline";
        if (level < 2500) return "🔥 Legendaarne";
        if (level < 5000) return "⚡ Iidne";
        if (level < 9000) return "🌌 Kosmiline";
        return "👑 Ülimuslik";
    }

    private Color levelColor(int level) {
        if (level < 50)   return Color.decode("#95A5A6");
        if (level < 100)  return Color.decode("#2ECC71");
        if (level < 500)  return Color.decode("#3498DB");
        if (level < 1000) return Color.decode("#9B59B6");
        if (level < 5000) return Color.decode("#E67E22");
        return Color.decode("#E74C3C");
    }
}