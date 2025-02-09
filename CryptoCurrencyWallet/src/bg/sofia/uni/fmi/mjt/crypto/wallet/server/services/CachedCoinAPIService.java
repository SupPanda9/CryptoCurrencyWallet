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
        this.lastFetchTime = Instant.EPOCH;
    }

    public List<CryptoOffering> getCryptoOfferings() throws IOException {
        lock.readLock().lock();
        try {
            boolean validCache = isCacheValid();
            System.out.println("Cache valid? " + validCache + " | Last fetch: " + lastFetchTime);
            if (validCache) {
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
            return cachedOfferings != null &&
                Instant.now().toEpochMilli() - lastFetchTime.toEpochMilli() <= CACHE_EXPIRY_TIME_MS;
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<CryptoOffering> refreshCache() throws IOException {
        lock.writeLock().lock();
        try {
            System.out.println("Fetching fresh crypto offerings from CoinAPI...");
            try {
                List<CryptoOffering> offerings = coinAPIService.fetchAllCryptoOfferings();
                cachedOfferings = sortOfferingsByVolume(offerings);
                lastFetchTime = Instant.now();

                if (!cachedOfferings.isEmpty()) {
                    System.out.println("Cached " + cachedOfferings.size() + " crypto offerings.");
                } else {
                    System.out.println("ERROR: No offerings fetched!");
                }
                return cachedOfferings;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                throw new IOException("Thread interrupted while fetching crypto data", e);
            }
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
