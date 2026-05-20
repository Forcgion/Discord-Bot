package org.example.tickets;

import org.example.Main;
import org.example.storage.DefaultStorage;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.function.Consumer;

public class TranscriptService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")
                    .withZone(ZoneId.of("UTC"));

    public static void saveAndPost(TextChannel channel, String closedBy, Consumer<Integer> onDone) {
        channel.getHistory().retrievePast(100).queue(messages -> {
            Collections.reverse(messages);

            long count = messages.stream().filter(m -> !m.getAuthor().isBot()).count();

            StringBuilder html = new StringBuilder();

            html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Ticket Transcript — %s</title>
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { background: #313338; color: #dbdee1; font-family: 'gg sans', 'Noto Sans', sans-serif; font-size: 15px; line-height: 1.5; }
                    .header { background: #1e1f22; padding: 20px 30px; border-bottom: 2px solid #5865f2; display: flex; align-items: center; gap: 16px; }
                    .header-icon { width: 48px; height: 48px; background: #5865f2; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-size: 22px; }
                    .header-info h1 { font-size: 20px; font-weight: 700; color: #ffffff; }
                    .header-info p { font-size: 13px; color: #949ba4; }
                    .meta-bar { background: #2b2d31; padding: 12px 30px; display: flex; gap: 30px; border-bottom: 1px solid #1e1f22; font-size: 13px; color: #949ba4; }
                    .meta-bar span b { color: #dbdee1; }
                    .messages { padding: 20px 30px; display: flex; flex-direction: column; gap: 2px; }
                    .message { display: flex; gap: 14px; padding: 6px 8px; border-radius: 4px; transition: background 0.1s; }
                    .message:hover { background: #2e3035; }
                    .avatar { width: 40px; height: 40px; border-radius: 50%%; background: #5865f2; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; color: #fff; flex-shrink: 0; margin-top: 2px; }
                    .msg-content { flex: 1; }
                    .msg-header { display: flex; align-items: baseline; gap: 8px; margin-bottom: 2px; }
                    .username { font-weight: 600; font-size: 15px; color: #ffffff; }
                    .timestamp { font-size: 11px; color: #4e5058; }
                    .msg-text { color: #dbdee1; word-break: break-word; white-space: pre-wrap; }
                    .divider { border: none; border-top: 1px solid #3f4147; margin: 16px 0; }
                    .footer { background: #1e1f22; padding: 14px 30px; text-align: center; font-size: 12px; color: #4e5058; border-top: 1px solid #3f4147; }
                  </style>
                </head>
                <body>
                """.formatted(channel.getName()));

            html.append("""
                <div class="header">
                  <div class="header-icon">🎫</div>
                  <div class="header-info">
                    <h1>#%s</h1>
                    <p>Ticket Transcript</p>
                  </div>
                </div>
                """.formatted(channel.getName()));

            html.append("""
                <div class="meta-bar">
                  <span>📅 Closed: <b>%s</b></span>
                  <span>🔒 Closed by: <b>%s</b></span>
                  <span>💬 Messages: <b>%d</b></span>
                </div>
                """.formatted(
                    FORMATTER.format(Instant.now()),
                    escapeHtml(closedBy),
                    count
            ));

            html.append("<div class=\"messages\">\n");

            String lastAuthorId = "";
            for (Message msg : messages) {
                if (msg.getAuthor().isBot()) continue;

                String authorId    = msg.getAuthor().getId();
                String authorName  = escapeHtml(msg.getAuthor().getAsTag());
                String initial     = authorName.substring(0, 1).toUpperCase();
                String time        = FORMATTER.format(msg.getTimeCreated().toInstant());
                String content     = escapeHtml(msg.getContentDisplay());
                String avatarColor = avatarColor(authorId);

                if (!authorId.equals(lastAuthorId)) {
                    if (!lastAuthorId.isEmpty()) {
                        html.append("<hr class=\"divider\"/>\n");
                    }
                    html.append("""
                        <div class="message">
                          <div class="avatar" style="background:%s;">%s</div>
                          <div class="msg-content">
                            <div class="msg-header">
                              <span class="username">%s</span>
                              <span class="timestamp">%s</span>
                            </div>
                            <div class="msg-text">%s</div>
                          </div>
                        </div>
                        """.formatted(avatarColor, initial, authorName, time, content));
                } else {
                    html.append("""
                        <div class="message" style="padding-left:62px;">
                          <div class="msg-content">
                            <div class="msg-text">%s</div>
                          </div>
                        </div>
                        """.formatted(content));
                }

                lastAuthorId = authorId;
            }

            html.append("</div>\n");

            html.append("""
                <div class="footer">Generated by your Discord bot • %s UTC</div>
                </body>
                </html>
                """.formatted(FORMATTER.format(Instant.now())));

            File dir  = new File("transcripts");
            dir.mkdirs();
            File file = new File(dir, channel.getName() + "-" + System.currentTimeMillis() + ".html");

            try (FileWriter fw = new FileWriter(file)) {
                fw.write(html.toString());
            } catch (IOException e) {
                e.printStackTrace();
                onDone.accept((int) count);
                return;
            }

            TextChannel logChannel = Main.getJda().getTextChannelById(DefaultStorage.LOG_CHANNEL_ID);
            if (logChannel != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📋 Pilet suletud — #" + channel.getName())
                        .addField("Suletud kelle poolt", closedBy, true)
                        .addField("Vestlus", String.valueOf(count), true)
                        .addField("Millal Kinni panndud", FORMATTER.format(Instant.now()) + " UTC", true)
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now());

                logChannel.sendMessageEmbeds(embed.build())
                        .addFiles(FileUpload.fromData(file, file.getName()))
                        .queue(m -> onDone.accept((int) count));
            } else {
                onDone.accept((int) count);
            }
        });
    }

    private static String avatarColor(String userId) {
        String[] colors = {"#5865f2", "#57f287", "#fee75c", "#eb459e", "#ed4245", "#3ba55c"};
        int index = (int) (Math.abs(userId.hashCode()) % colors.length);
        return colors[index];
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}