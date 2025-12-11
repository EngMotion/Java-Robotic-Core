package com.lucaf.robotic_core.stepperOnline.iDmRs.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lucaf.robotic_core.UI.UISlider;
import com.lucaf.robotic_core.stepperOnline.iDmRs.IDMRS;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;

public class IDMRSUI {
    private JPanel mainWindow;
    private JPanel propsPanel;
    private JButton stop;
    private JButton start;
    private JComboBox mode;
    private JSpinner position;
    private JButton go;

    final UISlider speed;
    final UISlider acceleration;
    final UISlider deceleration;

    final String[] startModes = {
            "Absolute Positioning",
            "Relative Positioning",
            "Velocity Mode"
    };

    public IDMRSUI(IDMRS motor) throws IOException {
        propsPanel.setLayout(new GridLayoutManager(100, 1, new Insets(0, 0, 0, 0), -1, -1));
        int i = 0;

        speed = new UISlider("Speed", 0, 10000, motor.getSpeed());
        speed.setOnValueChange(value -> {
            try {
                motor.setSpeed(value);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error setting max speed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        propsPanel.add(speed.getSliderTemplate(), new GridConstraints(i++, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        acceleration = new UISlider("Acceleration", 0, 10000, motor.getAcceleration());
        acceleration.setOnValueChange(value -> {
            try {
                motor.setAcceleration(value);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error setting acceleration", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        propsPanel.add(acceleration.getSliderTemplate(), new GridConstraints(i++, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        deceleration = new UISlider("Deceleration", 0, 10000, motor.getDeceleration());
        deceleration.setOnValueChange(value -> {
            try {
                motor.setDeceleration(value);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error setting deceleration", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        propsPanel.add(deceleration.getSliderTemplate(), new GridConstraints(i++, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        for (String m : startModes) {
            mode.addItem(m);
        }

        start.addActionListener(e -> {
            try {
                int selectedMode = mode.getSelectedIndex();
                switch (selectedMode) {
                    case 0 -> {
                        motor.setPositioningMode();
                        motor.setRelativePositioning(false);
                    }
                    case 1 -> {
                        motor.setPositioningMode();
                        motor.setRelativePositioning(true);
                    }
                    case 2 -> {
                        motor.setVelocityMode();
                    }
                }
                motor.start();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error executing move command", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stop.addActionListener(e -> {
            try {
                motor.stop();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error executing stop command", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        go.addActionListener(e -> {
            try {
                int pos = (Integer) position.getValue();
                motor.setPosition(pos);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error setting target position", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void createWindow() {
        JFrame frame = new JFrame("IDM-RS Tuner");
        frame.setContentPane(mainWindow);
        frame.pack();
        frame.setSize(600, 750);
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
        mainWindow = new JPanel();
        mainWindow.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        mainWindow.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 28, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("IDM-RS Tuner");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane1.setViewportView(panel2);
        propsPanel = new JPanel();
        propsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(propsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        return mainWindow;
    }

}
