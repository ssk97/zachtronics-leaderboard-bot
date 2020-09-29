package com.faendir.zachtronics.bot.git

import com.faendir.zachtronics.bot.config.GitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GitConfiguration(private val gitProperties: GitProperties) {

    @Bean("omGithubPagesLeaderboardRepository")
    fun omGithubPagesLeaderboardRepository() = GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git")

    @Bean("omRedditLeaderboardRepository")
    fun omRedditLeaderboardRepository() = GitRepository(gitProperties, "om-wiki", "https://github.com/F43nd1r/OM-wiki.git")

    @Bean("configRepository")
    fun configRepository() = GitRepository(gitProperties, "config", "https://github.com/F43nd1r/zachtronics-leaderboard-bot-config.git")
}