/*
 * Copyright (c) 2023
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

package com.faendir.zachtronics.bot.utils

import com.faendir.zachtronics.bot.utils.InfinInt.Companion.MAX_FINITE_VALUE
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.MIN_FINITE_VALUE
import com.faendir.zachtronics.bot.utils.InfinInt.Companion.toInfinInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Represents an immutable element in {[MIN_FINITE_VALUE], ..., [MAX_FINITE_VALUE]} U {INF} */
@Serializable(with = InfinIntSerializer::class)
class InfinInt private constructor(private val value: Int) : Number(), Comparable<InfinInt> {
    companion object {
        private const val MIN_FINITE_VALUE: Int = Int.MIN_VALUE
        private const val MAX_FINITE_VALUE: Int = Int.MAX_VALUE - 1
        private const val INFINITY_VALUE: Int = Int.MAX_VALUE

        val INFINITY: InfinInt = InfinInt(INFINITY_VALUE)

        fun Int.toInfinInt(): InfinInt {
            if (this > MAX_FINITE_VALUE)
                throw IllegalArgumentException("Unrepresentable Int: $this")
            return InfinInt(this)
        }

        fun Double.toInfinInt(): InfinInt {
            if (this == Double.POSITIVE_INFINITY)
                return INFINITY
            return InfinInt(this.toInt())
        }

        fun String.toInfinInt(): InfinInt {
            if (this == "∞" || this == "INF")
                return INFINITY
            return InfinInt(this.toInt())
        }
    }

    override fun toDouble(): Double {
        if (value == INFINITY_VALUE)
            return Double.POSITIVE_INFINITY
        return value.toDouble()
    }

    override fun toFloat(): Float {
        if (value == INFINITY_VALUE)
            return Float.POSITIVE_INFINITY
        return value.toFloat()
    }

    override fun toLong(): Long {
        if (value == INFINITY_VALUE)
            throw IllegalArgumentException("The margin of this Long is too small to contain **INFINITY**")
        return value.toLong()
    }

    override fun toInt(): Int {
        if (value == INFINITY_VALUE)
            throw IllegalArgumentException("The margin of this Int is too small to contain **INFINITY**")
        return value
    }

    override fun toChar(): Char {
        if (value == INFINITY_VALUE)
            throw IllegalArgumentException("The margin of this Char is too small to contain **INFINITY**")
        return value.toChar()
    }

    override fun toShort(): Short {
        if (value == INFINITY_VALUE)
            throw IllegalArgumentException("The margin of this Short is too small to contain **INFINITY**")
        return value.toShort()
    }

    override fun toByte(): Byte {
        if (value == INFINITY_VALUE)
            throw IllegalArgumentException("The margin of this Byte is too small to contain **INFINITY**")
        return value.toByte()
    }

    override fun compareTo(other: InfinInt): Int {
        // infinity is correctly ordered so this works
        return value - other.value
    }

    override fun toString(): String {
        if (value == INFINITY_VALUE)
            return "∞"
        return value.toString()
    }

    fun toLatinString(): String {
        if (value == INFINITY_VALUE)
            return "INF"
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InfinInt

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

}

object InfinIntSerializer : KSerializer<InfinInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InfinInt", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: InfinInt) {
        encoder.encodeString(value.toLatinString())
    }

    override fun deserialize(decoder: Decoder): InfinInt {
        return decoder.decodeString().toInfinInt()
    }
}