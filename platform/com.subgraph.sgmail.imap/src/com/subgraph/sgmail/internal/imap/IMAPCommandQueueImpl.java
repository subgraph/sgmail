package com.subgraph.sgmail.internal.imap;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableLinkedList;
import com.db4o.ta.Activatable;

import java.util.*;

public class IMAPCommandQueueImpl implements IMAPCommandQueue, Activatable {

    private final Queue<IMAPCommand> commandQueue = new ActivatableLinkedList<>();//new ArrayDeque<>();
    
	private transient Activator activator;

    @Override
    public synchronized int getPendingCommandCount() {
        activate(ActivationPurpose.READ);
        return commandQueue.size();
    }

    @Override
    public synchronized IMAPCommand peekNextCommand() {
        activate(ActivationPurpose.READ);
        return commandQueue.peek();
    }

    @Override
    public synchronized void removeCommand(IMAPCommand command) {
        activate(ActivationPurpose.WRITE);
        if(command == null) {
            throw new NullPointerException("argument to removeCommand() must not be null");
        }
        commandQueue.remove(command);
    }

    @Override
    public void addCommandListener(IMAPCommandListener listener) {
    	throw new UnsupportedOperationException("Not implemented yet...");
    }

    @Override
    public void removeCommandListener(IMAPCommandListener listener) {
    	throw new UnsupportedOperationException("Not implemented yet...");
    }

    @Override
    public synchronized void queueCommand(IMAPCommand command) {
        activate(ActivationPurpose.WRITE);

        if(queueContainsFolder(command)) {
            insertCommandGroupedByFolder(command);
        } else {
            commandQueue.add(command);
        }
    }

    private void insertCommandGroupedByFolder(IMAPCommand command) {
        final List<IMAPCommand> commands = new ArrayList<>(commandQueue);
        commandQueue.clear();

        for(Iterator<IMAPCommand> it = commands.iterator(); it.hasNext();) {
            IMAPCommand c = it.next();
            commandQueue.add(c);
            if(haveSameFolder(c, command)) {
                appendFromFolderMatch(it, command);
                return;
            }
        }
        // Shouldn't happen...
        commandQueue.add(command);
    }

    /**
     * Append remaining items to Queue after an element with a matching folderName is discovered.
     *
     * @param it An iterator positioned at the element following the first element with a matching folder name
     * @param command The command to be inserted
     */
    private void appendFromFolderMatch(Iterator<IMAPCommand> it, IMAPCommand command) {
        final IMAPCommand next = skipMatchingFolderName(it, command.getFolderName());
        commandQueue.add(command);
        if(next != null) {
            commandQueue.add(next);
        }
        while(it.hasNext()) {
            commandQueue.add(it.next());
        }
    }

    private IMAPCommand skipMatchingFolderName(Iterator<IMAPCommand> it, String folderName) {
        while(it.hasNext()) {
            IMAPCommand next = it.next();
            if(!next.getFolderName().equals(folderName)) {
               return next;
            }
            commandQueue.add(next);
        }
        return null;
    }

    private boolean queueContainsFolder(IMAPCommand command) {
        for(IMAPCommand c: commandQueue) {
            if(haveSameFolder(c, command)) {
                return true;
            }
        }
        return false;
    }

    private boolean haveSameFolder(IMAPCommand c1, IMAPCommand c2) {
        return c1.getFolderName().equals(c2.getFolderName());
    }
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

}
