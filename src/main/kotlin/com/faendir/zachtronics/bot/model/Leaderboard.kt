package com.faendir.zachtronics.bot.model

interface Leaderboard<C : Category, P : Puzzle, R : Record> {
    val supportedCategories: List<C>

    fun update(puzzle: P, record: R): UpdateResult = UpdateResult.NotSupported()

    fun get(puzzle: P, category: C): R?

    fun getAll(puzzle: P, categories: Collection<C>): Map<C, R>
}

sealed class UpdateResult {
    class Success(val oldScores: Map<out Category, Score?>) : UpdateResult()

    class ParetoUpdate : UpdateResult()

    class BetterExists(val scores: Map<out Category, Score>) : UpdateResult()

    class NotSupported : UpdateResult()
}

