# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build System and Commands

This is a Gradle-based IntelliJ IDEA plugin project using Kotlin DSL.

### Common Commands
- **Build the plugin**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run plugin in IDE**: `./gradlew runIde`
- **Build plugin distribution**: `./gradlew buildPlugin`

### Project Structure
- Uses IntelliJ Platform Gradle Plugin 2.6.0
- Targets IntelliJ IDEA Community Edition 2025.1
- Java/Kotlin compatibility: JDK 21
- Test framework: JUnit 4.13.2 with Mockito 5.11.0

## Architecture Overview

ContextFetcher is an IntelliJ IDEA plugin that helps developers collect code context from multiple files and snippets to feed into AI chat systems.

### Core Components

**Services (located in `services/` package):**
- `FileAggregatorService`: Interface for managing file and snippet collection
- `FileAggregatorServiceImpl`: Implementation that tracks files and code snippets
- `ContextGeneratorService`: Generates formatted markdown context from collected files/snippets

**UI Architecture (located in `ui/` package):**
- `ContextFetcherPanel`: Main panel using BorderLayout with toolbar, file list, and preview
- `ToolbarPanel`: Contains action buttons for file management
- `FileListPanel`: Displays collected files and snippets in a list
- `PreviewPanel`: Shows generated context output
- Uses JBSplitter for resizable panes (60/40 split)

**Data Models (located in `model/` package):**
- `FileContextItem`: Individual context items representing either complete files or file snippets
- `LineRange`: Represents line ranges for code snippets

**Actions (located in `actions/` package):**
All plugin actions follow IntelliJ's Action framework:
- `AddFileAction`: Add files/directories to context
- `AddOpenFilesAction`: Add all currently open files
- `AddSelectionAction`: Add selected text as snippet
- `GenerateContextAction`: Generate formatted output
- `RemoveSelectedAction`: Remove selected items
- `ClearAllAction`: Clear all context

### Key Patterns

**Service Registration**: Services are registered as projectServices in plugin.xml and accessed via Project.getService()

**Data Flow**: FileAggregatorService manages state → ContextGeneratorService formats output → UI components display/interact

**Context Generation**: Creates markdown-formatted output with file paths, code blocks with syntax highlighting, and snippet line numbers

**File vs Snippet Handling**: FileContextItem can represent either complete files or file snippets. Files and snippets are tracked separately - adding a snippet to an existing complete file does not modify the complete file entry.

### Plugin Integration Points

- **Tool Window**: Registered as "ContextAggregator" docked to right side
- **Context Menus**: Actions available in editor popup, project view, and editor tabs
- **Main Toolbar**: Actions accessible from main IDE toolbar
- **Data Provider**: Uses custom DataKeys for action context

The plugin follows IntelliJ's threading model with ReadAction for safe file system access and uses CopyOnWriteArrayList for thread-safe listener management.

## Current Status (2025-01-04)

**Refactoring Status**: Major refactoring completed on branch `refactor-code-to-use-actions-instead-of-swing-components`
- ✅ Migrated from `FileEntry` to `FileContextItem` architecture  
- ✅ Updated all services, UI components, and actions
- ✅ Fixed all test compilation issues and updated test suite
- ✅ All changes staged and ready for commit

**Next Steps**: 
1. **MUST run tests first** - Execute `./gradlew test` in IntelliJ to verify all tests pass
2. If tests pass, the refactoring can be committed and merged
3. If tests fail, investigate and fix any remaining issues

**Note**: Cannot run Gradle from this WSL terminal - tests must be run through IntelliJ IDE.