package zz.utils.undo2;

import java.util.ArrayList;

import javax.swing.Action;

import zz.utils.ArrayStack;
import zz.utils.Stack;

public class UndoStack
{
	private static ThreadLocal<UndoStack> itsCurrentStack = new ThreadLocal<UndoStack>();
	
	private Stack<Operation> itsUndoStack = new ArrayStack<Operation>();
	private Stack<Operation> itsRedoStack = new ArrayStack<Operation>();
	private Operation itsCurrentOperation;
	
	protected Action itsUndoAction;
	protected Action itsRedoAction;

	public static UndoStack getCurrent()
	{
		return itsCurrentStack.get();
	}
	
	public void startOperation()
	{
		if (itsCurrentOperation != null) throw new IllegalStateException();
		itsCurrentStack.set(this);
		itsCurrentOperation = new Operation();
	}
	
	public void commitOperation()
	{
		if (itsCurrentOperation == null) throw new IllegalStateException();
		itsCurrentStack.set(this);
		itsUndoStack.push(itsCurrentOperation);
	}
	
	public void cancelOperation()
	{
		if (itsCurrentOperation == null) throw new IllegalStateException();
		itsCurrentOperation.undo();
	}

	public void setActions (Action aUndoAction, Action aRedoAction)
	{
		itsUndoAction = aUndoAction;
		itsRedoAction = aRedoAction;
		updateUndoActions();
	}
	
	/**
	 * Undoes the command on the top of the stack
	 * @return The executed command
	 */
	public void undo ()
	{
		if (getCurrent() != null) throw new IllegalStateException("Cannot undo or redo while an operation is in progress");
		if (itsUndoStack.isEmpty()) return;

		Operation theOperation = itsUndoStack.pop();
		theOperation.undo();
		itsRedoStack.push(theOperation);
		updateUndoActions();
	}

	/**
	 * Redoes the command on the top of the redo stack
	 * @return the executed command
	 */
	public void redo ()
	{
		if (getCurrent() != null) throw new IllegalStateException("Cannot undo or redo while an operation is in progress");
		if (itsRedoStack.isEmpty()) return;

		Operation theOperation = itsRedoStack.pop();
		theOperation.perform();
		itsUndoStack.push(theOperation);
		updateUndoActions();
	}

	protected void updateUndoActions ()
	{
		if (itsUndoAction != null) itsUndoAction.setEnabled(! itsUndoStack.isEmpty());
		if (itsRedoAction != null) itsRedoAction.setEnabled(! itsRedoStack.isEmpty());
	}
	
	public void addCommand(Command aCommand)
	{
		if (itsCurrentOperation == null) throw new IllegalStateException("No current operation");
		itsCurrentOperation.addCommand(aCommand);
	}
	
	/**
	 * An operation groups a list of commands that are performed or undone together.
	 * @author gpothier
	 */
	private static class Operation
	{
		private ArrayList<Command> itsCommands = new ArrayList<Command>();
		
		public void addCommand(Command aCommand)
		{
			itsCommands.add(aCommand);
		}
		
		public void perform()
		{
			for (int i=0;i<itsCommands.size();i++) itsCommands.get(i).perform();
		}
		
		public void undo()
		{
			for (int i=itsCommands.size()-1;i>=0;i--) itsCommands.get(i).undo();
		}
	}
}
