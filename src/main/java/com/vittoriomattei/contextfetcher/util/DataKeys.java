package com.vittoriomattei.contextfetcher.util;

import com.intellij.openapi.actionSystem.DataKey;
import com.vittoriomattei.contextfetcher.model.FileContextItem;

import java.util.ArrayList;
import java.util.List;

public class DataKeys {
    public static final DataKey<List<FileContextItem>> SELECTED_FILES_KEY = DataKey.create("contextfetcher.selected_files");
}
