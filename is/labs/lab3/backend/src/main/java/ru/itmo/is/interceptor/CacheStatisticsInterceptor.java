package ru.itmo.is.interceptor;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import java.util.logging.Logger;

@Interceptor
@CacheStatisticsLogging
public class CacheStatisticsInterceptor {

    private static final Logger logger = Logger.getLogger(CacheStatisticsInterceptor.class.getName());

    @Inject
    private EntityManagerFactory entityManagerFactory;

    @AroundInvoke
    public Object logCacheStatistics(InvocationContext context) throws Exception {
        CacheStatisticsLogging annotation = getAnnotation(context);
        
        if (annotation == null || !annotation.enabled()) {
            logger.fine("CacheStatisticsLogging is disabled or annotation not found for method: " + 
                context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName());
            return context.proceed();
        }

        logger.info("CacheStatisticsLogging enabled for method: " + 
            context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName());

        CacheStatistics beforeStats = getCacheStatistics();
        logger.fine(String.format("Before method execution - Cache Hits: %d, Misses: %d", 
            beforeStats.getHits(), beforeStats.getMisses()));

        Object result;
        try {
            result = context.proceed();
        } finally {
            CacheStatistics afterStats = getCacheStatistics();
            logger.fine(String.format("After method execution - Cache Hits: %d, Misses: %d", 
                afterStats.getHits(), afterStats.getMisses()));

            long hits = afterStats.getHits() - beforeStats.getHits();
            long misses = afterStats.getMisses() - beforeStats.getMisses();

            if (hits > 0 || misses > 0) {
                logger.info(String.format(
                    "[CACHE STATS] Method: %s.%s | Hits: %d | Misses: %d | Hit Ratio: %.2f%%",
                    context.getMethod().getDeclaringClass().getSimpleName(),
                    context.getMethod().getName(),
                    hits,
                    misses,
                    (hits + misses > 0) ? (hits * 100.0 / (hits + misses)) : 0.0
                ));
            } else {
                logger.fine(String.format(
                    "[CACHE STATS] Method: %s.%s | No cache activity (Hits: 0, Misses: 0)",
                    context.getMethod().getDeclaringClass().getSimpleName(),
                    context.getMethod().getName()
                ));
            }
        }

        return result;
    }

    private CacheStatisticsLogging getAnnotation(InvocationContext context) {
        CacheStatisticsLogging methodAnnotation = context.getMethod().getAnnotation(CacheStatisticsLogging.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return context.getMethod().getDeclaringClass().getAnnotation(CacheStatisticsLogging.class);
    }

    private CacheStatistics getCacheStatistics() {
        try {
            if (entityManagerFactory == null) {
                logger.warning("EntityManagerFactory is null");
                return new CacheStatistics(0, 0);
            }

            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            if (sessionFactory == null) {
                logger.warning("Could not unwrap SessionFactory from EntityManagerFactory");
                return new CacheStatistics(0, 0);
            }
            
            Statistics statistics = sessionFactory.getStatistics();
            if (statistics == null) {
                logger.warning("Statistics is null");
                return new CacheStatistics(0, 0);
            }

            if (!statistics.isStatisticsEnabled()) {
                logger.info("Enabling Hibernate statistics");
                statistics.setStatisticsEnabled(true);
            }
            
            long secondLevelCacheHitCount = statistics.getSecondLevelCacheHitCount();
            long secondLevelCacheMissCount = statistics.getSecondLevelCacheMissCount();
            
            logger.fine(String.format("Cache stats - Hits: %d, Misses: %d", secondLevelCacheHitCount, secondLevelCacheMissCount));
            
            return new CacheStatistics(secondLevelCacheHitCount, secondLevelCacheMissCount);
        } catch (Exception e) {
            logger.warning("Error retrieving cache statistics: " + e.getMessage());
            e.printStackTrace();
            return new CacheStatistics(0, 0);
        }
    }

    private static class CacheStatistics {
        private final long hits;
        private final long misses;

        public CacheStatistics(long hits, long misses) {
            this.hits = hits;
            this.misses = misses;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }
    }
}

