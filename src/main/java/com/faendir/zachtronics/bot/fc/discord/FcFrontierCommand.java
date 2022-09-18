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

package com.faendir.zachtronics.bot.fc.discord;

import com.faendir.zachtronics.bot.discord.command.AbstractFrontierCommand;
import com.faendir.zachtronics.bot.discord.command.option.CommandOption;
import com.faendir.zachtronics.bot.discord.command.option.OptionHelpersKt;
import com.faendir.zachtronics.bot.fc.FcQualifier;
import com.faendir.zachtronics.bot.fc.model.FcCategory;
import com.faendir.zachtronics.bot.fc.model.FcPuzzle;
import com.faendir.zachtronics.bot.fc.model.FcRecord;
import com.faendir.zachtronics.bot.fc.repository.FcSolutionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@FcQualifier
public class FcFrontierCommand extends AbstractFrontierCommand<FcCategory, FcPuzzle, FcRecord> {
    @Getter
    private final CommandOption<String, FcPuzzle> puzzleOption = OptionHelpersKt.enumOptionBuilder("puzzle", FcPuzzle.class, FcPuzzle::getDisplayName)
            .description("Puzzle name. Can be shortened or abbreviated. E.g. `1-1`, `add 2-3`")
            .required()
            .build();
    @Getter
    private final FcSolutionRepository repository;
}
