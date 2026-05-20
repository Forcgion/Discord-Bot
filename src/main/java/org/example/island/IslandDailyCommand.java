package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;
import java.util.Random;

public class IslandDailyCommand extends ListenerAdapter {

    private static final Random RANDOM = new Random();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("islanddaily")) return;

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

        if (!IslandStorage.canClaimIslandDaily(userId)) {
            long timeLeft = IslandStorage.getLastDailyClaim(userId) + 86400000L - System.currentTimeMillis();
            long hours   = (timeLeft / 3600000) % 24;
            long minutes = (timeLeft / 60000) % 60;
            long seconds = (timeLeft / 1000) % 60;
            event.replyEmbeds(buildCooldownEmbed(event.getUser().getName(), hours, minutes, seconds))
                    .setEphemeral(true).queue();
            return;
        }

        long reward = 1 + RANDOM.nextInt(500);
        IslandStorage.addDailyCoins(userId, reward);
        IslandStorage.setLastDailyClaim(userId);

        long newBalance = IslandStorage.getDailyCoins(userId);

        event.replyEmbeds(buildSuccessEmbed(event.getUser().getName(), reward, newBalance)).queue();
    }

    private MessageEmbed buildSuccessEmbed(String username, long reward, long newBalance) {
        return new EmbedBuilder()
                .setTitle("🏝️ Päevane Auhind — " + username)
                .setColor(Color.GREEN)
                .setDescription("Sa võtsid saare päevase auhinna ära!")
                .addField("Auhind", "💰 +" + reward + " münti", true)
                .addField("Saare Päevane Summa", "🏝️ " + newBalance + " münti", true)
                .setFooter("Tule tagasi 24 tunni pärast!")
                .build();
    }

    private MessageEmbed buildCooldownEmbed(String username, long hours, long minutes, long seconds) {
        return new EmbedBuilder()
                .setTitle("⏳ Päevane Auhind — " + username)
                .setColor(Color.RED)
                .setDescription("Sa oled juba võtnud oma päevase auhinna!")
                .addField("Aega jäänud", hours + "t " + minutes + "m " + seconds + "s", false)
                .setFooter("Tule tagasi varsti!")
                .build();
    }
}