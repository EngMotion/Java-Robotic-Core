package com.lucaf.robotic_core.KERN.ui;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.UI.ThemeSetup;
import com.lucaf.robotic_core.dataInterfaces.serial.SerialPortCache;
import com.lucaf.robotic_core.impl.ScaleInterface;
import jssc.SerialPort;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Swing harness to exercise a KERN scale (PCB or PLJ) over a real serial connection.
 * <p>
 * On startup {@link ScaleConnectDialog} asks which scale family is connected and on which COM port;
 * the main window is then split in two columns: on the left a real-time monitor of the raw serial
 * traffic (every byte sent and received, plus the driver's own log messages), on the right the
 * buttons that drive the {@link ScaleInterface} API — read, stable read, tare, streaming — and a
 * field to push arbitrary commands to the device.
 * <p>
 * Every device command runs on a dedicated single-thread executor: the driver calls block (a PCB
 * read waits up to one second, a tare up to ten) and must never run on the Event Dispatch Thread.
 */
public class ScaleTesterUI {

    /**
     * Maximum number of lines kept in the monitor before the oldest ones are dropped. A streaming
     * PLJ produces several lines per second, so the document has to be bounded.
     */
    private static final int MAX_LOG_LINES = 2000;

    /**
     * Timestamp format used to prefix every monitor line.
     */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * Severity/direction of a monitor line, with the colour used to render it. The colours are
     * mid-tones so they stay legible on both the light and the dark FlatLaf theme.
     */
    private enum LogLevel {
        /**
         * Bytes written to the device.
         */
        TX("TX", new Color(0x2F80ED)),
        /**
         * Bytes received from the device.
         */
        RX("RX", new Color(0x1E9E52)),
        /**
         * A command issued from the UI.
         */
        CMD("CMD", new Color(0x9B51E0)),
        /**
         * Informational message.
         */
        INFO("INFO", new Color(0x808080)),
        /**
         * Warning reported by the driver.
         */
        WARN("WARN", new Color(0xD98200)),
        /**
         * Error reported by the driver or raised by a command.
         */
        ERROR("ERR", new Color(0xD64541));

        /**
         * Fixed-width tag shown in the monitor.
         */
        private final String tag;

        /**
         * Colour used to render the line.
         */
        private final Color color;

        LogLevel(String tag, Color color) {
            this.tag = tag;
            this.color = color;
        }
    }

    /**
     * The scale family being tested.
     */
    private final KernScaleType scaleType;

    /**
     * The serial parameters the port was opened with.
     */
    private final SerialParams params;

    /**
     * Serializes the blocking device commands off the Event Dispatch Thread.
     */
    private final ExecutorService commands = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "scale-commands");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Number of readings pushed through the reading-listener API since startup.
     */
    private final AtomicLong streamedReadings = new AtomicLong();

    /**
     * Guards the automatic polling loop so a slow reply cannot pile up a queue of reads.
     */
    private final AtomicBoolean pollInFlight = new AtomicBoolean();

    private final JFrame frame = new JFrame("KERN Scale Tester");
    private final JTextPane monitor = new JTextPane();
    private final JCheckBox autoscroll = new JCheckBox("Autoscroll", true);
    private final JCheckBox showHex = new JCheckBox("Hex", false);
    private final JCheckBox pauseMonitor = new JCheckBox("Pausa", false);
    private final JLabel weightLabel = new JLabel("—");
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel footerLabel = new JLabel(" ");
    private final JTextField rawCommand = new JTextField();
    private final JComboBox<String> lineEnding = new JComboBox<>(new String[]{"nessuno", "CR", "LF", "CR+LF"});
    private final JCheckBox autoPoll = new JCheckBox("Polling automatico", false);
    private final JSpinner pollInterval = new JSpinner(new SpinnerNumberModel(500, 50, 10000, 50));

    /**
     * Repeatedly issues a read while {@link #autoPoll} is selected.
     */
    private final Timer pollTimer;

    /**
     * Refreshes the weight readout and the status line from the driver state.
     */
    private final Timer refreshTimer;

    /**
     * The monitoring serial connector; also used for the purge command.
     */
    private MonitoringSerialConnector connector;

    /**
     * The scale driver under test.
     */
    private ScaleInterface scale;

    /**
     * Builds the window and opens the serial connection.
     *
     * @param selection the scale family and serial parameters chosen at startup
     * @throws Exception if the serial port cannot be opened
     */
    public ScaleTesterUI(ScaleConnectDialog.Selection selection) throws Exception {
        this.scaleType = selection.scaleType();
        this.params = selection.params();

        buildUi();

        this.pollTimer = new Timer((Integer) pollInterval.getValue(), e -> poll());
        this.refreshTimer = new Timer(150, e -> refreshReadout());

        connect();
    }

    /**
     * Entry point: sets up the theme, asks for the connection details and opens the tester. On a
     * failed connection the startup dialog is shown again instead of exiting.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        ThemeSetup.setupTheme();
        SwingUtilities.invokeLater(() -> {
            while (true) {
                ScaleConnectDialog.Selection selection = ScaleConnectDialog.prompt();
                if (selection == null) {
                    System.exit(0);
                }
                try {
                    new ScaleTesterUI(selection).showWindow();
                    return;
                } catch (Exception e) {
                    // The port may have been opened and cached before the failure: drop it so the
                    // next attempt starts from a clean state.
                    closeSerialPorts();
                    JOptionPane.showMessageDialog(null,
                            "Connessione fallita su " + selection.params().getComPort() + ":\n"
                                    + e.getMessage(),
                            "Errore di connessione", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Makes the window visible and starts the periodic refresh.
     */
    public void showWindow() {
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        refreshTimer.start();
    }

    // ------------------------------------------------------------------ connection

    /**
     * Opens the serial port, wraps it in a {@link MonitoringSerialConnector} so the traffic reaches
     * the monitor, builds the driver and initializes it.
     *
     * @throws Exception if the serial port cannot be opened
     */
    private void connect() throws Exception {
        SerialPort port = SerialPortCache.getSerialPort(params.getComPort(), params);
        connector = new MonitoringSerialConnector(port, scaleType.name(), new MonitorLogger(), this::onTraffic);
        scale = scaleType.create(connector, weight -> streamedReadings.incrementAndGet());

        log(LogLevel.INFO, "Connesso a " + params.getComPort() + " @ " + params.getBaudrate()
                + " " + params.getDatabits() + "N" + params.getStopbits() + " — " + scaleType);
        log(LogLevel.INFO, scaleType.getSetupHint());
        runCommand("initialize()", () -> "connesso=" + scale.initialize().get());
    }

    /**
     * Stops the timers, shuts the driver and the port down and terminates the JVM.
     */
    private void shutdownAll() {
        pollTimer.stop();
        refreshTimer.stop();
        commands.shutdownNow();
        try {
            if (scale != null) {
                scale.shutdown();
            }
        } catch (Exception e) {
            System.err.println("Failed to shut the scale down: " + e.getMessage());
        }
        if (connector != null) {
            connector.shutdown();
        }
        closeSerialPorts();
        System.exit(0);
    }

    /**
     * Closes every cached serial port, swallowing the failures: this runs on the way out and there
     * is nothing useful left to do if a close fails.
     */
    private static void closeSerialPorts() {
        try {
            SerialPortCache.closeAll();
        } catch (Exception e) {
            System.err.println("Failed to close the serial ports: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ commands

    /**
     * Runs a device command off the Event Dispatch Thread and logs both the call and its outcome.
     *
     * @param label  how the command is shown in the monitor
     * @param action the call to perform; its result is logged when not {@code null}
     */
    private void runCommand(String label, Callable<String> action) {
        log(LogLevel.CMD, label);
        commands.submit(() -> {
            try {
                String result = action.call();
                if (result != null) {
                    log(LogLevel.CMD, label + " → " + result);
                }
            } catch (Exception e) {
                log(LogLevel.ERROR, label + " ✗ " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Formats a weight returned by the driver, which uses {@code -1} to signal "no reading yet".
     *
     * @param weight the value returned by the driver
     * @return the formatted result
     */
    private String formatReadResult(double weight) {
        if (weight == -1) {
            return "-1 (nessuna lettura disponibile)";
        }
        return weight + " " + scale.getUnit();
    }

    /**
     * Sends the content of the raw command field, appending the selected line ending.
     */
    private void sendRawCommand() {
        String text = rawCommand.getText();
        if (text.isEmpty()) {
            return;
        }
        String payload = text + switch ((String) lineEnding.getSelectedItem()) {
            case "CR" -> "\r";
            case "LF" -> "\n";
            case "CR+LF" -> "\r\n";
            default -> "";
        };
        runCommand("send(" + describe(payload.getBytes(), false) + ")",
                () -> "inviato=" + connector.send(payload.getBytes()));
        rawCommand.selectAll();
    }

    /**
     * Issues one automatic read, skipping the tick when the previous one has not answered yet.
     */
    private void poll() {
        if (!pollInFlight.compareAndSet(false, true)) {
            return;
        }
        commands.submit(() -> {
            try {
                double weight = scale.read();
                log(LogLevel.CMD, "poll read() → " + formatReadResult(weight));
            } catch (Exception e) {
                log(LogLevel.ERROR, "poll read() ✗ " + e.getMessage());
            } finally {
                pollInFlight.set(false);
            }
        });
    }

    /**
     * Starts or stops the automatic polling loop according to the checkbox state.
     */
    private void toggleAutoPoll() {
        if (autoPoll.isSelected()) {
            pollTimer.setDelay((Integer) pollInterval.getValue());
            pollTimer.start();
            log(LogLevel.INFO, "Polling automatico attivo ogni " + pollInterval.getValue() + " ms");
        } else {
            pollTimer.stop();
            log(LogLevel.INFO, "Polling automatico fermato");
        }
    }

    // ------------------------------------------------------------------ monitor

    /**
     * Called from the serial thread for every chunk of traffic; renders it in the monitor.
     *
     * @param direction whether the bytes were sent or received
     * @param data      the raw bytes
     */
    private void onTraffic(MonitoringSerialConnector.Direction direction, byte[] data) {
        LogLevel level = direction == MonitoringSerialConnector.Direction.TX ? LogLevel.TX : LogLevel.RX;
        SwingUtilities.invokeLater(() -> append(level, describe(data, showHex.isSelected())));
    }

    /**
     * Appends a line to the monitor from any thread.
     *
     * @param level   the severity/direction of the line
     * @param message the text to append
     */
    private void log(LogLevel level, String message) {
        SwingUtilities.invokeLater(() -> append(level, message));
    }

    /**
     * Appends a line to the monitor. Must run on the Event Dispatch Thread.
     *
     * @param level   the severity/direction of the line
     * @param message the text to append
     */
    private void append(LogLevel level, String message) {
        if (pauseMonitor.isSelected() && (level == LogLevel.TX || level == LogLevel.RX)) {
            return;
        }
        StyledDocument doc = monitor.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, level.color);
        try {
            doc.insertString(doc.getLength(),
                    LocalTime.now().format(TIME_FORMAT) + "  " + pad(level.tag) + "  " + message + "\n",
                    style);
            trim(doc);
        } catch (BadLocationException e) {
            // The insert position is always the document end, so this cannot happen.
            throw new IllegalStateException(e);
        }
        if (autoscroll.isSelected()) {
            monitor.setCaretPosition(doc.getLength());
        }
    }

    /**
     * Drops the oldest lines once the document grows past {@link #MAX_LOG_LINES}.
     *
     * @param doc the monitor document
     * @throws BadLocationException if the computed range is invalid
     */
    private static void trim(StyledDocument doc) throws BadLocationException {
        Element root = doc.getDefaultRootElement();
        while (root.getElementCount() > MAX_LOG_LINES) {
            Element first = root.getElement(0);
            doc.remove(first.getStartOffset(), first.getEndOffset() - first.getStartOffset());
        }
    }

    /**
     * Right-pads a level tag to a fixed width so the monitor columns line up.
     *
     * @param tag the tag to pad
     * @return the padded tag
     */
    private static String pad(String tag) {
        return (tag + "    ").substring(0, 5);
    }

    /**
     * Renders raw bytes as a quoted string with the non-printable characters escaped, optionally
     * followed by the hex dump.
     *
     * @param data the bytes to render
     * @param hex  whether to append the hex dump
     * @return the printable representation
     */
    private static String describe(byte[] data, boolean hex) {
        StringBuilder text = new StringBuilder();
        StringBuilder dump = new StringBuilder();
        for (byte b : data) {
            int value = b & 0xFF;
            switch (value) {
                case '\r' -> text.append("\\r");
                case '\n' -> text.append("\\n");
                case '\t' -> text.append("\\t");
                default -> {
                    if (value >= 0x20 && value < 0x7F) {
                        text.append((char) value);
                    } else {
                        text.append(String.format("\\x%02X", value));
                    }
                }
            }
            dump.append(String.format("%02X ", value));
        }
        String result = "\"" + text + "\"";
        if (hex) {
            result += "   [" + dump.toString().trim() + "]";
        }
        return result;
    }

    /**
     * Refreshes the weight readout, the status line and the footer from the current driver state.
     * Runs on the Event Dispatch Thread, driven by {@link #refreshTimer}.
     */
    private void refreshReadout() {
        Double last = scale.getLastReading();
        weightLabel.setText(last == null ? "—" : last + " " + scale.getUnit());
        statusLabel.setText("<html>"
                + flag("connesso", scale.isConnected())
                + " &nbsp; " + flag("inizializzato", scale.isInitialized())
                + "<br>" + flag("stream", scale.isEventReadingEnabled())
                + " &nbsp; " + flag("stabile", scale.isStable())
                + "</html>");
        footerLabel.setText(params.getComPort() + " @ " + params.getBaudrate() + " baud"
                + "  •  " + scaleType
                + "  •  letture in streaming: " + streamedReadings.get());
    }

    /**
     * Renders a boolean driver flag as a coloured bullet plus its name.
     *
     * @param name  the flag name
     * @param value the flag value
     * @return the HTML fragment
     */
    private static String flag(String name, boolean value) {
        return "<font color='" + (value ? "#1E9E52" : "#D64541") + "'>●</font> " + name;
    }

    // ------------------------------------------------------------------ layout

    /**
     * Builds the two-column window: serial monitor on the left, commands on the right.
     */
    private void buildUi() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMonitorPanel(), buildCommandPanel());
        split.setResizeWeight(0.6);
        split.setBorder(null);

        footerLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        frame.setLayout(new BorderLayout());
        frame.add(split, BorderLayout.CENTER);
        frame.add(footerLabel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownAll();
            }
        });
        frame.setSize(1100, 640);
        frame.setMinimumSize(new Dimension(820, 480));
    }

    /**
     * Builds the left column: the real-time serial monitor and its display options.
     *
     * @return the monitor panel
     */
    private JPanel buildMonitorPanel() {
        monitor.setEditable(false);
        monitor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        monitor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JButton clear = new JButton("Pulisci");
        clear.addActionListener(e -> monitor.setText(""));

        JPanel options = new JPanel(new BorderLayout());
        JPanel toggles = new JPanel();
        toggles.add(autoscroll);
        toggles.add(showHex);
        toggles.add(pauseMonitor);
        options.add(toggles, BorderLayout.WEST);
        options.add(clear, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(header("Monitor seriale", "Ogni byte scambiato con la bilancia, in tempo reale"),
                BorderLayout.NORTH);
        panel.add(new JScrollPane(monitor), BorderLayout.CENTER);
        panel.add(options, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(620, 0));
        return panel;
    }

    /**
     * Builds the right column: the weight readout and the command buttons.
     *
     * @return the command panel
     */
    private JPanel buildCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(header("Comandi", "Chiamate dirette all'API ScaleInterface"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BorderLayout(0, 12));
        body.add(buildReadout(), BorderLayout.NORTH);
        body.add(buildButtons(), BorderLayout.CENTER);
        body.add(buildRawCommandRow(), BorderLayout.SOUTH);

        panel.add(body, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(400, 0));
        return panel;
    }

    /**
     * Builds the weight readout card sitting on top of the command column.
     *
     * @return the readout panel
     */
    private JPanel buildReadout() {
        weightLabel.setFont(weightLabel.getFont().deriveFont(Font.BOLD, 34f));
        weightLabel.setHorizontalAlignment(JLabel.CENTER);

        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel readout = new JPanel(new BorderLayout(0, 6));
        readout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x808080, false), 1, true),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));
        readout.add(weightLabel, BorderLayout.CENTER);
        readout.add(statusLabel, BorderLayout.SOUTH);
        return readout;
    }

    /**
     * Builds the grid of command buttons plus the automatic polling controls.
     *
     * @return the buttons panel
     */
    private JPanel buildButtons() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.add(button("Leggi", () -> runCommand("read()", () -> formatReadResult(scale.read()))));
        grid.add(button("Leggi stabile",
                () -> runCommand("readStable()", () -> formatReadResult(scale.readStable()))));
        grid.add(button("Tara", () -> runCommand("tare()", () -> String.valueOf(scale.tare().get()))));
        grid.add(button("Re-inizializza",
                () -> runCommand("initialize()", () -> "connesso=" + scale.initialize().get())));
        grid.add(button("Abilita stream", () -> runCommand("enableEventReading()", () -> {
            scale.enableEventReading();
            return "ok";
        })));
        grid.add(button("Disabilita stream", () -> runCommand("disableEventReading()", () -> {
            scale.disableEventReading();
            return "ok";
        })));
        grid.add(button("Svuota buffer", () -> runCommand("purge()", () -> {
            connector.purge();
            return "ok";
        })));
        grid.add(button("Stato", () -> log(LogLevel.INFO,
                "connesso=" + scale.isConnected()
                        + " inizializzato=" + scale.isInitialized()
                        + " stream=" + scale.isEventReadingEnabled()
                        + " stabile=" + scale.isStable()
                        + " ultimaLettura=" + scale.getLastReading()
                        + " unità=" + scale.getUnit())));

        autoPoll.addActionListener(e -> toggleAutoPoll());
        pollInterval.addChangeListener(e -> pollTimer.setDelay((Integer) pollInterval.getValue()));

        JPanel polling = new JPanel(new BorderLayout(8, 0));
        polling.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        polling.add(autoPoll, BorderLayout.WEST);
        polling.add(pollInterval, BorderLayout.CENTER);
        polling.add(new JLabel("ms"), BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(grid, BorderLayout.NORTH);
        panel.add(polling, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the row that sends an arbitrary command to the device.
     *
     * @return the raw command panel
     */
    private JPanel buildRawCommandRow() {
        JButton send = new JButton("Invia");
        send.addActionListener(e -> sendRawCommand());
        rawCommand.addActionListener(e -> sendRawCommand());
        rawCommand.setToolTipText("Comando ASCII da inviare così com'è, es. w / s / t / T");

        JPanel input = new JPanel(new BorderLayout(8, 0));
        input.add(rawCommand, BorderLayout.CENTER);
        input.add(lineEnding, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.add(header("Comando diretto", scaleType.getProtocolSummary()), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(send, BorderLayout.EAST);
        return panel;
    }

    /**
     * Builds a titled header with a smaller, dimmed subtitle underneath.
     *
     * @param title    the header text
     * @param subtitle the subtitle text
     * @return the header panel
     */
    private static JPanel header(String title, String subtitle) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize2D() + 3f));

        JLabel subtitleLabel = new JLabel("<html><div style='width:360px'>" + subtitle + "</div></html>");
        subtitleLabel.setForeground(new Color(0x808080, false));

        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds a command button wired to the given action.
     *
     * @param text   the button label
     * @param action what to run when the button is pressed
     * @return the button
     */
    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * Bridges the library {@link Logger} into the serial monitor, so the driver's own warnings and
     * errors (read timeouts, tare timeouts, disconnections) show up next to the raw traffic.
     */
    private final class MonitorLogger implements Logger {

        @Override
        public void log(String message) {
            ScaleTesterUI.this.log(LogLevel.INFO, message);
        }

        @Override
        public void error(String message) {
            ScaleTesterUI.this.log(LogLevel.ERROR, message);
        }

        @Override
        public void warn(String message) {
            ScaleTesterUI.this.log(LogLevel.WARN, message);
        }

        @Override
        public void debug(String message) {
            ScaleTesterUI.this.log(LogLevel.INFO, message);
        }
    }
}
