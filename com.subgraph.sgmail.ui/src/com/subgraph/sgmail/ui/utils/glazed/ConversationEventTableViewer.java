/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.subgraph.sgmail.ui.panes.middle;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
import ca.odell.glazedlists.swt.TableColumnConfigurer;
import ca.odell.glazedlists.swt.TableItemConfigurer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;


/**
 * A view helper that displays an EventList in an SWT table.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class ConversationEventTableViewer<E> implements ListEventListener<E> {

    /** the heavyweight table */
    private Table table;

    /** the original source EventList to which this EventTableViewer is listening */
    private EventList<E> originalSource;

    /** the actual EventList to which this EventTableViewer is listening */
    protected EventList<E> source;

    /** to manipulate Tables in a generic way */
    private TableHandler<E> tableHandler;

    /** Specifies how to render table headers and sort */
    private TableFormat<? super E> tableFormat;

    /** Specifies how to render column values represented by TableItems. */
    private TableItemConfigurer<? super E> tableItemConfigurer;

    /**
     * Creates a new viewer for the given {@link org.eclipse.swt.widgets.Table} that updates the table
     * contents in response to changes on the specified {@link ca.odell.glazedlists.EventList}. The
     * {@link org.eclipse.swt.widgets.Table} is formatted with the specified {@link ca.odell.glazedlists.gui.TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public ConversationEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
        this(source, table, tableFormat, TableItemConfigurer.DEFAULT);
    }

    /**
     * Creates a new viewer for the given {@link org.eclipse.swt.widgets.Table} that updates the table
     * contents in response to changes on the specified {@link ca.odell.glazedlists.EventList}. The
     * {@link org.eclipse.swt.widgets.Table} is formatted with the specified {@link ca.odell.glazedlists.gui.TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    public ConversationEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
                                        TableItemConfigurer<? super E> tableItemConfigurer) {
    	this(source, table, tableFormat, tableItemConfigurer, false);
    }

    /**
     * Creates a new viewer for the given {@link org.eclipse.swt.widgets.Table} that updates the table
     * contents in response to changes on the specified {@link ca.odell.glazedlists.EventList}. The
     * {@link org.eclipse.swt.widgets.Table} is formatted with the specified {@link ca.odell.glazedlists.gui.TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     * @param disposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     */
    protected ConversationEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
                                           TableItemConfigurer<? super E> tableItemConfigurer, boolean disposeSource) {
        // check for valid arguments early
        if (source == null)
            throw new IllegalArgumentException("source list may not be null");
        if (table == null)
            throw new IllegalArgumentException("Table may not be null");
        if (tableFormat == null)
            throw new IllegalArgumentException("TableFormat may not be null");
        if (tableItemConfigurer == null)
            throw new IllegalArgumentException("TableItemConfigurer may not be null");

        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableViewer
        source.getReadWriteLock().writeLock().lock();
        try {
            this.source = source;
            if (disposeSource) {
            	this.originalSource = source;
            }
            this.table = table;
            this.tableFormat = tableFormat;
            this.tableItemConfigurer = tableItemConfigurer;

            // configure how the Table will be manipulated
            if(isTableVirtual()) {
                tableHandler = new VirtualTableHandler();
            } else {
                tableHandler = new DefaultTableHandler();
            }

            // setup the Table with initial values
            initTable();
            tableHandler.populateTable();

            // prepare listeners
            this.source.addListEventListener(this);

        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets wether the table is virtual or not.
     */
    private boolean isTableVirtual() {
        return ((table.getStyle() & SWT.VIRTUAL) == SWT.VIRTUAL);
    }

    /**
     * Builds the columns and headers for the {@link org.eclipse.swt.widgets.Table}
     */
    private void initTable() {
        //table.setHeaderVisible(true);
        final TableColumnConfigurer configurer = getTableColumnConfigurer();
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            TableColumn column = new TableColumn(table, SWT.LEFT, c);
            column.setText(tableFormat.getColumnName(c));
            column.setWidth(80);
            if (configurer != null) {
                configurer.configure(column, c);
            }
        }
    }

    /** Removes existing columns from the table. */
    private void removeTableColumns() {
    	while (table.getColumnCount() > 0) {
    	    table.getColumns()[0].dispose();
    	}
    }

    /**
     * Sets all of the column values on a {@link org.eclipse.swt.widgets.TableItem}.
     */
    private void renderTableItem(TableItem item, E value, int row) {
        item.setData(value);
        for(int i = 0; i < tableFormat.getColumnCount(); i++) {
            final Object cellValue = tableFormat.getColumnValue(value, i);
            tableItemConfigurer.configure(item, value, cellValue, row, i);
        }
    }

    /**
     * Gets the {@link ca.odell.glazedlists.gui.TableFormat}.
     */
    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }

    /**
     * Sets this {@link org.eclipse.swt.widgets.Table} to be formatted by a different {@link ca.odell.glazedlists.gui.TableFormat}.
     * @param tableFormat the new TableFormat
     * @throws IllegalArgumentException if tableFormat is <code>null</code>
     */
    public void setTableFormat(TableFormat<? super E> tableFormat) {
    	if (tableFormat == null)
    		throw new IllegalArgumentException("TableFormat may not be null");
        this.tableFormat = tableFormat;

        table.setRedraw(false);
        removeTableColumns();
        initTable();
		this.tableHandler.redraw();
		table.setRedraw(true);
    }

    /**
     * Gets the {@link ca.odell.glazedlists.swt.TableItemConfigurer}.
     */
    public TableItemConfigurer<? super E> getTableItemConfigurer() {
        return tableItemConfigurer;
    }

    /**
     * Sets a new {@link ca.odell.glazedlists.swt.TableItemConfigurer}. The cell values of existing,
     * non-virtual table items will be reconfigured with the specified configurer.
     */
    public void setTableItemConfigurer(TableItemConfigurer<? super E> tableItemConfigurer) {
        if (tableItemConfigurer == null)
            throw new IllegalArgumentException("TableItemConfigurer may not be null");

        this.tableItemConfigurer = tableItemConfigurer;
        // determine the index of the last, non-virtual table item
        final int maxIndex = tableHandler.getLastIndex();
        if (maxIndex < 0) return;
        // Disable redraws so that the table is updated in bulk
        table.setRedraw(false);
        source.getReadWriteLock().readLock().lock();
        try {
            // reprocess all table items between indexes 0 and maxIndex
            for (int i = 0; i <= maxIndex; i++) {
    //            System.out.println("setTableItemConfigurer: Reconfigure Item " + i);
                final E rowValue = source.get(i);
                for (int c = 0; c < tableFormat.getColumnCount(); c++) {
                    final Object columnValue = tableFormat.getColumnValue(rowValue, c);
                    tableItemConfigurer.configure(table.getItem(i), rowValue, columnValue, i, c);
                }
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        // Re-enable redraws to update the table
        table.setRedraw(true);
    }

    /**
     * Gets the {@link ca.odell.glazedlists.swt.TableColumnConfigurer} or <code>null</code> if not
     * available.
     */
    private TableColumnConfigurer getTableColumnConfigurer() {
        if (tableFormat instanceof TableColumnConfigurer) {
            return (TableColumnConfigurer) tableFormat;
        }
        return null;
    }

    /**
     * Gets the {@link org.eclipse.swt.widgets.Table} that is being managed by this
     * {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer}.
     */
    public Table getTable() {
        return table;
    }

    /**
     * Get the source of this {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer}.
     */
    public EventList<E> getSourceList() {
        return source;
    }

   /**
     * When the source list is changed, this forwards the change to the
     * displayed {@link org.eclipse.swt.widgets.Table}.
     */
    public void listChanged(ListEvent listChanges) {
        // if the table is no longer available, we don't want to do anything as
        // it will result in a "Widget is disposed" exception
        if (table.isDisposed()) return;

        Barcode deletes = new Barcode();
        deletes.addWhite(0, source.size());
        int firstChange = source.size();
        // Disable redraws so that the table is updated in bulk
        table.setRedraw(false);

        // Apply changes to the list
        while (listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int adjustedIndex = deletes.getIndex(changeIndex, Barcode.WHITE);
            int changeType = listChanges.getType();

            // Insert a new element in the Table and the Barcode
            if (changeType == ListEvent.INSERT) {
                deletes.addWhite(adjustedIndex, 1);
                tableHandler.addRow(adjustedIndex, source.get(changeIndex));
                firstChange = Math.min(changeIndex, firstChange);

                // Update the element in the Table
            } else if (changeType == ListEvent.UPDATE) {
                tableHandler.updateRow(adjustedIndex, source.get(changeIndex));

                // Just mark the element as deleted in the Barcode
            } else if (changeType == ListEvent.DELETE) {
                deletes.setBlack(adjustedIndex, 1);
                firstChange = Math.min(changeIndex, firstChange);
            }
        }

        // Process the deletes as a single Table change
        if (deletes.blackSize() > 0) {
            int[] deletedIndices = new int[deletes.blackSize()];
            for (BarcodeIterator i = deletes.iterator(); i.hasNextBlack();) {
                i.nextBlack();
                deletedIndices[i.getBlackIndex()] = i.getIndex();
            }
            tableHandler.removeAll(deletedIndices);
        }

        // Re-enable redraws to update the table
        table.setRedraw(true);
    }

    /**
     * Releases the resources consumed by this {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link ca.odell.glazedlists.EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer}
     * to be garbage collected before its source {@link ca.odell.glazedlists.EventList}. This is
     * necessary for situations where an {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer} is short-lived but
     * its source {@link ca.odell.glazedlists.EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link com.subgraph.sgmail.ui.panes.middle.ConversationEventTableViewer} after it has been disposed.
     */
    public void dispose() {
        tableHandler.dispose();
        source.removeListEventListener(this);

        if (originalSource != null)
        	originalSource.dispose();

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        tableHandler = null;
        source = null;
        originalSource = null;
    }

    /**
     * Defines how Tables will be manipulated.
     */
    private interface TableHandler<E> {

        /**
         * Populate the Table with data.
         */
        public void populateTable();

        /**
         * Add a row with the given value.
         */
        public void addRow(int row, E value);

        /**
         * Update a row with the given value.
         */
        public void updateRow(int row, E value);

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows);

        /**
         * Disposes of this TableHandler
         */
        public void dispose();

        /**
         * Gets the last real, non-virtual row index. -1 means empty or
         * completely virtual table
         */
        public int getLastIndex();

        /**
         * redraws the table
         */
        public void redraw();
    }

    /**
     * Allows manipulation of standard SWT Tables.
     */
    private final class DefaultTableHandler implements TableHandler<E> {
        /**
         * Populate the Table with initial data.
         */
        public void populateTable() {
            for(int i = 0, n = source.size(); i < n; i++) {
                addRow(i, source.get(i));
            }
        }

        /**
         * Adds a row with the given value.
         */
        public void addRow(int row, E value) {
            TableItem item = new TableItem(table, 0, row);
            renderTableItem(item, value, row);
        }

        /**
         * Updates a row with the given value.
         */
        public void updateRow(int row, E value) {
            TableItem item = table.getItem(row);
            renderTableItem(item, value, row);
        }

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows) {
            table.remove(rows);
        }

        /**
         * Disposes of this TableHandler.
         */
        public void dispose() {
            // no-op for default Tables
        }

        /** {@inheritedDoc} */
        public int getLastIndex() {
            return table.getItemCount() - 1;
        }

        /** {@inheritedDoc} */
        public void redraw() {
            for (int i=0; i<source.size(); i++) {
				renderTableItem(table.getItem(i), source.get(i), i);
			}
		}
    }

    /**
     * Allows manipulation of Virtual Tables and handles additional aspects
     * like providing the SetData callback method and tracking which values
     * are Virtual.
     */
    private final class VirtualTableHandler implements TableHandler<E>, Listener {

        /** to keep track of what's been requested */
        private final Barcode requested = new Barcode();

        /**
         * Create a new VirtualTableHandler.
         */
        public VirtualTableHandler() {
            requested.addWhite(0, source.size());
            table.addListener(SWT.SetData, this);
        }

        /**
         * Populate the Table with initial data.
         */
        public void populateTable() {
            table.setItemCount(source.size());
        }

        /**
         * Adds a row with the given value.
         */
        public void addRow(int row, E value) {
            // Adding before the last non-Virtual value
            if(row <= getLastIndex()) {
                requested.addBlack(row, 1);
                TableItem item = new TableItem(table, 0, row);
                renderTableItem(item, value, row);

            // Adding in the Virtual values at the end
            } else {
                requested.addWhite(requested.size(), 1);
                table.setItemCount(table.getItemCount() + 1);
            }
        }

        /**
         * Updates a row with the given value.
         */
        public void updateRow(int row, E value) {
            // Only set a row if it is NOT Virtual
            if(!isVirtual(row)) {
                requested.setBlack(row, 1);
                TableItem item = table.getItem(row);
                renderTableItem(item, value, row);
            }
        }

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows) {
            // Sync the requested barcode to clear values that have been removed
            for(int i = 0; i < rows.length; i++) {
                requested.remove(rows[i] - i, 1);
            }
            table.remove(rows);
        }

        /**
         * Returns the highest index that has been requested or -1 if the
         * Table is entirely Virtual.
         */
        public int getLastIndex() {
            // Everything is Virtual
            if(requested.blackSize() == 0) return -1;

            // Return the last index
            else return requested.getIndex(requested.blackSize() - 1, Barcode.BLACK);
        }

        /**
         * Returns whether a particular row is Virtual in the Table.
         */
        private boolean isVirtual(int rowIndex) {
            return requested.getBlackIndex(rowIndex) == -1;
        }

        /**
         * Respond to requests for values to fill Virtual rows.
         */
        public void handleEvent(Event e) {
            // Get the TableItem from the Table
            TableItem item = (TableItem)e.item;

            // Calculate the index that should be requested because the Table
            // might be sending incorrectly indexed TableItems in the event.
            int whiteIndex = requested.getWhiteIndex(table.indexOf(item), false);
            int index = requested.getIndex(whiteIndex, Barcode.WHITE);
//            System.out.println("ETV.handleEvent: e.index|index|topindex|lastindex=" + e.index + "|"
//                    + index + "|" + table.getTopIndex() + "|" + getLastIndex());
            // Set the value on the Virtual element
            requested.setBlack(index, 1);
            renderTableItem(item, source.get(index), index);
        }

        /**
         * Allows this handler to clean up after itself.
         */
        public void dispose() {
            table.removeListener(SWT.SetData, this);
        }

        /** {@inheritedDoc} */
        public void redraw() {
			this.requested.clear();
			table.setItemCount(0);

			requested.addWhite(0, source.size());
			table.setItemCount(source.size());
		}
    }
}