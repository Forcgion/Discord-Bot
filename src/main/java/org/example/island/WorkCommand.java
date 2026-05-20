package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkCommand extends ListenerAdapter {

    private static final long REWARD      = 150;
    private static final long COOLDOWN_MS = 60 * 60 * 1000;
    private static final Map<String, Long> lastWork = new HashMap<>();

    private static final String[] JOBS = {
            "Sa töötasid kohvikus ja teenisid",
            "Sa parandisid arvuteid ja teenisid",
            "Sa vedasid kaupu ja teenisid",
            "Sa tegid ülekandeid ja teenisid",
            "Sa ehitasid maja ja teenisid",
            "Sa müüsid kala turul ja teenisid",
            "Sa olid valvur ja teenisid",
            "Sa kirjutasid koodi ja teenisid",
            "Sa töötasid poes ja teenisid",
            "Sa sõid lõunat tööl ja teenisid"
    };

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("work")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (IslandStorage.hasIsland(userId)) {
            event.reply("❌ Sul on juba saar! Kasuta **/collect**, **/isafk** ja **/islanddaily** müntide teenimiseks.")
                    .setEphemeral(true).queue();
            return;
        }

        long now = System.currentTimeMillis();

        if (lastWork.containsKey(userId)) {
            long diff = now - lastWork.get(userId);
            if (diff < COOLDOWN_MS) {
                long remaining = COOLDOWN_MS - diff;
                event.reply("⏳ Sa oled väsinud! Tule tagasi **" + formatTime(remaining) + "** pärast.")
                        .setEphemeral(true).queue();
                return;
            }
        }

        if (!IslandStorage.hasStorageEntry(userId)) {
            IslandStorage.createIsland(userId);
        }

        lastWork.put(userId, now);
        IslandStorage.addCoins(userId, REWARD);

        String job = JOBS[(int) (Math.random() * JOBS.length)];
        long islandCoins = IslandStorage.getCoins(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💼 Töö tehtud!")
                .setColor(Color.decode("#3498DB"))
                .setDescription(job + " **" + REWARD + " münti**!")
                .addField("🔨 Saare mündid", String.format("%,d", islandCoins) + " münti", true)
                .addField("⏳ Järgmine töö", "1 tunni pärast", true)
                .setFooter("Kasuta /work iga tund! • Osta saar /buyisland käsuga!");

        event.replyEmbeds(embed.build()).queue();
    }

    private String formatTime(long ms) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}