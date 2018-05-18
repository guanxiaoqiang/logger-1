package com.orhanobut.logger;

public class SingLineFormatStrategy implements FormatStrategy {

    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 5;


    private final boolean showThreadInfo;
    private final LogStrategy logStrategy;
    private final String tag;

    private SingLineFormatStrategy(SingLineFormatStrategy.Builder builder) {

        showThreadInfo = builder.showThreadInfo;
        logStrategy = builder.logStrategy;
        tag = builder.tag;
    }

    public static SingLineFormatStrategy.Builder newBuilder() {
        return new SingLineFormatStrategy.Builder();
    }

    @Override
    public void log(int priority, String onceOnlyTag, String message) {
        String tag = formatTag(onceOnlyTag);


        StringBuilder builder = getLogHeadContent();
        builder.append(message);
        message = builder.toString();
        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        if (length <= CHUNK_SIZE) {
            logContent(priority, tag, message);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(priority, tag, new String(bytes, i, count));
        }
    }

    private StringBuilder getLogHeadContent() {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (showThreadInfo) {
            builder.append(" Thread : \"" + Thread.currentThread().getName() + "\"");
        }

        String level = "";
        int stackOffset = getStackOffset(trace);
        builder.append(' ')
                .append(level)
                .append(getSimpleClassName(trace[stackOffset].getClassName()))
                .append(".")
                .append(trace[stackOffset].getMethodName())
                .append(" (")
                .append(trace[stackOffset].getFileName())
                .append(":")
                .append(trace[stackOffset].getLineNumber())
                .append(")  ");
        return builder;
    }

    private void logContent(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logChunk(logType, tag, line);
        }
    }

    private void logChunk(int priority, String tag, String chunk) {
        logStrategy.log(priority, tag, chunk);
    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private int getStackOffset(StackTraceElement[] trace) {
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LoggerPrinter.class.getName()) && !name.equals(Logger.class.getName())) {
                return i;
            }
        }
        return -1;
    }

    private String formatTag(String tag) {
        if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    public static class Builder {
        boolean showThreadInfo = true;
        LogStrategy logStrategy;
        String tag = "PRETTY_LOGGER";

        private Builder() {
        }


        public SingLineFormatStrategy.Builder showThreadInfo(boolean val) {
            showThreadInfo = val;
            return this;
        }

        public SingLineFormatStrategy.Builder logStrategy(LogStrategy val) {
            logStrategy = val;
            return this;
        }

        public SingLineFormatStrategy.Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public SingLineFormatStrategy build() {
            if (logStrategy == null) {
                logStrategy = new LogcatLogStrategy();
            }
            return new SingLineFormatStrategy(this);
        }
    }

}
