package org.example.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.Main;

import java.util.Objects;

public class EventHandler extends ListenerAdapter {
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();
        if (!user.isBot() && !user.isSystem()) {
            event.getGuild().addRoleToMember(user, Objects.requireNonNull(Main.getJda().getRoleById(""))).queue();
            // When joining discord they get this role
        }

    }
}
