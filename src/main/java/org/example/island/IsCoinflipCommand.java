package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import java.awt.Color;
import java.util.Random;

public class IsCoinflipCommand extends ListenerAdapter {

    private static final Random RANDOM = new Random();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("iscoinflip")) return;

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

        OptionMapping betOption   = event.getOption("amount");
        OptionMapping valikOption = event.getOption("valik");

        if (betOption == null || valikOption == null) {
            event.reply("❌ Palun sisesta panus ja valik. Näide: `/iscoinflip kogus:500 valik:pea`")
                    .setEphemeral(true).queue();
            return;
        }

        long bet = betOption.getAsLong();

        if (bet <= 0) {
            event.reply("❌ Panus peab olema suurem kui 0.").setEphemeral(true).queue();
            return;
        }

        long balance = IslandStorage.getCoins(userId);

        if (bet > balance) {
            event.reply("❌ Sul pole piisavalt saare münte! Sinu saldo: **" + String.format("%,d", balance) + " münti**")
                    .setEphemeral(true).queue();
            return;
        }

        String valik  = valikOption.getAsString();
        String result = RANDOM.nextBoolean() ? "pea" : "kiri";

        String valikEmoji  = valik.equals("pea")  ? "🪙 Pea" : "🎴 Kiri";
        String resultEmoji = result.equals("pea") ? "🪙 Pea" : "🎴 Kiri";

        boolean win = valik.equals(result);

        if (win) {
            IslandStorage.addCoins(userId, bet);
            IslandStorage.addXp(userId, 30);

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("🪙 Mündiviise — Sa võitsid!")
                    .setColor(Color.decode("#2ECC71"))
                    .setDescription("Münt kukkus sinu kasuks!")
                    .addField("🎯 Sinu valik", valikEmoji, true)
                    .addField("🪙 Tulemus", resultEmoji, true)
                    .addField("💰 Võit", "+" + String.format("%,d", bet) + " münti", true)
                    .addField("💳 Uus summa", String.format("%,d", IslandStorage.getCoins(userId)) + " münti", true)
                    .setFooter("50/50 võimalus igal korral!")
                    .build()).queue();
        } else {
            IslandStorage.removeCoins(userId, bet);

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("🪙 Mündiviise — Sa kaotasid!")
                    .setColor(Color.decode("#E74C3C"))
                    .setDescription("Rohkem õnne järgmine kord!")
                    .addField("🎯 Sinu valik", valikEmoji, true)
                    .addField("🪙 Tulemus", resultEmoji, true)
                    .addField("💸 Kaotus", "-" + String.format("%,d", bet) + " münti", true)
                    .addField("💳 Uus summa", String.format("%,d", IslandStorage.getCoins(userId)) + " münti", true)
                    .setFooter("50/50 võimalus igal korral!")
                    .build()).queue();
        }
    }
}