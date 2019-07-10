package com.cxplan.projection.ui;

import com.cxplan.projection.script.ScriptRect;
import com.cxplan.projection.script.ViewNode;
import info.clearthought.layout.TableLayout;

import javax.swing.*;

/**
 * @author Kenny
 * created on 2019/4/4
 */
public class ViewPropertyPanel extends JPanel {

    private JTextField resourceId;
    private JTextField text;
    private JTextField contentDesc;
    private JTextField packageName;
    private JTextField className;
    private JTextField checkable;
    private JTextField checked;
    private JTextField clickable;
    private JTextField enabled;
    private JTextField focusable;
    private JTextField focused;
    private JTextField scrollable;
    private JTextField longClickable;
    private JTextField password;
    private JTextField selected;
    private JTextField editable;
    private JTextField bound;

    public ViewPropertyPanel() {
        init();
    }

    private void init() {
        double b=10;
        double p = TableLayout.PREFERRED;
        double vg = 10;

        double[][] size=new double[][]{{b,p,b,330,b},
                {b,
                        p, vg ,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        p, vg,
                        b}};
        setLayout(new TableLayout(size));

        int yIndex = 1;
        add(new JLabel("Resource Id"), "1, " + yIndex);
        resourceId = new JTextField();
        resourceId.setEditable(false);
        add(resourceId, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Text"), "1, " + yIndex);
        text = new JTextField();
        text.setEditable(false);
        add(text, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Content Desc"), "1, " + yIndex);
        contentDesc = new JTextField();
        contentDesc.setEditable(false);
        add(contentDesc, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Package"), "1, " + yIndex);
        packageName = new JTextField();
        packageName.setEditable(false);
        add(packageName, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Class"), "1, " + yIndex);
        className = new JTextField();
        className.setEditable(false);
        add(className, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Checkable"), "1, " + yIndex);
        checkable = new JTextField();
        checkable.setEditable(false);
        add(checkable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Checked"), "1, " + yIndex);
        checked = new JTextField();
        checked.setEditable(false);
        add(checked, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Clickable"), "1, " + yIndex);
        clickable = new JTextField();
        clickable.setEditable(false);
        add(clickable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("LongClickable"), "1, " + yIndex);
        longClickable = new JTextField();
        longClickable.setEditable(false);
        add(longClickable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Enabled"), "1, " + yIndex);
        enabled = new JTextField();
        enabled.setEditable(false);
        add(enabled, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Focusable"), "1, " + yIndex);
        focusable = new JTextField();
        focusable.setEditable(false);
        add(focusable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Focused"), "1, " + yIndex);
        focused = new JTextField();
        focused.setEditable(false);
        add(focused, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Scrollable"), "1, " + yIndex);
        scrollable = new JTextField();
        scrollable.setEditable(false);
        add(scrollable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Password"), "1, " + yIndex);
        password = new JTextField();
        password.setEditable(false);
        add(password, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Selected"), "1, " + yIndex);
        selected = new JTextField();
        selected.setEditable(false);
        add(selected, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Editable"), "1, " + yIndex);
        editable = new JTextField();
        editable.setEditable(false);
        add(editable, "3, " + yIndex);

        yIndex += 2;
        add(new JLabel("Bound"), "1, " + yIndex);
        bound = new JTextField();
        bound.setEditable(false);
        add(bound, "3, " + yIndex);
    }

    public void setViewNode(ViewNode node) {
        resourceId.setText(node.getResourceId());
        text.setText(node.getText());
        contentDesc.setText(node.getContentDesc());
        packageName.setText(node.getPackageName());
        className.setText(node.getClassName());
        checkable.setText(node.isCheckable() ? "true" : "false");
        checked.setText(node.isChecked() ? "true" : "false");
        clickable.setText(node.isCheckable() ? "true" : "false");
        enabled.setText(node.isEnabled() ? "true" : "false");
        focusable.setText(node.isFocusable() ? "true" : "false");
        focused.setText(node.isFocused() ? "true" : "false");
        scrollable.setText(node.isScrollable() ? "true" : "false");
        longClickable.setText(node.isLongClickable() ? "true" : "false");
        password.setText(node.isPassword() ? "true" : "false");
        selected.setText(node.isSelected() ? "true" : "false");
        editable.setText(node.isEditable() ? "true" : "false");

        ScriptRect rect = node.getBound();
        if (rect != null) {
            StringBuilder sb = new StringBuilder(32);
            sb.append("[");
            sb.append(rect.getLeft());
            sb.append(", ");
            sb.append(rect.getTop());
            sb.append("] - [");
            sb.append(rect.getRight());
            sb.append(", ");
            sb.append(rect.getBottom());
            sb.append("]");
            bound.setText(sb.toString());
        }
    }
}
