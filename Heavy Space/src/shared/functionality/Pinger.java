package shared.functionality;

public class Pinger {
	short[] ping = new short[15];
	short[] tick = new short[15];
	short pingPos = 0;
	short pingAcc = 0;
	short latestMS;
	short latestTick;
	short averageMS;

	public void handlePing(float dt, short receivedTick) {
		short ms = (short) (dt*1000);
		latestMS = ms;
		latestTick = receivedTick;
		pingAcc -= ping[pingPos];
		pingAcc += ms;
		ping[pingPos] = ms;
		tick[pingPos] = receivedTick;
		if (++pingPos == ping.length)
			pingPos = 0;
		averageMS = (short) (pingAcc / ping.length);
	}

	@Override
	public String toString() {
		return "avg:"+latestMS + " last:" + latestTick;
	}

	public short getAverageMS() {
		return averageMS;
	}
}
