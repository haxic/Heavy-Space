package gameServer;

import shared.functionality.Globals;

public class Pinger {
	int[] ping = new int[15];
	short[] tick = new short[15];
	int pingPos = 0;
	int pingAcc = 0;
	int latestMS;
	short latestTick;
	int averageMS;

	public void handlePing(long timeSent, short receivedTick) {
		int ms = (int) (Globals.now - timeSent);
		latestMS = ms;
		latestTick = receivedTick;
		pingAcc -= ping[pingPos];
		pingAcc += ms;
		ping[pingPos] = ms;
		tick[pingPos] = receivedTick;
		if (++pingPos == ping.length)
			pingPos = 0;
		averageMS = pingAcc / ping.length;
	}

	@Override
	public String toString() {
		return "LAST PING:" + latestMS + " AVG PING:" + averageMS + " TICK:" + latestTick;
	}

	public int getAverageMS() {
		return averageMS;
	}
}
