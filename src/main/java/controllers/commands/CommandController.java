package controllers.commands;

import java.util.*;

public class CommandController {

    private final List<Command> history;

    private final Deque<UndoableRedoableCommand> undos;

    private final Deque<UndoableRedoableCommand> redos;

    public CommandController() {
        this.history = new ArrayList<>();
        this.undos = new ArrayDeque<>();
        this.redos = new ArrayDeque<>();
    }

    public void execute(Command command) {
        if (command == null)
            return;

        System.out.println(command);
        command.execute();

        this.history.add(command);
        this.undos.clear();
        this.redos.clear();
    }

    public void execute(UndoableRedoableCommand command) {
        if (command == null)
            return;

        System.out.println(command);

        command.execute();

        this.history.add(command);
        this.redos.clear();
        this.undos.push(command);
    }

    public void undo() {
        if (this.undos.isEmpty())
            return;

        UndoableRedoableCommand command = this.undos.pop();
        System.out.println(command);

        command.undo();

        this.redos.push(command);
        this.history.add(command);
    }

    public void redo() {
        if (this.redos.isEmpty())
            return;

        UndoableRedoableCommand command = this.redos.pop();
        System.out.println(command);

        command.redo();

        this.undos.push(command);
        this.history.add(command);
    }

    public boolean canUndo() {
        return !this.undos.isEmpty();
    }

    public boolean canRedo() {
        return !this.redos.isEmpty();
    }

    public List<Command> getHistory() {
        return Collections.unmodifiableList(this.history);
    }
}
