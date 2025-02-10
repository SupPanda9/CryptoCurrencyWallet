package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;
import bg.sofia.uni.fmi.mjt.crypto.wallet.server.utils.LoggerUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachedCoinAPIService {
    private static final long CACHE_EXPIRY_TIME_MS = 30 * 60 * 1000; // 30 minutes
    private final CoinAPIService coinAPIService;
    private List<CryptoOffering> cachedOfferings;
    private Instant lastFetchTime;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CachedCoinAPIService(CoinAPIService coinAPIService) {
        this.coinAPIService = coinAPIService;
        this.lastFetchTime = Instant.EPOCH;
        LoggerUtil.logInfo("Initialized CachedCoinAPIService");
    }

    public List<CryptoOffering> getCryptoOfferings() throws IOException {
        lock.readLock().lock();
        try {
            boolean validCache = isCacheValid();
            LoggerUtil.logInfo("Cache valid? " + validCache + " | Last fetch: " + lastFetchTime);
            if (validCache) {
                LoggerUtil.logInfo("Returning cached offerings.");
                return cachedOfferings;
            }
        } finally {
            lock.readLock().unlock();
        }

        return refreshCache();
    }

    private boolean isCacheValid() {
        lock.readLock().lock();
        try {
            boolean valid = cachedOfferings != null &&
                Instant.now().toEpochMilli() - lastFetchTime.toEpochMilli() <= CACHE_EXPIRY_TIME_MS;
            LoggerUtil.logInfo("Cache validation result: " + valid);
            return valid;
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<CryptoOffering> refreshCache() throws IOException {
        lock.writeLock().lock();
        try {
            LoggerUtil.logInfo("Fetching fresh crypto offerings from CoinAPI...");
            try {
                List<CryptoOffering> offerings = coinAPIService.fetchAllCryptoOfferings();
                cachedOfferings = sortOfferingsByVolume(offerings);
                lastFetchTime = Instant.now();

                if (!cachedOfferings.isEmpty()) {
                    LoggerUtil.logInfo("Successfully cached " + cachedOfferings.size() + " crypto offerings.");
                } else {
                    LoggerUtil.logWarning("ERROR: No offerings fetched!");
                }
                return cachedOfferings;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LoggerUtil.logError("Thread interrupted while fetching crypto data", e);
                throw new IOException("Thread interrupted while fetching crypto data", e);
            } catch (IOException e) {
                LoggerUtil.logError("Error fetching crypto data from CoinAPI", e);
                throw e;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<CryptoOffering> sortOfferingsByVolume(List<CryptoOffering> offerings) {
        LoggerUtil.logInfo("Sorting crypto offerings by volume...");
        return offerings.stream()
            .sorted(Comparator.comparingDouble(CryptoOffering::volumeUsd).reversed())
            .toList();
    }
}
