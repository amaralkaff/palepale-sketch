package com.example.drawinggame.ui.drawing.operations

import com.example.drawinggame.ui.drawing.operations.commands.DrawCommand
import java.util.Stack

/**
 * Central coordinator for all undoable operations using Command pattern.
 * Manages undo/redo stack and command execution.
 */
class CommandManager {
    
    // Command stacks for undo/redo functionality
    private val undoStack = Stack<DrawCommand>()
    private val redoStack = Stack<DrawCommand>()
    
    // Maximum number of operations to keep in history
    private var maxHistorySize = 50
    
    // Current memory usage tracking
    private var currentMemoryUsage = 0L
    private val maxMemoryUsage = 50 * 1024 * 1024L // 50MB limit
    
    // Listeners for command manager events
    private val listeners = mutableListOf<CommandManagerListener>()
    
    // Batch operation support
    private var batchMode = false
    private val batchCommands = mutableListOf<DrawCommand>()
    
    /**
     * Execute a command and add it to the undo stack
     */
    fun executeCommand(command: DrawCommand): Boolean {
        return try {
            // Execute the command
            val success = command.execute()
            
            if (success) {
                if (batchMode) {
                    // Add to batch instead of directly to stack
                    batchCommands.add(command)
                } else {
                    // Add to undo stack
                    addToUndoStack(command)
                    
                    // Clear redo stack when new command is executed
                    clearRedoStack()
                    
                    // Notify listeners
                    notifyCommandExecuted(command)
                }
            }
            
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Undo the last command
     */
    fun undo(): Boolean {
        if (undoStack.isEmpty()) {
            return false
        }
        
        return try {
            val command = undoStack.pop()
            val success = command.undo()
            
            if (success) {
                // Add to redo stack
                redoStack.push(command)
                currentMemoryUsage -= command.getMemoryUsage()
                
                // Notify listeners
                notifyCommandUndone(command)
                notifyStacksChanged()
            } else {
                // If undo failed, put command back
                undoStack.push(command)
            }
            
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Redo the last undone command
     */
    fun redo(): Boolean {
        if (redoStack.isEmpty()) {
            return false
        }
        
        return try {
            val command = redoStack.pop()
            val success = command.execute()
            
            if (success) {
                // Add back to undo stack
                undoStack.push(command)
                currentMemoryUsage += command.getMemoryUsage()
                
                // Notify listeners
                notifyCommandRedone(command)
                notifyStacksChanged()
            } else {
                // If redo failed, put command back
                redoStack.push(command)
            }
            
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Start batch mode for grouping multiple commands
     */
    fun startBatch() {
        batchMode = true
        batchCommands.clear()
    }
    
    /**
     * End batch mode and add all commands as a composite command
     */
    fun endBatch(description: String = "Batch Operation"): Boolean {
        batchMode = false
        
        if (batchCommands.isNotEmpty()) {
            val compositeCommand = CompositeCommand(batchCommands.toList(), description)
            batchCommands.clear()
            
            // Add composite command to undo stack
            addToUndoStack(compositeCommand)
            clearRedoStack()
            notifyCommandExecuted(compositeCommand)
            
            return true
        }
        
        return false
    }
    
    /**
     * Cancel current batch operation
     */
    fun cancelBatch() {
        if (batchMode) {
            // Undo all commands in current batch
            for (command in batchCommands.reversed()) {
                command.undo()
            }
            batchCommands.clear()
            batchMode = false
        }
    }
    
    /**
     * Clear all command history
     */
    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        currentMemoryUsage = 0L
        notifyStacksChanged()
        notifyHistoryCleared()
    }
    
    /**
     * Get the number of undoable operations
     */
    fun getUndoCount(): Int = undoStack.size
    
    /**
     * Get the number of redoable operations
     */
    fun getRedoCount(): Int = redoStack.size
    
    /**
     * Check if undo is available
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    
    /**
     * Check if redo is available
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    
    /**
     * Get description of the next undo operation
     */
    fun getUndoDescription(): String? {
        return if (undoStack.isNotEmpty()) {
            undoStack.peek().getDescription()
        } else null
    }
    
    /**
     * Get description of the next redo operation
     */
    fun getRedoDescription(): String? {
        return if (redoStack.isNotEmpty()) {
            redoStack.peek().getDescription()
        } else null
    }
    
    /**
     * Set maximum history size
     */
    fun setMaxHistorySize(size: Int) {
        maxHistorySize = size
        trimHistoryIfNeeded()
    }
    
    /**
     * Get current memory usage
     */
    fun getCurrentMemoryUsage(): Long = currentMemoryUsage
    
    /**
     * Add listener for command manager events
     */
    fun addListener(listener: CommandManagerListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    /**
     * Remove listener
     */
    fun removeListener(listener: CommandManagerListener) {
        listeners.remove(listener)
    }
    
    /**
     * Add command to undo stack with memory management
     */
    private fun addToUndoStack(command: DrawCommand) {
        // Try to merge with previous command if possible
        if (undoStack.isNotEmpty()) {
            val lastCommand = undoStack.peek()
            if (lastCommand.canMerge(command)) {
                // Replace last command with merged version
                undoStack.pop()
                currentMemoryUsage -= lastCommand.getMemoryUsage()
                
                val mergedCommand = lastCommand.merge(command)
                undoStack.push(mergedCommand)
                currentMemoryUsage += mergedCommand.getMemoryUsage()
                return
            }
        }
        
        // Add command normally
        undoStack.push(command)
        currentMemoryUsage += command.getMemoryUsage()
        
        // Trim history if needed
        trimHistoryIfNeeded()
        
        notifyStacksChanged()
    }
    
    /**
     * Clear redo stack and update memory usage
     */
    private fun clearRedoStack() {
        while (redoStack.isNotEmpty()) {
            val command = redoStack.pop()
            currentMemoryUsage -= command.getMemoryUsage()
        }
        notifyStacksChanged()
    }
    
    /**
     * Trim command history based on size and memory limits
     */
    private fun trimHistoryIfNeeded() {
        // Trim by count
        while (undoStack.size > maxHistorySize) {
            val oldCommand = undoStack.removeAt(0)
            currentMemoryUsage -= oldCommand.getMemoryUsage()
        }
        
        // Trim by memory usage
        while (currentMemoryUsage > maxMemoryUsage && undoStack.isNotEmpty()) {
            val oldCommand = undoStack.removeAt(0)
            currentMemoryUsage -= oldCommand.getMemoryUsage()
        }
        
        if (undoStack.isEmpty() || redoStack.isEmpty()) {
            notifyStacksChanged()
        }
    }
    
    /**
     * Notify listeners of command execution
     */
    private fun notifyCommandExecuted(command: DrawCommand) {
        listeners.forEach { it.onCommandExecuted(command) }
    }
    
    /**
     * Notify listeners of command undo
     */
    private fun notifyCommandUndone(command: DrawCommand) {
        listeners.forEach { it.onCommandUndone(command) }
    }
    
    /**
     * Notify listeners of command redo
     */
    private fun notifyCommandRedone(command: DrawCommand) {
        listeners.forEach { it.onCommandRedone(command) }
    }
    
    /**
     * Notify listeners of stack changes
     */
    private fun notifyStacksChanged() {
        listeners.forEach { it.onStacksChanged(canUndo(), canRedo()) }
    }
    
    /**
     * Notify listeners of history clear
     */
    private fun notifyHistoryCleared() {
        listeners.forEach { it.onHistoryCleared() }
    }
    
    /**
     * Interface for listening to command manager events
     */
    interface CommandManagerListener {
        fun onCommandExecuted(command: DrawCommand) {}
        fun onCommandUndone(command: DrawCommand) {}
        fun onCommandRedone(command: DrawCommand) {}
        fun onStacksChanged(canUndo: Boolean, canRedo: Boolean) {}
        fun onHistoryCleared() {}
    }
    
    /**
     * Composite command for grouping multiple operations
     */
    private class CompositeCommand(
        private val commands: List<DrawCommand>,
        private val description: String
    ) : DrawCommand {
        
        override fun execute(): Boolean {
            return commands.all { it.execute() }
        }
        
        override fun undo(): Boolean {
            return commands.reversed().all { it.undo() }
        }
        
        override fun getDescription(): String = description
        
        override fun getMemoryUsage(): Long {
            return commands.sumOf { it.getMemoryUsage() }
        }
        
        override fun canMerge(other: DrawCommand): Boolean = false
        
        override fun merge(other: DrawCommand): DrawCommand = this
    }
} 