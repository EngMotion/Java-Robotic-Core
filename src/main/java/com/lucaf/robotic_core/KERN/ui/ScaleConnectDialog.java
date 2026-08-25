package com.lucaf.robotic_core.KERN.ui;

import com.fazecast.jSerialComm.SerialPort;
import com.lucaf.robotic_core.SerialParams;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static jssc.SerialPort.BAUDRATE_115200;
import static jssc.SerialPort.BAUDRATE_19200;
import static jssc.SerialPort.BAUDRATE_2400;
import static jssc.SerialPort.BAUDRATE_38400;
import static jssc.SerialPort.BAUDRATE_4800;
import static jssc.SerialPort.BAUDRATE_57600;
import static jssc.SerialPort.BAUDRATE_9600;
import static jssc.SerialPort.DATABITS_8;
import static jssc.SerialPort.PARITY_NONE;
import static jssc.SerialPort.STOPBITS_1;

/**
 * Startup modal of {@link ScaleTesterUI}: asks which KERN scale family is connected and on which
 * COM port, then hands the choice back to the caller.
 * <p>
 * The remaining serial parameters are fixed to the KERN defaults (8 data bits, no parity, 1 stop
 * bit); only the baud rate is exposed, since that is the one setting that actually changes between
 * devices.
 */
public class ScaleConnectDialog extends JDialog {

    /**
     * Baud rates offered in the combo box.
     */
    private static final Integer[] BAUDRATES = {
            BAUDRATE_2400,
            BAUDRATE_4800,
            BAUDRATE_9600,
            BAUDRATE_19200,
            BAUDRATE_38400,
            BAUDRATE_57600,
            BAUDRATE_115200
    };

    /**
     * The choice made by the operator: which scale family, on which port, at which speed.
     *
     * @param scaleType the selected KERN scale family
     * @param params    the serial parameters to open the port with
     */
    public record Selection(KernScaleType scaleType, SerialParams params) {
    }

    /**
     * Scale family selector.
     */
    private final JComboBox<KernScaleType> scaleTypes = new JComboBox<>(KernScaleType.values());

    /**
     * COM port selector, refreshed from the ports currently enumerated by the OS.
     */
    private final JComboBox<String> ports = new JComboBox<>();

    /**
     * Baud rate selector.
     */
    private final JComboBox<Integer> baudrates = new JComboBox<>(BAUDRATES);

    /**
     * Describes the protocol and the device setup required by the selected scale family.
     */
    private final JLabel hint = new JLabel();

    /**
     * The choice confirmed by the operator, or {@code null} if the dialog was cancelled.
     */
    private Selection selection;

    /**
     * Builds the modal dialog. Use {@link #prompt()} instead of instantiating it directly.
     */
    private ScaleConnectDialog() {
        super((Dialog) null, "KERN Scale Tester — Connessione", true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Seleziona bilancia e porta");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 6f));

        JButton refresh = new JButton("Aggiorna");
        refresh.addActionListener(e -> refreshPorts());

        JPanel portRow = new JPanel(new BorderLayout(8, 0));
        portRow.add(ports, BorderLayout.CENTER);
        portRow.add(refresh, BorderLayout.EAST);

        hint.setFont(hint.getFont().deriveFont(Font.PLAIN));

        int row = 0;
        form.add(title, constraints(0, row++, 2, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 16, 0)));
        form.add(new JLabel("Tipo bilancia"), labelConstraints(row));
        form.add(scaleTypes, fieldConstraints(row++));
        form.add(new JLabel("Porta COM"), labelConstraints(row));
        form.add(portRow, fieldConstraints(row++));
        form.add(new JLabel("Baudrate"), labelConstraints(row));
        form.add(baudrates, fieldConstraints(row++));
        form.add(hint, constraints(0, row, 2, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 0, 0)));

        JButton connect = new JButton("Connetti");
        JButton cancel = new JButton("Annulla");
        connect.addActionListener(e -> confirm());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
        buttons.add(cancel, BorderLayout.WEST);
        buttons.add(Box.createHorizontalStrut(8), BorderLayout.CENTER);
        buttons.add(connect, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(connect);
        registerEscapeToCancel();

        baudrates.setSelectedItem(BAUDRATE_9600);
        scaleTypes.addActionListener(e -> updateHint());
        updateHint();
        refreshPorts();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    /**
     * Shows the modal dialog and blocks until the operator confirms or cancels.
     *
     * @return the confirmed selection, or {@code null} if the dialog was cancelled or closed
     */
    public static Selection prompt() {
        ScaleConnectDialog dialog = new ScaleConnectDialog();
        dialog.setVisible(true);
        return dialog.selection;
    }

    /**
     * Re-enumerates the serial ports exposed by the OS, keeping the current choice when it is still
     * present so that an accidental refresh does not reset the selection.
     */
    private void refreshPorts() {
        String previous = (String) ports.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            model.addElement(port.getSystemPortName());
        }
        ports.setModel(model);
        if (previous != null && model.getIndexOf(previous) >= 0) {
            ports.setSelectedItem(previous);
        }
    }

    /**
     * Refreshes the hint shown under the form with the details of the selected scale family.
     */
    private void updateHint() {
        KernScaleType type = (KernScaleType) scaleTypes.getSelectedItem();
        if (type == null) {
            hint.setText(" ");
            return;
        }
        hint.setText("<html><div style='width:340px'>" + type.getProtocolSummary()
                + "<br><br><b>Nota:</b> " + type.getSetupHint() + "</div></html>");
        pack();
    }

    /**
     * Validates the form and stores the {@link Selection} before closing the dialog.
     */
    private void confirm() {
        String port = (String) ports.getSelectedItem();
        if (port == null || port.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Nessuna porta seriale disponibile. Collega la bilancia e premi Aggiorna.",
                    "Porta mancante", JOptionPane.WARNING_MESSAGE);
            return;
        }
        KernScaleType type = (KernScaleType) scaleTypes.getSelectedItem();
        Integer baudrate = (Integer) baudrates.getSelectedItem();
        SerialParams params = new SerialParams(
                port, 1, baudrate == null ? BAUDRATE_9600 : baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);
        selection = new Selection(type, params);
        dispose();
    }

    /**
     * Makes the ESC key close the dialog like the Cancel button.
     */
    private void registerEscapeToCancel() {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        root.getActionMap().put("cancel", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /**
     * Builds the constraints for a form label in the left column.
     *
     * @param row the grid row
     * @return the constraints
     */
    private static GridBagConstraints labelConstraints(int row) {
        GridBagConstraints c = constraints(0, row, 1, GridBagConstraints.NONE, new Insets(4, 0, 4, 12));
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0;
        return c;
    }

    /**
     * Builds the constraints for a form field in the right column.
     *
     * @param row the grid row
     * @return the constraints
     */
    private static GridBagConstraints fieldConstraints(int row) {
        return constraints(1, row, 1, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 0));
    }

    /**
     * Builds generic grid-bag constraints.
     *
     * @param x      the grid column
     * @param y      the grid row
     * @param width  how many columns the component spans
     * @param fill   the fill policy
     * @param insets the outer margins
     * @return the constraints
     */
    private static GridBagConstraints constraints(int x, int y, int width, int fill, Insets insets) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.fill = fill;
        c.insets = insets;
        c.weightx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        return c;
    }
}
