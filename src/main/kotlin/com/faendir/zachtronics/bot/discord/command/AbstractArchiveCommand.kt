package com.faendir.zachtronics.bot.discord.command

import com.faendir.zachtronics.bot.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.EmbedFieldData
import discord4j.discordjson.json.EmbedFooterData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractArchiveCommand<S : Solution> : AbstractCommand(), SecuredCommand {
    protected abstract val archive: Archive<S>

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val solutions = parseSolutions(event)
        val embed = archiveAll(solutions)
        return MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(embed).build())
    }

    fun archiveAll(solutions: Collection<S>): EmbedData {
        val results = archive.archiveAll(solutions)

        val successes = results.count { it.first.isNotEmpty() }
        val title = if (successes != 0) "Success: $successes solution(s) archived" else "Failure: no solutions archived"

        // Discord cries if an embed is bigger than 6k or we have more tha 25 embed fields:
        // https://discord.com/developers/docs/resources/channel#embed-limits
        var totalSize = title.length
        var totalFields = 0

        val embed = EmbedData.builder().title(title)
        for ((solution, result) in solutions.zip(results)) {
            val name: String
            val value: String
            if (result.first.isNotEmpty()) {
                name = "*${solution.puzzle.displayName}* ${result.first}"
                value = "`${solution.score.toDisplayString()}` has been archived.\n" + result.second
            } else {
                name = "*${solution.puzzle.displayName}*"
                value = "`${solution.score.toDisplayString()}` did not qualify for archiving."
            }

            totalFields++
            totalSize += name.length + value.length
            if (totalFields > 25 || totalSize > 5900) {
                embed.footer(EmbedFooterData.builder().text("${results.size - totalFields - 1} more results hidden").build())
                break
            }

            embed.addField(
                EmbedFieldData.builder()
                    .name(name)
                    .value(value)
                    .inline(true)
                    .build()
            )
        }
        return embed.build()
    }

    abstract fun parseSolutions(interaction: SlashCommandEvent): List<S>
}