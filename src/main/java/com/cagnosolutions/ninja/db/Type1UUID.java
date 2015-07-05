package com.cagnosolutions.ninja.db;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Type1UUID {

	private static final long START_EPOCH = -12219292800000L;
	private static final long clockSeqAndNode = makeClockSeqAndNode();
	private static final long MIN_CLOCK_SEQ_AND_NODE = 0x8080808080808080L;
	private static final long MAX_CLOCK_SEQ_AND_NODE = 0x7f7f7f7f7f7f7f7fL;
	private static final Type1UUID instance = new Type1UUID();
	private long lastNanos;

	private Type1UUID() {
		// make sure someone didn't whack the clockSeqAndNode by changing the order of instantiation.
		if (clockSeqAndNode == 0) throw new RuntimeException("singleton instantiation is misplaced.");
	}

	// Creates a type 1 UUID (time-based UUID)
	public static UUID getTimeUUID() {
		return new UUID(instance.createTimeSafe(), clockSeqAndNode);
	}

	// Creates a type 1 UUID (time-based UUID) with the timestamp of when, in milliseconds
	public static UUID getTimeUUID(long when) {
		return new UUID(createTime(fromUnixTimestamp(when)), clockSeqAndNode);
	}

	// Creates a type 1 uuid from raw bytes
	public static UUID getUUID(ByteBuffer raw) {
		return new UUID(raw.getLong(raw.position()), raw.getLong(raw.position() + 8));
	}

	// Decomposes a uuid into raw bytes
	public static byte[] decompose(UUID uuid) {
		long most = uuid.getMostSignificantBits();
		long least = uuid.getLeastSignificantBits();
		byte[] b = new byte[16];
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) (most >>> ((7 - i) * 8));
			b[8 + i] = (byte) (least >>> ((7 - i) * 8));
		}
		return b;
	}

	// Returns a 16 byte representation of a type 1 UUID (a time-based UUID), based on the current system time
	public static byte[] getTimeUUIDBytes() {
		return createTimeUUIDBytes(instance.createTimeSafe());
	}

	private static byte[] createTimeUUIDBytes(long msb) {
		long lsb = clockSeqAndNode;
		byte[] uuidBytes = new byte[16];
		for (int i = 0; i < 8; i++)
			uuidBytes[i] = (byte) (msb >>> 8 * (7 - i));
		for (int i = 8; i < 16; i++)
			uuidBytes[i] = (byte) (lsb >>> 8 * (7 - i));
		return uuidBytes;
	}

	// Return milliseconds since Unix epoch
	public static long unixTimestamp(UUID uuid) {
		return (uuid.timestamp() / 10000) + START_EPOCH;
	}

	// Return microseconds since Unix epoch
	public static long microsTimestamp(UUID uuid) {
		return (uuid.timestamp() / 10) + START_EPOCH * 1000;
	}

	// Return timestamp in milliseconds since Unix epoch
	private static long fromUnixTimestamp(long timestamp) {
		return (timestamp - START_EPOCH) * 10000;
	}

	// Returns a milliseconds-since-epoch value for a type-1 UUID
	public static long getAdjustedTimestamp(UUID uuid) {
		if (uuid.version() != 1)
			throw new IllegalArgumentException("incompatible with uuid version: " + uuid.version());
		return (uuid.timestamp() / 10000) + START_EPOCH;
	}

	private static long makeClockSeqAndNode() {
		long clock = new Random(System.currentTimeMillis()).nextLong();
		long lsb = 0;
		lsb |= 0x8000000000000000L;                 // variant (2 bits)
		lsb |= (clock & 0x0000000000003FFFL) << 48; // clock sequence (14 bits)
		lsb |= makeNode();                          // 6 bytes
		return lsb;
	}

	// Needs to return two different values for the same when.
	// we can generate at most 10k UUIDs per ms.
	private synchronized long createTimeSafe() {
		long nanosSince = (System.currentTimeMillis() - START_EPOCH) * 10000;
		if (nanosSince > lastNanos)
			lastNanos = nanosSince;
		else
			nanosSince = ++lastNanos;

		return createTime(nanosSince);
	}

	private long createTimeUnsafe(long when, int nanos) {
		long nanosSince = ((when - START_EPOCH) * 10000) + nanos;
		return createTime(nanosSince);
	}

	private static long createTime(long nanosSince) {
		long msb = 0L;
		msb |= (0x00000000ffffffffL & nanosSince) << 32;
		msb |= (0x0000ffff00000000L & nanosSince) >>> 16;
		msb |= (0xffff000000000000L & nanosSince) >>> 48;
		msb |= 0x0000000000001000L; // sets the version to 1.
		return msb;
	}

	private static Collection<InetAddress> getAllLocalAddresses() {
		Collection<InetAddress> addrs = new ArrayList<>();
		Enumeration<NetworkInterface> nets = null;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		for(NetworkInterface iface : Collections.list(nets)) {
			try {
				if (!iface.isLoopback())
					addrs.add(iface.getInetAddresses().nextElement());
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		return addrs;
	}

	private static long makeNode() {
	   /*
        * We don't have access to the MAC address but need to generate a node part
        * that identify this host as uniquely as possible.
        * The spec says that one option is to take as many source that identify
        * this node as possible and hash them together. That's what we do here by
        * gathering all the ip of this host.
        * Note that FBUtilities.getBroadcastAddress() should be enough to uniquely
        * identify the node *in the cluster* but it triggers DatabaseDescriptor
        * instanciation and the UUID generator is used in Stress for instance,
        * where we don't want to require the yaml.
        */
		Collection<InetAddress> localAddresses = getAllLocalAddresses();
		if(localAddresses.isEmpty())
			throw new RuntimeException("Cannot generate the node component of the UUID because cannot retrieve any IP addresses.");
		// ideally, we'd use the MAC address, but java doesn't expose that.
		byte[] hash = hash(localAddresses);
		long node = 0;
		for (int i = 0; i < Math.min(6, hash.length); i++)
			node |= (0x00000000000000ff & (long) hash[i]) << (5 - i) * 8;
		assert (0xff00000000000000L & node) == 0;
		// Since we don't use the mac address, the spec says that multicast
		// bit (least significant bit of the first octet of the node ID) must be 1.
		return node | 0x0000010000000000L;
	}

	private static byte[] hash(Collection<InetAddress> data) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			for (InetAddress addr : data)
				messageDigest.update(addr.getAddress());

			return messageDigest.digest();
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("MD5 digest algorithm is not available", nsae);
		}
	}

}
