package com.faendir.zachtronics.bot.generic.discord

import com.faendir.zachtronics.bot.generic.archive.Archive
import com.faendir.zachtronics.bot.model.Solution
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.entity.User
import discord4j.discordjson.json.EmbedData
import discord4j.discordjson.json.ImmutableWebhookExecuteRequest
import discord4j.discordjson.json.WebhookExecuteRequest
import reactor.core.publisher.Mono

abstract class AbstractArchiveCommand<S : Solution> : Command {
    override val name: String = "archive"
    override val isReadOnly: Boolean = false
    protected abstract val archive: Archive<S>

    override fun handle(options: List<ApplicationCommandInteractionOption>, user: User): Mono<WebhookExecuteRequest> {
        return parseSolution(options, user)
            .flatMap { solution -> archive(solution) }
            .map { WebhookExecuteRequest.builder().addEmbed(it).build() }
    }

    fun archive(solution: S): Mono<EmbedData> = archive.archive(solution).map { result -> getResultMessage(result, solution) }

    private fun getResultMessage(result: List<String>, solution: S): EmbedData {
        return if (result.isNotEmpty()) {
            EmbedData.builder()
                .title("Success: *${solution.puzzle}* ${result.joinToString()}")
                .description("`${solution.score.toDisplayString()}` has been archived.")
                .build()
        } else {
            EmbedData.builder()
                .title("Failure: *${solution.puzzle}*")
                .description("`${solution.score.toDisplayString()}` did not qualify for archiving.")
                .build()
        }
    }

    abstract fun parseSolution(options: List<ApplicationCommandInteractionOption>, user: User): Mono<S>
}