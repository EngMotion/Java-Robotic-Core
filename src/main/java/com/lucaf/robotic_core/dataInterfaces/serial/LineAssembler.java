package com.lucaf.robotic_core.dataInterfaces.serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reassembles the CR/LF terminated lines of a serial device out of the arbitrary chunks in which the
 * operating system delivers them.
 * <p>
 * A serial port hands over whatever bytes happen to be in its buffer when it fires an event, which
 * has nothing to do with where the device put its terminators: a single reply such as
 * {@code "      11.203 g  \r\n"} regularly arrives as {@code "      11."} followed by
 * {@code "203 g  \r\n"}. A driver that decodes each chunk on its own would read {@code 11} instead of
 * {@code 11.203}. This class buffers the incoming characters and only hands back a line once its
 * terminator has actually been received.
 * <p>
 * Two safety valves keep a misbehaving device from poisoning the stream:
 * <ul>
 *     <li>a partial line that has been sitting in the buffer for longer than the fragment timeout is
 *     dropped, so that a truncated reply is not glued to the front of the next one;</li>
 *     <li>the buffer is emptied if it grows past its maximum length, so that a device that never
 *     sends a terminator cannot consume memory without bound.</li>
 * </ul>
 * Instances are thread safe: the serial event thread and the thread waiting for a reply may both
 * touch the assembler.
 */
public class LineAssembler {

    /**
     * Default idle time after which a partial line is considered abandoned. Even at the slowest
     * supported baud rate a line takes a handful of milliseconds to arrive, so a gap this long means
     * the rest of it is never coming.
     */
    public static final long DEFAULT_FRAGMENT_TIMEOUT_MS = 250;

    /**
     * Default upper bound on the unterminated characters kept in the buffer.
     */
    public static final int DEFAULT_MAX_LENGTH = 256;

    /**
     * Characters not yet forming a complete line. Guarded by its own monitor.
     */
    private final StringBuilder buffer = new StringBuilder();

    /**
     * Idle time after which a partial line is dropped.
     */
    private final long fragmentTimeoutMs;

    /**
     * Upper bound on the unterminated characters kept in {@link #buffer}.
     */
    private final int maxLength;

    /**
     * Notified with a human readable reason whenever buffered characters are thrown away, so the
     * driver can log it through its own logger. May be {@code null}.
     */
    private final Consumer<String> discardListener;

    /**
     * Timestamp of the last chunk appended, used to detect abandoned lines.
     */
    private long lastChunkAt = 0;

    /**
     * Constructs an assembler with the default timeout and size limit.
     *
     * @param discardListener notified when buffered characters are discarded (may be {@code null})
     */
    public LineAssembler(Consumer<String> discardListener) {
        this(DEFAULT_FRAGMENT_TIMEOUT_MS, DEFAULT_MAX_LENGTH, discardListener);
    }

    /**
     * @param fragmentTimeoutMs idle time after which a partial line is dropped
     * @param maxLength         upper bound on the buffered unterminated characters
     * @param discardListener   notified when buffered characters are discarded (may be {@code null})
     */
    public LineAssembler(long fragmentTimeoutMs, int maxLength, Consumer<String> discardListener) {
        this.fragmentTimeoutMs = fragmentTimeoutMs;
        this.maxLength = maxLength;
        this.discardListener = discardListener;
    }

    /**
     * Appends a chunk of incoming characters and extracts every line it completes, leaving any
     * unterminated tail buffered for the next chunk.
     *
     * @param chunk the characters just received (may be {@code null} or empty)
     * @return the lines completed by this chunk, terminators stripped, in arrival order
     */
    public List<String> append(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        synchronized (buffer) {
            long now = System.currentTimeMillis();
            if (buffer.length() > 0 && now - lastChunkAt > fragmentTimeoutMs) {
                discard("Dropping abandoned partial line \"" + buffer + "\"");
            }
            lastChunkAt = now;
            buffer.append(chunk);

            int end;
            while ((end = indexOfTerminator(buffer)) >= 0) {
                String line = buffer.substring(0, end);
                // Consume the line plus the whole terminator, however many CR/LF characters it uses.
                int consumed = end;
                while (consumed < buffer.length() && isTerminator(buffer.charAt(consumed))) {
                    consumed++;
                }
                buffer.delete(0, consumed);
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }

            if (buffer.length() > maxLength) {
                discard("Dropping " + buffer.length() + " unterminated characters");
            }
        }
        return lines;
    }

    /**
     * Throws away everything buffered so far, for instance before starting a new exchange.
     */
    public void reset() {
        synchronized (buffer) {
            buffer.setLength(0);
        }
    }

    /**
     * Empties the buffer and reports why. Must be called while holding the buffer monitor.
     *
     * @param reason the message handed to {@link #discardListener}
     */
    private void discard(String reason) {
        buffer.setLength(0);
        if (discardListener != null) {
            discardListener.accept(reason);
        }
    }

    /**
     * @param data the buffered characters
     * @return the position of the first terminator character, or {@code -1} if there is none
     */
    private static int indexOfTerminator(CharSequence data) {
        for (int i = 0; i < data.length(); i++) {
            if (isTerminator(data.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param c a received character
     * @return {@code true} if the character is part of a line terminator
     */
    private static boolean isTerminator(char c) {
        return c == '\r' || c == '\n';
    }
}
