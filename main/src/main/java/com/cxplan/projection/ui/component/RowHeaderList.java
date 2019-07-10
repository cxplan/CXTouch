package com.cxplan.projection.ui.component;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * A list with row header which displays the sequence number of rows.
 *
 * @author Kenny
 * created on 2019/4/5
 */
public class RowHeaderList<E> extends JList<E> {

    //The flag indicates whether the sequence number of rows should be displayed.
    private boolean showRowNumber;

    public RowHeaderList(ListModel dataModel) {
        super(dataModel);
        init();
    }

    public RowHeaderList(E[] listData) {
        super(listData);
        init();
    }

    public RowHeaderList(Vector listData) {
        super(listData);
        init();
    }

    public RowHeaderList() {
        init();
    }

    private void init() {
        setFixedCellHeight(16);
    }

    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
        Container con = getParent();
        if (!(con instanceof JViewport)) {
            return;
        }
        con = con.getParent();
        if (!(con instanceof JScrollPane)) {
            return;
        }
        JScrollPane scrollPane = (JScrollPane) con;
        if (showRowNumber)  //set header component
        {
            JViewport rowHeaderView = scrollPane.getRowHeader();
            if (rowHeaderView != null && rowHeaderView.getView() instanceof PrivateList) {
                return;
            } else {
                JList list = getRowHeadList();
                list.setBackground(scrollPane.getBackground());
                scrollPane.setRowHeaderView(list);
            }

        } else //remove header component
        {
            JViewport rowHeaderView = scrollPane.getRowHeader();
            if (rowHeaderView != null && rowHeaderView.getView() instanceof PrivateList) {
                rowHeaderView.remove(rowHeaderView.getView());
                ((PrivateList)rowHeaderView.getView()).release();
                scrollPane.getParent().validate();
            }
        }
    }
    /**
     * Return the icon of specified row displayed with row number in the left section of scroll pane.
     *
     * @param row the index of row in the view.
     */
    public Icon getRowIcon(int row) {
        return null;
    }

    /**
     * Create a new row header list.
     */
    JList getRowHeadList() {
        int tableRowCount = getModel().getSize();
        final PrivateList rowHeader = new PrivateList(tableRowCount);

        rowHeader.setFixedCellHeight(getFixedCellHeight());

        RowHeaderRenderer cellRender = new RowHeaderRenderer(this);
        rowHeader.setCellRenderer(cellRender);
        int width = 10;
        if (tableRowCount > 0) {
            width = (int) cellRender.getListCellRendererComponent(rowHeader, null, tableRowCount - 1, false, false)
                    .getPreferredSize().getWidth() + 16;
        }
        rowHeader.setFixedCellWidth(width);

        addPropertyChangeListener(new PropertyChangeListener() {

              public void propertyChange(PropertyChangeEvent evt) {
                  if (evt.getPropertyName().equals("fixedCellHeight")) {
                      rowHeader.setFixedCellHeight(getFixedCellHeight());
                  }

              }

          }
        );

        return rowHeader;
    }

    class PrivateList<String> extends JList<String> {

        private ListDataListener mainListListener;

        public PrivateList(int length) {
            super(new RowNumberListModel(length));
            RowNumberListModel model = (RowNumberListModel) getModel();
            model.setListObject(this);

            mainListListener = new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent e) {
                    mainModelChanged(e);
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    mainModelChanged(e);
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    mainModelChanged(e);
                }
            };
            (RowHeaderList.this.getModel()).addListDataListener(mainListListener);
        }

        void release() {
            (RowHeaderList.this.getModel()).removeListDataListener(mainListListener);
        }

        private void mainModelChanged(ListDataEvent e) {
            RowNumberListModel listModel = (RowNumberListModel) getModel();
            int listRowCount = listModel.getSize();
            int mainRowCount = RowHeaderList.this.getModel().getSize();
            if (mainRowCount == listRowCount) {
                if ( e.getType() == ListDataEvent.CONTENTS_CHANGED ) {
                    listModel.fireContentsChanged(e.getIndex0(), e.getIndex1());
                }
                return;
            }

            listModel.setSize(mainRowCount);
            if (mainRowCount == 0) {
                return;
            }

            Insets insets = getInsets();
            ListCellRenderer cellRender = getCellRenderer();
            int with = (int) cellRender.getListCellRendererComponent(this, null, mainRowCount - 1, false, false)
                    .getPreferredSize().getWidth() + insets.left + insets.right;
            setFixedCellWidth(with);
        }
    }



    class RowNumberListModel extends AbstractListModel {
        private int length;
        private JList list;

        public RowNumberListModel(int length) {
            this.length = length;
        }

        public RowNumberListModel(int length, JList list) {
            this.length = length;
            this.list = list;
        }

        public void fireContentsChanged(int index0, int index1) {
            fireContentsChanged(this, index0, index1);
        }
        public void setListObject(JList list) {
            this.list = list;
        }

        public Object getElementAt(int index) {
            return null;
        }
        public int getSize() {

            return length;
        }

        public void setSize(int length) {
            if (length != this.length) {
                this.length = length;
                list.repaint();
            }
        }
    }

    class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = 1L;

        private RowHeaderList mainList;

        RowHeaderRenderer(RowHeaderList table) {
            super();

            this.mainList = table;

            setOpaque(true);
            setBorder(UIManager.getBorder("List.cellNoFocusBorder"));
            setHorizontalAlignment(LEFT);
            setHorizontalTextPosition(SwingConstants.LEFT);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setForeground(mainList.getForeground());
            setFont(mainList.getFont());

            setText("" + (index + 1));
                Icon icon = mainList.getRowIcon(index);
                setIcon(icon);

                if (icon != null) {
                    int preferredWidth = this.getPreferredSize().width + 2;
                    if (list.getFixedCellWidth() < preferredWidth) {
                        list.setFixedCellWidth(preferredWidth);
                    }
                }
            return this;
        }
    }
}
