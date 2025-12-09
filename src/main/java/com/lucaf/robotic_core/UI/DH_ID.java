package com.lucaf.robotic_core.UI;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lucaf.robotic_core.DH_ROBOTICS.RGI100.RGI100;
import com.lucaf.robotic_core.DataInterfaces.modbus.ModbusRS485Interface;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DH_ID {
    private JComboBox comList;
    private JButton refreshCom;
    private JPanel app;
    private JButton connectBtn;
    private JComboBox selectMotor;
    private JButton refreshMotor;
    private JButton SETButton;
    private JComboBox selectID;
    private JButton disconnectButton;
    private JLabel idCheck;


    JFrame frame = new JFrame("DH ROBOTICS");
    ModbusSerialMaster master = null;

    void updateSerialPorts() {
        comList.setModel(new DefaultComboBoxModel<>());
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            comList.addItem(port.getSystemPortName());
        }
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    void listConnected() {
        executorService.submit(() -> {
            try {
                refreshMotor.setEnabled(false);
                selectMotor.setModel(new DefaultComboBoxModel<>());
                selectID.setModel(new DefaultComboBoxModel<>());
                master.setTimeout(20);
                for (int i = 0; i <= 255; i++) {
                    selectID.addItem(i);
                }
                for (int i = 0; i <= 255; i++) {
                    idCheck.setText(String.valueOf(i));
                    if (ModbusRS485Interface.ping(master, i)) {
                        selectMotor.addItem(i);
                    }
                }
                refreshMotor.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    boolean connect() {
        SerialParameters params = new SerialParameters();
        params.setPortName(comList.getSelectedItem().toString());
        params.setBaudRate(115200);
        params.setDatabits(8);
        params.setParity("None");
        params.setStopbits(1);
        params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        master = new ModbusSerialMaster(
                params
        );
        try {
            master.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void startUpdate() throws IOException {
        byte id = Byte.parseByte(selectMotor.getSelectedItem().toString());
        int newId = Integer.parseInt(selectID.getSelectedItem().toString());
        RGI100 rgi100_22 = new RGI100(new ModbusRS485Interface(master, id, ""), new HashMap<>());
        if (rgi100_22.changeAddress(newId)) {
            if (rgi100_22.saveConfig()) {
                JOptionPane.showMessageDialog(null, "ID cambiato con successo. Riavviare il dispositivo", "Successo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Errore durante la comunicazione", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Errore durante la comunicazione", "Errore", JOptionPane.ERROR_MESSAGE);
        }
        listConnected();
    }

    public DH_ID() {
        refreshCom.addActionListener(e -> {
            updateSerialPorts();
        });
        refreshMotor.addActionListener(e -> {
            listConnected();
        });
        connectBtn.addActionListener(e -> {
            if (!connect()) {
                JOptionPane.showMessageDialog(frame, "Error connecting to the device");
                return;
            }
            selectMotor.setEnabled(true);
            refreshMotor.setEnabled(true);
            selectID.setEnabled(true);
            SETButton.setEnabled(true);
            disconnectButton.setEnabled(true);
            connectBtn.setEnabled(false);
            comList.setEnabled(false);
            listConnected();
        });
        disconnectButton.addActionListener(e -> {
            if (master != null) {
                master.disconnect();
            }
            selectMotor.setEnabled(false);
            refreshMotor.setEnabled(false);
            selectID.setEnabled(false);
            SETButton.setEnabled(false);
            disconnectButton.setEnabled(false);
            connectBtn.setEnabled(true);
            comList.setEnabled(true);
        });

        SETButton.addActionListener(e -> {
            try {
                startUpdate();
            } catch (IOException deviceCommunicationException) {
                JOptionPane.showMessageDialog(frame, "Error updating the device: " + deviceCommunicationException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateSerialPorts();
    }


    public void show() {
        frame.setContentPane(app);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(400, 350);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        DH_ID id = new DH_ID();
        id.show();
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
        app = new JPanel();
        app.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(9, 3, new Insets(10, 10, 10, 10), -1, -1));
        app.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        comList = new JComboBox();
        panel1.add(comList, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, -1), null, 0, false));
        refreshCom = new JButton();
        refreshCom.setText("Refresh");
        panel1.add(refreshCom, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Select COM");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectBtn = new JButton();
        connectBtn.setText("Connect");
        panel1.add(connectBtn, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectMotor = new JComboBox();
        selectMotor.setEnabled(false);
        panel1.add(selectMotor, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Select motor");
        panel1.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshMotor = new JButton();
        refreshMotor.setEnabled(false);
        refreshMotor.setText("Refresh");
        panel1.add(refreshMotor, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        SETButton = new JButton();
        SETButton.setEnabled(false);
        SETButton.setText("SET");
        panel1.add(SETButton, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectID = new JComboBox();
        selectID.setEnabled(false);
        panel1.add(selectID, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setEnabled(true);
        label3.setText("Set new ID");
        panel1.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        disconnectButton = new JButton();
        disconnectButton.setEnabled(false);
        disconnectButton.setText("Disconnect");
        panel1.add(disconnectButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Checking id:");
        panel1.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        idCheck = new JLabel();
        idCheck.setText("XX");
        panel1.add(idCheck, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return app;
    }

}
