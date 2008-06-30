package com.aconex.cache.policy.replacement;

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.custardsource.cache.util.LogUtils;

/**
 * Cache eviction policy which implements the Fixed Replacement Cache algorithm by Nimrod Megiddo
 * and Dharmendra S. Modhain of IBM. The algorithm partitions the cache space into two queues: hit
 * items are originally loaded into 't1' (for recently-hit items), once they are hit again they move
 * into 't2' (for frequently-hit items). The split between the sizes of t1 and t2 is determined by
 * the user. The Adaptive Replacement cache (as implemented in
 * {@link AdaptiveReplacementCacheManager}) is a dynamically self-tuning version of this.
 * 
 * @see <a href="http://www.almaden.ibm.com/cs/people/dmodha/ARC.pdf"><cite>Outperforming LRU with
 *      an Adaptive Replacement Cache Algorithm</cite></a>, Nimrod Megiddo and Dharmendra S.
 *      Modhain
 * @see <a href="www.usenix.org/publications/login/2003-08/pdfs/Megiddo.pdf"><cite>One Up On LRU</cite></a>,
 *      Nimrod Megiddo and Dharmendra S. Modhain, <cite>;login:</cite> August 2003
 * @see AdaptiveReplacementCacheManager
 * @author pcowan
 */
public class FixedReplacementCacheManager<T> extends ReplacementCacheManager<T> {
    private static final Log LOG = LogFactory.getLog(FixedReplacementCacheManager.class);
    private final FixedReplacementConfiguration config;

    public FixedReplacementCacheManager(FixedReplacementConfiguration config) {
        this.config = config;
    }

    @Override
    public void assertInvariants() {
        assertInvariant(cacheSize() >= 0);
        assertInvariant(cacheSize() <= cacheCapacity());
    }

    @Override
    protected int cacheCapacity() {
        return config.getMaxNodes();
    }

    @Override
    protected void dumpStatus() {
        if (LOG.isTraceEnabled()) {
            // TODO make these one message
            LOG.trace("  t1 size: " + config.getT1Size());
            LOG.trace("  " + dumpQueue(t1));
            LOG.trace("  " + dumpQueue(t2));
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("  Target t1 size: " + config.getT1Size() + ", actual capacities "
                    + dumpCapacity(t1) + " " + dumpCapacity(t2));
        }

    }

    @Override
    protected void onHit(T entry, Queue<T> currentLocation) {
        LogUtils.debug(LOG, " Moving from %s to head of t2", queueName(currentLocation));
        moveNode(entry, currentLocation, t2);
    }

    @Override
    protected void onMiss(T entry) {
        if (!t1.isEmpty() && t1.size() >= config.getT1Size()) {
            LogUtils.debug(LOG, " t1 full, evicting from t1");
            evictNode(t1);
        } else if (!t2.isEmpty() && cacheSize() >= cacheCapacity()) {
            LogUtils.debug(LOG, " cache full, evicting from t2");
            evictNode(t2);
        } else {
            LogUtils.debug(LOG, " room in the cache, straight to t1");
        }
        insertNode(entry, t1);
    }
}
