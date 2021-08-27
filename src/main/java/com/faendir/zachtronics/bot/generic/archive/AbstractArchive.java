package com.faendir.zachtronics.bot.generic.archive;

import com.faendir.zachtronics.bot.main.git.GitRepository;
import com.faendir.zachtronics.bot.model.Solution;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractArchive<S extends Solution> implements Archive<S> {
    public abstract GitRepository getGitRepo();

    @NotNull
    public Mono<List<String>> archive(@NotNull S solution) {
        return getGitRepo().access(a -> performArchive(a, solution));
    }

    public abstract Path relativePuzzlePath(@NotNull S solution);

    public abstract SolutionsIndex<S> makeSolutionIndex(@NotNull Path puzzlePath,
                                                        @NotNull S solution) throws IOException;

    @NotNull
    private List<String> performArchive(@NotNull GitRepository.AccessScope accessScope, @NotNull S solution) {
        Path repoPath = accessScope.getRepo().toPath();
        Path puzzlePath = repoPath.resolve(relativePuzzlePath(solution));
        boolean frontierChanged;
        try {
            SolutionsIndex<S> index = makeSolutionIndex(puzzlePath, solution);
            frontierChanged = index.add(solution);
        } catch (IOException e) {
            // failures could happen after we dirtied the repo, so we call reset&clean on the puzzle dir
            accessScope.resetAndClean(puzzlePath.toFile());
            log.warn("Recoverable error during archive: ", e);
            return Collections.emptyList();
        }

        if (frontierChanged && !accessScope.status().isClean()) {
            accessScope.addAll(puzzlePath.toFile());
            String repoUrl = accessScope.originUrl();
            List<String> result = Stream.concat(accessScope.status().getChanged().stream(),
                                                accessScope.status().getAdded().stream())
                                        .map(f -> "[" + f.replaceFirst(".+/", "") + "](" + repoUrl + "/" + f + ")")
                                        .collect(Collectors.toList());
            accessScope.commitAndPush(
                    "Added " + solution.getScore().toDisplayString() + " for " + solution.getPuzzle().getDisplayName());
            result.add(0, "[commit](" + repoUrl + "/" + accessScope.currentHash() + ")");
            return result;
        }
        else
            return Collections.emptyList();
    }
}
