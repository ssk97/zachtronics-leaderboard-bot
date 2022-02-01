/*
 * Copyright (c) 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.discord.command;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Description;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUser;
import com.faendir.zachtronics.bot.discord.command.security.DiscordUserSecured;
import com.faendir.zachtronics.bot.discord.command.security.Secured;
import com.faendir.zachtronics.bot.git.GitRepository;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RestartCommand implements TopLevelCommand<RestartCommand.RestartData> {
    @Delegate
    private final RestartCommand_RestartDataParser parser = RestartCommand_RestartDataParser.INSTANCE;
    @Getter
    private final Secured secured = new DiscordUserSecured(DiscordUser.BOT_OWNERS);
    @Getter
    private final String commandName = "restart";

    private final ApplicationContext applicationContext;
    private final List<GitRepository> repositories;

    @SneakyThrows
    @NotNull
    @Override
    public Mono<Void> handle(@NotNull DeferrableInteractionEvent event, @NotNull RestartData parameters) {
        if (!parameters.sudo) {
            // acquire and hold all the repo write locks, which ensures no write operations are concurrently running
            repositories.parallelStream().forEach(GitRepository::acquireWriteAccess);
        }

        return event.editReply("shutting down, see you soon!").then(Mono.fromCallable(() -> {
            log.error("Requested shut down, see you soon");
            SpringApplication.exit(applicationContext);
            Thread.sleep(5000);
            System.exit(0);
            return null;
        }));
    }

    @ApplicationCommand(name = "restart", description = "Stops the bot, which will restart with the latest image")
    public static class RestartData {
        Boolean sudo;

        public RestartData(@Description("Restarts immediately without waiting process termination") Boolean sudo) {
            this.sudo = sudo;
        }
    }
}