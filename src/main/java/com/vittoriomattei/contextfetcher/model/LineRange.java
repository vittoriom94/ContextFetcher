package com.vittoriomattei.contextfetcher.model;

import org.jetbrains.annotations.NotNull;

public record LineRange(int startLine, int endLine) {
    @Override
    public @NotNull String toString() {
        return startLine + " - " + endLine;
    }
}
