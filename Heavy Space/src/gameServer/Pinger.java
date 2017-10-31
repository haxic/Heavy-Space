package gameServer;

public class Pinger {
	int[] ping = new int[50];
	int pingPos = 0;
	int pingAcc = 0;
	long pingLastTime = 0;

	public void onSend() {
		pingLastTime = System.currentTimeMillis();
		// try {
		// Thread.sleep((long) (Math.random()*25+10));
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	public String onReceive() {
		int diff = (int) (System.currentTimeMillis() - pingLastTime);
		pingAcc -= ping[pingPos];
		pingAcc += diff;
		ping[pingPos] = diff;
		if (++pingPos == ping.length)
			pingPos = 0;
		return "LAST PING:" + diff + " AVG PING:" + (pingAcc / 50);
	}
}
