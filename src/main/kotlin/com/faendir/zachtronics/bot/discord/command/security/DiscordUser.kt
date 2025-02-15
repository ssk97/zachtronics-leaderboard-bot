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

package com.faendir.zachtronics.bot.discord.command.security

import com.faendir.zachtronics.bot.utils.asReaction
import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji

enum class DiscordUser(val id: Long, val getSpecialEmoji: (Guild?) -> ReactionEmoji? = { null }) {
    F43ND1R(288766560938622976, { ReactionEmoji.unicode("\uD83C\uDDEB") }),
    IEEE12345(295868901042946048), // aka 12345IEEE
    TT(516462621382410260),
    ZIG(185983061190508544),
    OMGITSABIST(223019983557361664, { guild -> guild?.getGuildEmojiById(Snowflake.of(1030311764182704138L))?.block()?.asReaction() }),
    TULARE(219319015649181698, { ReactionEmoji.unicode("\uD83C\uDF52") }),
    REGULAR_HUMANOID(297954865223696384, { ReactionEmoji.unicode("\uD83D\uDC0C") }),
    SYX(206689051028357121),
    A_SNOWBALL(136186716334587904),
    ;

    companion object {
        @JvmField
        val BOT_OWNERS = setOf(F43ND1R, IEEE12345)
        val OM_LB_ADMINS = BOT_OWNERS + SYX;
    }
}

class DiscordUserSecured(users: Collection<DiscordUser>) : Secured {
    private val ids = users.map { it.id }.toSet()

    override fun hasExecutionPermission(event: InteractionCreateEvent, user: User): Boolean = ids.contains(user.id.asLong())
}

fun User.asDiscordUser() = DiscordUser.values().find { it.id == this.id.asLong() }