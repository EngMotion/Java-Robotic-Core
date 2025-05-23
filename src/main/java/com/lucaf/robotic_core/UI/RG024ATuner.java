package com.lucaf.robotic_core.UI;

import com.fazecast.jSerialComm.SerialPort;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.MOXMEC.RG024A.RG024A;
import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import com.lucaf.robotic_core.utils.serials.MoxMecSerialCache;
import jssc.SerialPortException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class RG024ATuner {
    private JPanel panel1;
    private JSlider rampSlider;
    private JSlider amplitudeSlider;
    private JSlider frequencySlider;
    private JButton SAVEButton;
    private JComboBox coms;
    private JButton CONNECTButton;
    private JButton TOGGLEButton;
    private JButton OFFButton;
    private JButton ONButton;
    private JLabel rampLabel;
    private JLabel amplitudeLabel;
    private JLabel frequencyLabel;

    private RG024A rg024A = null;

    public static void main(String[] args) {
        RG024ATuner frame = new RG024ATuner();

        frame.show();
    }

    static Logger logger = new Logger() {
        @Override
        public void log(String message) {
            System.out.println(message);
        }

        @Override
        public void error(String message) {
            System.out.println(message);
        }

        @Override
        public void warn(String message) {
            System.out.println(message);
        }

        @Override
        public void debug(String message) {
            System.out.println(message);
        }
    };

    public void setupData() throws DeviceCommunicationException {
        rampSlider.setMaximum(99);
        rampSlider.setMinimum(0);
        rampSlider.setValue(rg024A.getRamp());
        rampLabel.setText(String.valueOf(rg024A.getRamp()));
        rampSlider.addChangeListener(e -> {
            try {
                rg024A.setRamp(rampSlider.getValue());
                rampLabel.setText(String.valueOf(rg024A.getRamp()));
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        amplitudeSlider.setMaximum(100);
        amplitudeSlider.setMinimum(0);
        amplitudeSlider.setValue(rg024A.getAmplitude());
        amplitudeLabel.setText(String.valueOf(rg024A.getAmplitude()));
        amplitudeSlider.addChangeListener(e -> {
            try {
                rg024A.setAmplitude(amplitudeSlider.getValue());
                amplitudeLabel.setText(String.valueOf(rg024A.getAmplitude()));
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        frequencySlider.setMaximum(4000);
        frequencySlider.setMinimum(600);
        frequencySlider.setValue(rg024A.getFrequency());
        frequencyLabel.setText(String.valueOf(rg024A.getFrequency()));
        frequencySlider.addChangeListener(e -> {
            try {
                rg024A.setFrequency(frequencySlider.getValue());
                frequencyLabel.setText(String.valueOf(rg024A.getFrequency()));
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        SAVEButton.addActionListener(e -> {
            try {
                rg024A.save();
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        ONButton.addActionListener(e -> {
            try {
                rg024A.enable();
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        OFFButton.addActionListener(e -> {
            try {
                rg024A.disable();
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
        TOGGLEButton.addActionListener(e -> {
            try {
                rg024A.toggle();
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
    }

    public RG024ATuner() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            coms.addItem(port.getSystemPortName());
        }
        CONNECTButton.addActionListener(e -> {
            try {
                String portName = coms.getSelectedItem().toString();
                rg024A = new RG024A(MoxMecSerialCache.getSerial(portName, new SerialParams()), logger);
                CONNECTButton.setEnabled(false);
                setupData();
            } catch (SerialPortException ex) {
                ex.printStackTrace();
            } catch (DeviceCommunicationException ex) {
                ex.printStackTrace();
            }
        });
    }


    public void show() {
        JFrame frame = new JFrame("RG024ATuner");
        frame.setContentPane(new RG024ATuner().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(400, 500);
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
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(13, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Ramp");
        panel2.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Amplitude");
        panel2.add(label2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Frequency");
        panel2.add(label3, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rampSlider = new JSlider();
        panel2.add(rampSlider, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        amplitudeSlider = new JSlider();
        panel2.add(amplitudeSlider, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frequencySlider = new JSlider();
        panel2.add(frequencySlider, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SAVEButton = new JButton();
        SAVEButton.setText("SAVE");
        panel2.add(SAVEButton, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 24, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("RG024A Tuner");
        panel2.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        coms = new JComboBox();
        panel3.add(coms, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CONNECTButton = new JButton();
        CONNECTButton.setText("CONNECT");
        panel3.add(CONNECTButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rampLabel = new JLabel();
        rampLabel.setText("xx");
        panel2.add(rampLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        amplitudeLabel = new JLabel();
        amplitudeLabel.setText("xx");
        panel2.add(amplitudeLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frequencyLabel = new JLabel();
        frequencyLabel.setText("xx");
        panel2.add(frequencyLabel, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        TOGGLEButton = new JButton();
        TOGGLEButton.setText("TOGGLE");
        panel4.add(TOGGLEButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OFFButton = new JButton();
        OFFButton.setText("OFF");
        panel4.add(OFFButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ONButton = new JButton();
        ONButton.setText("ON");
        panel4.add(ONButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
