package com.lucaf.robotic_core.wenglor.ep8e001.data;

import lombok.Getter;

@Getter
public class IOLinkData {
    @Getter
    public static class IOData {
        private boolean valid;
        private byte[]  value;
    }

    private IOData iolink;
    private boolean iqValue;
}
