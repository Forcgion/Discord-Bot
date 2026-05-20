package org.example.listener;

import org.example.ChannelConfig;
import org.example.util.ChannelGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.List;

public class HelpCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("help")) return;
        if (!ChannelGuard.requireBotChannel(event)) return;

        // ── Embed 1: Gambling & Moderation ───────────────────────────────────
        MessageEmbed gambling = new EmbedBuilder()
                .setTitle("📖 Estiva Bot — Käskude nimekiri")
                .setColor(Color.decode("#5865F2"))
                .setDescription("Siin on kõik saadaolevad käsud!\n\u200B")

                .addField("💰 Hasartmängud — <#" + ChannelConfig.BOT_CHANNEL_ID + ">",
                        """
                        `/balance` — Vaata oma €-saldot
                        `/daily` — Päevane €-auhind (1–70 €, 24h ooteaeg)
                        `/coinflip <panus> <pea/kiri>` — Viska münti ja panusta
                        `/blackjack <panus>` — Mängi blackjacki diileriga
                        """, false)

                .addField("🛡️ Moderatsioon",
                        """
                        `/purge <kogus>` — Kustuta kuni 100 sõnumit *(ainult Meeskond)*
                        """, false)

                .setFooter("Leht 1/2")
                .build();

        // ── Embed 2: Island ───────────────────────────────────────────────────
        MessageEmbed island = new EmbedBuilder()
                .setTitle("🏝️ Saare käsud — <#" + ChannelConfig.ISLAND_CHANNEL_ID + ">")
                .setColor(Color.decode("#2ECC71"))
                .setDescription("Kõik saare käsud töötavad ainult saare kanalil!\n\u200B")

                .addField("🚀 Alustamine",
                        """
                        `/work` — Teeni 150 münti (kord tunnis, ilma saareta)
                        `/newdaily` — Päevane 1–500 münti (ilma saareta, 24h ooteaeg)
                        `/buyisland` — Osta oma saar (hind: 5 000 münti)
                        """, false)

                .addField("📊 Info",
                        """
                        `/island` — Vaata oma saare ülevaadet
                        `/isbalance` — Vaata saare müntide saldot
                        `/islevel` — Vaata saare taset, XP-d ja järku
                        `/isdaily` — Vaata kui palju päevast saldot on saadaval
                        """, false)

                .addField("💸 Teenimine",
                        """
                        `/islanddaily` — Päevane 1–500 saare münti (24h ooteaeg)
                        `/collect` — Kogu münte oma farmibottidelt
                        `/isafk` — AFK preemia: +20 000 münti & +500 XP (1 AFK päev)
                        `/iscoinflip <summa> <pea/kiri>` — Panusta saare münte mündiviskele
                        """, false)

                .addField("🛒 Pood",
                        """
                        `/buybot <tüüp>` — Osta farmibott saarele
                        ‣ 🥔 Kartul — 100 münti | +20/collect
                        ‣ 🥕 Porgand — 250 münti | +45/collect
                        ‣ 🫛 Hernes — 500 münti | +90/collect
                        ‣ 🌾 Vilj — 1 000 münti | +180/collect
                        ‣ 🎋 Suhkruroog — 2 500 münti | +400/collect
                        ‣ 🪵 Bambus — 5 000 münti | +900/collect
                        `/buyafk` — Osta 10 AFK päeva (hind: 250 000 münti)
                        """, false)

                .setFooter("Leht 2/2 — Alusta /work & /newdaily käskudega!")
                .build();

        event.replyEmbeds(List.of(gambling, island)).queue();
    }
}