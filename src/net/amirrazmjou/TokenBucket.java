package net.amirrazmjou;

import com.google.common.base.Ticker;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

/**
 * A token bucket implementation it has a finite capacity and any added
 * tokens that would exceed this capacity will "overflow" out of the bucket and are lost forever.
 * It is of a analogous to leaky bucket in that sense. For more information:
 * https://en.wikipedia.org/wiki/Leaky_bucket
 *
 * bucket refill period duration , bigger bucket size makes the traffic bursty and unsuitable for
 * real-time, multimedia services. The smaller bucket period duration makes a smoother traffic
 * but it consumes more CPU resources. It is basically it is a trade-off between quality and
 * resource management.
 */
public class TokenBucket {
    protected long size;
    protected final long numTokens;
    protected final long periodDuration;
    protected long nextRefillTime;


    public TokenBucket(long numTokens) {
        this.numTokens = numTokens;

        /**
        * bucket refill period duration , bigger bucket size makes the traffic bursty and make it unsuitable for
        * real-time, multimedia services. The smaller bucket size makes more smoother traffic
        * shape but it consumes more cpu resources. so basically it is a trade-off between
        * quality and resource management.
        */
        this.periodDuration = TimeUnit.SECONDS.toNanos(1);
        this.nextRefillTime = 0;
    }


    /**
     * Consume specified number of tokens from the bucket.  If no token is currently available then this
     * method will block until a token becomes available.
     *
     * @param tokens number of tokens
     */
    public  void consume(long tokens) {
        while (true) {
            // this block must be synchronized so only one thread at a time can refill the bucket and
            // move next refill time forward
            synchronized (this) {
                long currentTime = Ticker.systemTicker().read();
                if (currentTime > nextRefillTime) {
                    nextRefillTime = currentTime + periodDuration;
                    size = numTokens;
                }
            }

            // consume the token and break if there is any
            if (size >= tokens) {
                size -= tokens;
                break;
            }

            // in all cases, if a thread is interrupted during such a call, the call continues
            // to block until the result is available or the timeout elapses, and only then
            // re-interrupts the thread.
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.NANOSECONDS);
        }
    }
}
