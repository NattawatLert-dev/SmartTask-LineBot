package com.smarttask.parser;

public class ParsedCommand {

    private final CommandType commandType;
    private final String taskName;

    public ParsedCommand(CommandType commandType, String taskName) {
        this.commandType = commandType;
        this.taskName = taskName;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getTaskName() {
        return taskName;
    }
}