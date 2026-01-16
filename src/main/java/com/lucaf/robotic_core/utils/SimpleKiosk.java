package com.lucaf.robotic_core.utils;

import java.awt.*;
import java.io.IOException;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Module that starts the Chromium browser in kiosk mode.
 */
public class SimpleKiosk {

    static final String os = System.getProperty("os.name").toLowerCase();

    final int width;
    final int height;
    final String url;


    /**
     * Executes a command and returns the output.
     *
     * @param cmd the command to execute
     * @return the output of the command
     * @throws IOException if an I/O error occurs
     */
    public String execCmd(String cmd) throws IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    boolean isWindowsChromeInstalled(){
        try {
            return execCmd("reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe\"").contains("chrome.exe");
        } catch (Exception e) {
            return false;
        }
    }

    boolean isWindowsChromiumInstalled(){
        try {
            return execCmd("reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chromium.exe\"").contains("chromium.exe");
        } catch (Exception e) {
            return false;
        }
    }

    boolean isMacChromeInstalled(){
        try {
            return !execCmd("mdfind kMDItemCFBundleIdentifier == 'com.google.Chrome'").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    String isLinuxChromiumInstalled(){
        try {
            if (!execCmd("which chromium").isEmpty()) {
                return "chromium";
            }
            return execCmd("which chromium-browser").isEmpty() ? null : "chromium-browser";
        } catch (Exception e) {
            return null;
        }
    }

    String isLinuxChromeInstalled(){
        try {
            String cmd = "which google-chrome";
            String output = execCmd(cmd);
            if (!output.isEmpty()) {
                return "google-chrome";
            }
            cmd = "which google-chrome-stable";
            output = execCmd(cmd);
            return output.isEmpty() ? null : "google-chrome-stable";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if chromium is already running
     *
     * @return true if it is running
     */
    boolean isLinuxChromiumRunning() {
        try {
            return !execCmd("pgrep -f chromium").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    boolean isLinuxChromeRunning() {
        try {
            return !execCmd("pgrep -f google-chrome").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    boolean isWindowsChromiumRunning(){
        try {
            return execCmd("tasklist /FI \"IMAGENAME eq chromium.exe\"").contains("chromium.exe");
        } catch (Exception e) {
            return false;
        }
    }

    boolean isWindowsChromeRunning(){
        try {
            return execCmd("tasklist /FI \"IMAGENAME eq chrome.exe\"").contains("chrome.exe");
        } catch (Exception e) {
            return false;
        }
    }

    boolean isMacChromeRunning(){
        try {
            return !execCmd("pgrep -f 'Google Chrome'").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    double computeFactor(Dimension screenSize) {
        double scaleWidth = screenSize.getWidth() / (double) this.width;
        double scaleHeight = screenSize.getHeight() / (double) this.height;
        double scale = Math.min(scaleWidth, scaleHeight);
        scale = Math.round(scale * 10.0) / 10.0;
        return scale;
    }

    String[] getKioskArgs(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double factor = computeFactor(screenSize);
        int width = (int) (screenSize.width / factor);
        int height = (int) (screenSize.height / factor);
        return new String[]{
                "--kiosk",
                "--noerrdialogs",
                "--disable-infobars",
                "--window-size=" + width + "," + height,
                "--window-position=0,0",
                "--disable-translate",
                "--fast",
                "--fast-start",
                "--disable-features=TranslateUI",
                "--overscroll-history-navigation=0",
                "--force-device-scale-factor=" + factor,
                "--disable-pinch",
                "--no-first-run",
                url
        };
    }

    void spawnBrowser(ProcessBuilder pb){
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        runAsync(() -> {
            try {
                pb.start();
            } catch (Exception ignored) {}
        });
    }

    void spawnLinuxKiosk(){
        String browser;
        String chromium = isLinuxChromiumInstalled();
        String chrome = isLinuxChromeInstalled();
        if (chromium != null) {
            browser = chromium;
            if (isLinuxChromiumRunning()) return;
        } else if (chrome != null) {
            browser = chrome;
            if (isLinuxChromeRunning()) return;
        } else {
            return;
        }
        String[] args = getKioskArgs();
        String[] cmd = new String[args.length + 1];
        System.arraycopy(args, 0, cmd, 1, args.length);
        cmd[0] = browser;
        spawnBrowser(new ProcessBuilder(cmd));
    }

    void spawnWindowsKiosk(){
        String browser;
        if (isWindowsChromiumInstalled()) {
            browser = "chromium";
            if (isWindowsChromiumRunning()) return;
        } else if (isWindowsChromeInstalled()) {
            browser = "chrome";
            if (isWindowsChromeRunning()) return;
        } else {
            return;
        }
        String[] args = getKioskArgs();
        String[] cmd = new String[args.length + 4];
        cmd[0] = "cmd.exe";
        cmd[1] = "/c";
        cmd[2] = "start";
        cmd[3] = browser;
        System.arraycopy(args, 0, cmd, 4, args.length);
        spawnBrowser(new ProcessBuilder(cmd));
    }

    void spawnMacKiosk(){
        String browser;
        if (isMacChromeInstalled()) {
            browser = "Google Chrome";
            if (isMacChromeRunning()) return;
        } else {
            return;
        }
        String[] args = getKioskArgs();
        String[] cmd = new String[args.length + 4];
        cmd[0] = "open";
        cmd[1] = "-a";
        cmd[2] = browser;
        cmd[3] = "--args";
        System.arraycopy(args, 0, cmd, 4, args.length);
        spawnBrowser(new ProcessBuilder(cmd));
    }

    public SimpleKiosk(String url, int width, int height) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public void spawn(){
        if (os.contains("win")) {
            spawnWindowsKiosk();
        }else if (os.contains("mac")){
            spawnMacKiosk();
        }else{
            spawnLinuxKiosk();
        }
    }
}
