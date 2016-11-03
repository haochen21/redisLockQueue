package redisLockQueue.util;

public enum MacToHex {

	MACTOHEX;

	public String getHex(String mac) {
		String[] macAddressParts = mac.split(":");

		// convert hex string to byte values
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(macAddressParts[i]);
		}
		return sb.toString();
	}
}
