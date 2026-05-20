package org.example.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.awt.Color;

public class WelcomeListener extends ListenerAdapter {
    private static final String WELCOME_CHANNEL_ID = "";
    private static final String LOGO_URL = "";

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        var channel = event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID);
        if (channel == null) return;

        String userMention = event.getUser().getAsMention();
        String avatarUrl = event.getUser().getEffectiveAvatarUrl();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("\uD83D\uDC4B Welcome to  the server!")
                .setDescription("Welcome {user}, " + userMention + "! \uD83C\uDF89")
                .setColor(Color.BLACK)
                .setThumbnail("")
                .setImage("")
                .addField("\uD83D\uDCDC ", "<>", true)
                .addField("\uD83D\uDCE2 ", "<>", true)
                .setFooter("", avatarUrl)
                .build();

        channel.sendMessageEmbeds(embed)
                .setContent(userMention)
                .queue();
    }
}