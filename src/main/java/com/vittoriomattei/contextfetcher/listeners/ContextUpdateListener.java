package com.vittoriomattei.contextfetcher.listeners;

@FunctionalInterface
public interface ContextUpdateListener {
    void onContextUpdated(String newContent, String status);
}
