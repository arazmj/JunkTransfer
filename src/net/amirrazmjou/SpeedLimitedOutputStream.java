package net.amirrazmjou;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Amir Razmjou on 1/11/16.
 */
public class SpeedLimitedOutputStream extends FilterOutputStream {
    private TokenBucket bucket;
    private final int burst;

    /**
     * Creates an speed limited output stream built on top of the
     * specified underlying output stream and token bucket
     * @param burst maximum amount of traffic (in bytes) that can be sent over the wire
     *              small burst size takes more CPU resources.
     * @param bucket speed limiter token bucket
     * @param out the underlying output stream
     */
    public SpeedLimitedOutputStream(int burst, TokenBucket bucket, OutputStream out) {
        super(out);
        this.bucket = bucket;
        this.burst = burst;
    }

    @Override
    public void write(int b) throws IOException {
        bucket.consume(1);
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        int off = 0;
        for (; off + burst <= b.length; off += burst) {
            bucket.consume(burst);
            super.write(b, off, burst);
        }
        int rem = b.length % burst;
        bucket.consume(rem);
        super.write(b, off, rem);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (; off + burst <= len; off += burst) {
            bucket.consume(burst);
            super.write(b, off, burst);
        }
        int rem = len % burst;
        bucket.consume(rem);
        super.write(b, off, rem);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
