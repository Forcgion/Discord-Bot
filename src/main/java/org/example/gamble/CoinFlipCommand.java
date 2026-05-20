package org.example.gamble;

import org.example.Database;
import org.example.util.ChannelGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Random;

public class CoinFlipCommand extends ListenerAdapter {

    private static final Random RANDOM = new Random();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("coinflip")) return;
        if (!ChannelGuard.requireBotChannel(event)) return;

        String userId = event.getUser().getId();
        long   bet    = event.getOption("panus").getAsLong();
        String pick   = event.getOption("valik").getAsString().toLowerCase();

        if (bet <= 0) {
            event.reply("❌ Panus peab olema kõrgem kui 0.").setEphemeral(true).queue();
            return;
        }

        long balance = Database.getBalance(userId);
        if (bet > balance) {
            event.reply("❌ Sul pole piisavalt raha! Sinu saldo: **" + balance + "** €.").setEphemeral(true).queue();
            return;
        }

        boolean isHeads = RANDOM.nextBoolean();
        String  result  = isHeads ? "pea" : "kiri";
        String  emoji   = isHeads ? "🪙" : "🎴";
        boolean won     = (isHeads && pick.equals("pea")) || (!isHeads && pick.equals("kiri"));

        if (won) {
            Database.addBalance(userId, bet);
            event.replyEmbeds(buildEmbed(emoji, result, bet, true, event.getUser().getEffectiveAvatarUrl())).queue();
        } else {
            Database.removeBalance(userId, bet);
            event.replyEmbeds(buildEmbed(emoji, result, bet, false, event.getUser().getEffectiveAvatarUrl())).queue();
        }
    }

    private MessageEmbed buildEmbed(String emoji, String result, long bet, boolean won, String avatarUrl) {
        return new EmbedBuilder()
                .setTitle(emoji + " Mündi Vise")
                .setDescription("Tulemus: **" + result.substring(0, 1).toUpperCase() + result.substring(1) + "**")
                .addField(won ? "🎉 Võitsid!" : "😔 Kaotasid!", (won ? "+" : "-") + bet + " €", false)
                .setColor(won ? Color.GREEN : Color.RED)
                .setFooter("Estiva Bot", avatarUrl)
                .build();
    }
}