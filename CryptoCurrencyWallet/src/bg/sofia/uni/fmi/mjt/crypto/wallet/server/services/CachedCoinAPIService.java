package bg.sofia.uni.fmi.mjt.crypto.wallet.server.services;

import bg.sofia.uni.fmi.mjt.crypto.wallet.server.models.CryptoOffering;

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
        this.lastFetchTime = Instant.EPOCH;  // Ensures first call fetches from API
    }

    public List<CryptoOffering> getCryptoOfferings() throws IOException {
        if (isCacheValid()) {
            return cachedOfferings;
        }

        System.out.println("Cache expired. Fetching new data from CoinAPI...");
        return refreshCache();
    }

    private boolean isCacheValid() {
        lock.readLock().lock();
        try {
            return cachedOfferings != null &&
                Instant.now().toEpochMilli() - lastFetchTime.toEpochMilli() <= CACHE_EXPIRY_TIME_MS;
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<CryptoOffering> refreshCache() throws IOException {
        lock.writeLock().lock();
        try {
            if (!isCacheValid()) {
                List<CryptoOffering> offerings = coinAPIService.fetchAllCryptoOfferings();
                cachedOfferings = sortOfferingsByVolume(offerings);
                lastFetchTime = Instant.now();
            }
            return cachedOfferings;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operation interrupted while fetching crypto offerings.", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<CryptoOffering> sortOfferingsByVolume(List<CryptoOffering> offerings) {
        return offerings.stream()
            .sorted(Comparator.comparingDouble(CryptoOffering::volumeUsd).reversed())
            .toList();
    }

    public CryptoOffering getCryptoById(String assetId) throws IOException, InterruptedException {
        lock.readLock().lock();
        try {
            if (cachedOfferings != null) {
                return cachedOfferings.stream()
                    .filter(o -> o.assetId().equalsIgnoreCase(assetId))
                    .findFirst()
                    .orElse(null);
            }
        } finally {
            lock.readLock().unlock();
        }

        System.out.println("Crypto " + assetId + " not found in cache. Fetching from API...");
        return coinAPIService.fetchCryptoById(assetId);
    }
}
