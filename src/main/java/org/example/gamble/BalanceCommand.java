package org.example.gamble;

import org.example.Database;
import org.example.util.ChannelGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;

public class BalanceCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("balance")) return;
        if (!ChannelGuard.requireBotChannel(event)) return;

        String userId  = event.getUser().getId();
        long   balance = Database.getBalance(userId);

        event.replyEmbeds(buildEmbed(event.getUser().getName(), balance)).queue();
    }

    private MessageEmbed buildEmbed(String username, long balance) {
        return new EmbedBuilder()
                .setTitle("💳 Rahakott — " + username)
                .setColor(Color.CYAN)
                .setDescription("Siin on sinu praegune summa!")
                .addField("Summa", "💰 " + balance + " €", false)
                .setFooter("Kasuta /daily et saada tasuta €!")
                .build();
    }
}