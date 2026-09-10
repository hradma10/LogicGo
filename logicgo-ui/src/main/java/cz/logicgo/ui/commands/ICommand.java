package cz.logicgo.ui.commands;


interface ICommand {
    void execute();

    void undo();

    byte[] getCommandsAsBytes();
}
