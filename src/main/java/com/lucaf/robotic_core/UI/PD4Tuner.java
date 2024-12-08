package com.lucaf.robotic_core.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.PD4E;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.UnitControl;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Function;

public class PD4Tuner {
    private JPanel panel1;
    private JPanel propsPanel;
    private JComboBox unitSelector;
    private JSpinner unitScale;
    private JButton SAVEButton;
    private int row = 0;

    public void createPanelTemplate(String labelName, int min, int max, int current, Function<Integer, Void> onValueChange, JPanel parentPanel) {
        final JPanel panelTemplate = new JPanel();
        panelTemplate.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        parentPanel.add(panelTemplate, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText(labelName);
        panelTemplate.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        JSlider slider1 = new JSlider();
        slider1.setMinimum(min);
        slider1.setMaximum(max);
        slider1.setValue(current);
        panelTemplate.add(slider1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        JSpinner spinner1 = new JSpinner();
        spinner1.setValue(current);
        spinner1.addChangeListener(e -> {
            slider1.setValue((Integer) spinner1.getValue());
            onValueChange.apply((Integer) spinner1.getValue());
        });
        slider1.addChangeListener(e -> {
            spinner1.setValue(slider1.getValue());
        });
        panelTemplate.add(spinner1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), null, null, 0, false));
        row++;
    }

    public static String[] getUnits() {
        Field[] fields = UnitControl.Units.class.getFields();
        String[] units = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            units[i] = fields[i].getName();
        }
        return units;
    }

    public static String unitToString(int unit) {
        String[] units = getUnits();
        for (int i = 0; i < units.length; i++) {
            try {
                Field field = UnitControl.Units.class.getField(units[i]);
                if (field.getByte(null) == unit) {
                    return units[i];
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {

            }
        }
        return "ENCODER";
    }

    public static byte getUnit(String unit) {
        try {
            Class<UnitControl.Units> unitControlClass = UnitControl.Units.class;
            Field field = unitControlClass.getField(unit);
            return field.getByte(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return UnitControl.Units.ENCODER;
        }
    }

    public PD4Tuner(PD4E pd4e) throws DeviceCommunicationException {
        propsPanel.setLayout(new GridLayoutManager(100, 1, new Insets(0, 0, 0, 0), -1, -1));
        createPanelTemplate("Max Speed", 0, 1000, pd4e.getMaxSpeed(), value -> {
            try {
                pd4e.setMaxSpeed(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting max speed", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Ramp Speed", 0, 1000, pd4e.getTravelSpeed(), value -> {
            try {
                pd4e.setTravelSpeed(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting max speed", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Acceleration", 0, 1000, pd4e.getAcceleration(), value -> {
            try {
                pd4e.setAcceleration(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting acceleration", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Deceleration", 0, 1000, pd4e.getDeceleration(), value -> {
            try {
                pd4e.setDeceleration(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting deceleration", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Max Current", 0, 5000, pd4e.getMaxCurrent(), value -> {
            try {
                pd4e.setMaxCurrent(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting max current", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Rated Current", 0, 5000, pd4e.getTargetCurrent(), value -> {
            try {
                pd4e.setTargetCurrent(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting rated current", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Position Integral Gain", 0, 1000, pd4e.getClosedLoopPositionIntegralGain(), value -> {
            try {
                pd4e.setClosedLoopPositionIntegralGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop position integral gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Position Proportional Gain", 0, 50000, pd4e.getClosedLoopPositionProportionalGain(), value -> {
            try {
                pd4e.setClosedLoopPositionProportionalGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop position proportional gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Velocity Integral Gain", 0, 1000, pd4e.getClosedLoopVelocityIntegralGain(), value -> {
            try {
                pd4e.setClosedLoopVelocityIntegralGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop velocity integral gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Velocity Proportional Gain", 0, 50000, pd4e.getClosedLoopVelocityProportionalGain(), value -> {
            try {
                pd4e.setClosedLoopVelocityProportionalGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop velocity proportional gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Flux Integral Gain", 0, 5000000, pd4e.getClosedLoopFluxIntegralGain(), value -> {
            try {
                pd4e.setClosedLoopFluxIntegralGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop flux integral gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Flux Proportional Gain", 0, 5000000, pd4e.getClosedLoopFluxProportionalGain(), value -> {
            try {
                pd4e.setClosedLoopFluxProportionalGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop flux proportional gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Torque Integral Gain", 0, 5000000, pd4e.getClosedLoopTorqueIntegralGain(), value -> {
            try {
                pd4e.setClosedLoopTorqueIntegralGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop torque integral gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Closed Loop Torque Proportional Gain", 0, 5000000, pd4e.getClosedLoopTorqueProportionalGain(), value -> {
            try {
                pd4e.setClosedLoopTorqueProportionalGain(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting closed loop torque proportional gain", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Home offset", 0, 10000, pd4e.getHomeOffset(), value -> {
            try {
                pd4e.setHomeOffset(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting home offset", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Position Window", 0, 10000, pd4e.getPositionWindow(), value -> {
            try {
                pd4e.setPositionWindow(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting position window", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        createPanelTemplate("Position Window Time", 0, 10000, pd4e.getPositionWindowTime(), value -> {
            try {
                pd4e.setPositionWindowTime(value);
            } catch (DeviceCommunicationException e) {
                JOptionPane.showMessageDialog(null, "Error setting position window time", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }, propsPanel);
        UnitControl unitControl = pd4e.getUnitPosition();
        unitScale.setValue(unitControl.getFactor());
        unitScale.addChangeListener(e -> {
            try {
                pd4e.setUnitPositionFactor((Integer) unitScale.getValue());
            } catch (DeviceCommunicationException deviceCommunicationException) {
                JOptionPane.showMessageDialog(null, "Error setting unit position factor", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        unitSelector.setSelectedItem(unitToString(unitControl.getUnit()));
        unitSelector.addItemListener(e -> {
            try {
                pd4e.setUnitPositionUnit(getUnit((String) unitSelector.getSelectedItem()));
            } catch (DeviceCommunicationException deviceCommunicationException) {
                JOptionPane.showMessageDialog(null, "Error setting unit position unit", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        SAVEButton.addActionListener(e -> {
            try {
                pd4e.storeAllParams();
                JOptionPane.showMessageDialog(null, "Parameters saved", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DeviceCommunicationException deviceCommunicationException) {
                JOptionPane.showMessageDialog(null, "Error saving parameters", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        unitSelector.setModel(new DefaultComboBoxModel<>(getUnits()));
    }

    public void show() {
        JFrame frame = new JFrame("PD4Tuner");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setSize(600, 650);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 28, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("PD4 Tuner");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        scrollPane1.setViewportView(panel3);
        propsPanel = new JPanel();
        propsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(propsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        unitSelector = new JComboBox();
        panel4.add(unitSelector, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unitScale = new JSpinner();
        panel4.add(unitScale, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Unit");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Scale");
        panel4.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        SAVEButton = new JButton();
        SAVEButton.setText("SAVE");
        panel2.add(SAVEButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
