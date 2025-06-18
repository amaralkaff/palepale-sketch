package com.example.drawinggame.ui.drawing.operations.commands

/**
 * Base interface for all drawable commands in the command pattern.
 * Each command encapsulates a drawing operation that can be executed and undone.
 */
interface DrawCommand {
    
    /**
     * Execute the command
     * @return true if execution was successful, false otherwise
     */
    fun execute(): Boolean
    
    /**
     * Undo the command, reversing its effects
     * @return true if undo was successful, false otherwise
     */
    fun undo(): Boolean
    
    /**
     * Get a human-readable description of this command
     * @return description string for UI display
     */
    fun getDescription(): String
    
    /**
     * Get the estimated memory usage of this command in bytes
     * @return memory usage in bytes
     */
    fun getMemoryUsage(): Long
    
    /**
     * Check if this command can be merged with another command
     * @param other the command to potentially merge with
     * @return true if commands can be merged, false otherwise
     */
    fun canMerge(other: DrawCommand): Boolean
    
    /**
     * Merge this command with another command
     * @param other the command to merge with
     * @return a new command representing the merged operation
     */
    fun merge(other: DrawCommand): DrawCommand
} 