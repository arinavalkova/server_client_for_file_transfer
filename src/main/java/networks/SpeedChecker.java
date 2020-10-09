package networks;

public class SpeedChecker {
    private volatile long totalBytesCount = 0;
    private volatile long lastBytesCount = 0;
    private volatile long startTime = 0;
    private volatile long lastTime = 0;
    private volatile long endTime = 0;
    private volatile long instantSpeed = 0;


    public SpeedChecker() { }

    public  void addBytesCount(long gottenBytesCount) {
        var t = totalBytesCount;
        totalBytesCount = t + gottenBytesCount;
    }

    public void setEndTime(long gottenLastTime) {
        endTime = gottenLastTime;
    }

    public double getInstantSpeed() {
        long newTime = System.currentTimeMillis();
        if((newTime - lastTime) == 0) {
            instantSpeed = 0;
        } else {
            instantSpeed = (totalBytesCount - lastBytesCount) / (newTime - lastTime);
        }
        lastTime = newTime;
        lastBytesCount = totalBytesCount;

        var ret = (double) instantSpeed * 1000 / 1024 / 1024;
        return ret;
    }

    public double getAverageSpeed() {
        return (double) totalBytesCount / (System.currentTimeMillis() - startTime) * 1000 / 1024 / 1024;
    }

    public void setStartTime(long gottenFirstTime) {
        lastBytesCount = 0;
        startTime = lastTime = gottenFirstTime;
    }

    public void reset() {
        totalBytesCount = lastBytesCount = 0;
        startTime = 0;
        endTime = 0;
        instantSpeed = 0;
    }
}
