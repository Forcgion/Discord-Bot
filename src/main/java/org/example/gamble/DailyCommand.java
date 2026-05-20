package org.example.gamble;

import org.example.Database;
import org.example.util.ChannelGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.Random;

public class DailyCommand extends ListenerAdapter {

    private static final long   COOLDOWN_MS = 86_400_000L;
    private static final Random RANDOM      = new Random();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("daily")) return;
        if (!ChannelGuard.requireBotChannel(event)) return;

        String userId    = event.getUser().getId();
        long   now       = System.currentTimeMillis();
        long   lastDaily = Database.getLastDaily(userId);
        long   diff      = now - lastDaily;

        if (diff < COOLDOWN_MS) {
            long remaining = COOLDOWN_MS - diff;
            long hours     = remaining / 3_600_000;
            long minutes   = (remaining % 3_600_000) / 60_000;
            long seconds   = (remaining % 60_000) / 1_000;

            event.replyEmbeds(buildCooldownEmbed(event.getUser().getName(), hours, minutes, seconds))
                    .setEphemeral(true).queue();
            return;
        }

        long reward = 1 + RANDOM.nextInt(70);
        Database.addBalance(userId, reward);
        Database.setLastDaily(userId, now);

        long newBalance = Database.getBalance(userId);

        event.replyEmbeds(buildSuccessEmbed(event.getUser().getName(), reward, newBalance)).queue();
    }

    private MessageEmbed buildSuccessEmbed(String username, long amount, long newBalance) {
        return new EmbedBuilder()
                .setTitle("🎁 Päevane auhind — " + username)
                .setColor(Color.GREEN)
                .setDescription("Sa võtsid päevase auhinna ära!")
                .addField("Auhind", "💰 +" + amount + " €", true)
                .addField("Summa", "💳 " + newBalance + " €", true)
                .setFooter("Tule tagasi 24 tunni pärast tagasi!")
                .build();
    }

    private MessageEmbed buildCooldownEmbed(String username, long hours, long minutes, long seconds) {
        return new EmbedBuilder()
                .setTitle("⏳ Päeva auhind — " + username)
                .setColor(Color.RED)
                .setDescription("Sa oled juba võtnud oma päevase ära!")
                .addField("Aega jäänud", hours + "h " + minutes + "m " + seconds + "s", false)
                .setFooter("Tule tagasi varsti!")
                .build();
    }
}