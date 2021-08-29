package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Record
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.MultipartRequest

abstract class AbstractShowCommand<C : Category, P : Puzzle, R : Record> : AbstractCommand() {
    abstract val leaderboards: List<Leaderboard<C, P, R>>
    override val isReadOnly: Boolean = true

    override fun handle(event: SlashCommandEvent): MultipartRequest<WebhookExecuteRequest> {
        val (puzzle, category) = findPuzzleAndCategory(event)
        val record = leaderboards.asSequence().map { it.get(puzzle, category) }.firstOrNull()
            ?: throw IllegalArgumentException("sorry, there is no score for ${puzzle.displayName} ${category.displayName}.")
        return MultipartRequest.ofRequestAndFiles(
            WebhookExecuteRequest.builder().content("*${puzzle.displayName}* **${category.displayName}**\n${record.toDisplayString()}").build(),
            record.attachments()
        )
    }

    abstract fun findPuzzleAndCategory(interaction: SlashCommandEvent): Pair<P, C>
}