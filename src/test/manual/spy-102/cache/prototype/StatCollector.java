package cache.prototype;

import java.util.concurrent.atomic.AtomicLong;

public class StatCollector {
    private AtomicLong putCount = new AtomicLong(0);
    private AtomicLong putErrorCount = new AtomicLong(0);
    private AtomicLong putMessageSize = new AtomicLong(0);
    private AtomicLong putLatency = new AtomicLong(0);

    private AtomicLong getCount = new AtomicLong(0);
    private AtomicLong getErrorCount = new AtomicLong(0);
    private AtomicLong getMessageSize = new AtomicLong(0);
    private AtomicLong getLatency = new AtomicLong(0);

    private AtomicLong delCount = new AtomicLong(0);
    private AtomicLong delErrorCount = new AtomicLong(0);
    private AtomicLong delMessageSize = new AtomicLong(0);
    private AtomicLong delLatency = new AtomicLong(0);

    private AtomicLong updCount = new AtomicLong(0);
    private AtomicLong updErrorCount = new AtomicLong(0);
    private AtomicLong updMessageSize = new AtomicLong(0);
    private AtomicLong updLatency = new AtomicLong(0);
 
    private AtomicLong staleCount = new AtomicLong(0);


    public void incrementStaleCount() {
        staleCount.incrementAndGet();
    }

    // Setters

    public void incrementPutCount() {
        putCount.incrementAndGet();
    }

    public void incrementGetCount() {
        getCount.incrementAndGet();
    }

    public void incrementDelCount() {
        delCount.incrementAndGet();
    }

    public void incrementPutErrorCount() {
        putErrorCount.incrementAndGet();
    }

    public void incrementGetErrorCount() {
        getErrorCount.incrementAndGet();
    }

    public void incrementDelErrorCount() {
        delErrorCount.incrementAndGet();
    }


    public void addPutLateny(long timeInNanos) {
        putLatency.addAndGet(timeInNanos);
    }

    public void addDelLateny(long timeInNanos) {
        delLatency.addAndGet(timeInNanos);
    }

    public void addGetLatency(long timeInNanos) {
        getLatency.addAndGet(timeInNanos);
    }

    public void addPutMessageSize(long sizeInBytes) {
        getPutMessageSize().addAndGet(sizeInBytes);
    }

    public void addGetMessageSize(long sizeInBytes) {
        getGetMessageSize().addAndGet(sizeInBytes);
    }



    // Getters
    public AtomicLong getPutCount() {
        return putCount;
    }

    public AtomicLong getStaleCount() {
        return staleCount;
    }

    public AtomicLong getPutLatency() {
        return putLatency;
    }

    public AtomicLong getPutMessageSize() {
        return putMessageSize;
    }

	public AtomicLong getGetCount() {
		return getCount;
	}

	public AtomicLong getPutErrorCount() {
		return putErrorCount;
	}

	public AtomicLong getGetErrorCount() {
		return getErrorCount;
	}

	public AtomicLong getGetMessageSize() {
		return getMessageSize;
	}

	public AtomicLong getGetLatency() {
		return getLatency;
	}
 
	public void reset(){
		putCount.set(0);
		putLatency.set(0);
		putMessageSize.set(0);
		putErrorCount.set(0);
		getCount.set(0);
		getLatency.set(0);
		getMessageSize.set(0);
		getErrorCount.set(0);
		delCount.set(0);
		delLatency.set(0);
		delMessageSize.set(0);
		delErrorCount.set(0);
		updCount.set(0);
		updLatency.set(0);
		updMessageSize.set(0);
		updErrorCount.set(0);
	}
}
