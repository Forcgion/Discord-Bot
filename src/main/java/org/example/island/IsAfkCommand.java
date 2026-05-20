package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class IsAfkCommand extends ListenerAdapter {

    public static final long AFK_REWARD   = 20_000;
    public static final long AFK_XP       = 500;
    public static final long AFK_BUY_COST = 250_000;
    public static final int  AFK_DAYS     = 10;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("isafk")) return;

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

        int daysLeft = IslandStorage.getAfkDays(userId);

        if (daysLeft <= 0) {
            event.reply("❌ Sul pole AFK päevi alles! Osta 10 uut päeva käsuga **/buyafk** — hind: **250,000 münti**.")
                    .setEphemeral(true).queue();
            return;
        }

        IslandStorage.useAfkDay(userId);
        IslandStorage.addCoins(userId, AFK_REWARD);
        IslandStorage.addXp(userId, AFK_XP);

        int remaining = daysLeft - 1;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("😴 AFK preemia kogutud!")
                .setColor(Color.decode("#9B59B6"))
                .setDescription("Sa läksid saarelt AFK-le ja tulid tagasi preemiaga!")
                .addField("💰 Teenitud mündid", "+" + String.format("%,d", AFK_REWARD) + " münti", true)
                .addField("✨ Teenitud XP", "+" + AFK_XP + " XP", true)
                .addField("💳 Uus saldo", String.format("%,d", IslandStorage.getCoins(userId)) + " münti", true)
                .addField("📅 AFK päevi järel", remaining + " / " + AFK_DAYS, false)
                .setFooter(remaining == 0
                        ? "AFK päevad otsas! Osta uued käsuga /buyafk"
                        : "Tase " + IslandStorage.getLevel(userId));

        event.replyEmbeds(embed.build()).queue();
    }
}