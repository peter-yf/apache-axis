/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.components.uuid;

import java.util.Random;
import java.security.SecureRandom;

/**
 * Creates time-based UUID's. See the <a href="http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-03.txt">UUID Internet Draft</a> for details.
 *
 * @author Jarek Gawor (gawor@apache.org)
 */
public class FastUUIDGen implements UUIDGen {

    private static Random secureRandom;

    private static String nodeStr;
    private static int clockSequence;

    private long lastTime = 0;

    static {
        // problem: the node should be the IEEE 802 ethernet address, but can not
        // be retrieved in Java yet.
        // see bug ID 4173528
        // workaround (also suggested in bug ID 4173528)
        // If a system wants to generate UUIDs but has no IEE 802 compliant
        // network card or other source of IEEE 802 addresses, then this section
        // describes how to generate one.
        // The ideal solution is to obtain a 47 bit cryptographic quality random
        // number, and use it as the low 47 bits of the node ID, with the most
        // significant bit of the first octet of the node ID set to 1. This bit
        // is the unicast/multicast bit, which will never be set in IEEE 802
        // addresses obtained from network cards; hence, there can never be a
        // conflict between UUIDs generated by machines with and without network
        // cards.
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (Exception e) {
            secureRandom = new Random();
        }

        nodeStr = getNodeHexValue();
        clockSequence = getClockSequence();
    }

    private static String getNodeHexValue() {
        long node = 0;
        long nodeValue = 0;
        while ( (node = getBitsValue(nodeValue, 47, 47)) == 0 ) {
            nodeValue = secureRandom.nextLong();
        }
        node = node | 0x800000000000L;
        return leftZeroPadString(Long.toHexString(node), 12);
    }

    private static int getClockSequence() {
        return secureRandom.nextInt(16384);
    }

    public String nextUUID() {
        long time = System.currentTimeMillis();

        long timestamp = time * 10000;
        timestamp += 0x01b21dd2L << 32;
        timestamp += 0x13814000;
        
        synchronized(this) {
            if (time - lastTime <= 0) {
                clockSequence = ((clockSequence + 1) & 16383);
            }
            lastTime = time;
        }

        long timeLow = getBitsValue(timestamp, 32, 32);
        long timeMid = getBitsValue(timestamp, 48, 16);
        long timeHi = getBitsValue(timestamp, 64, 16) | 0x1000;

        long clockSeqLow = getBitsValue(clockSequence, 8, 8);
        long clockSeqHi = getBitsValue(clockSequence, 16, 8) | 0x80;
        
        String timeLowStr = leftZeroPadString(Long.toHexString(timeLow), 8);
        String timeMidStr = leftZeroPadString(Long.toHexString(timeMid), 4);
        String timeHiStr = leftZeroPadString(Long.toHexString(timeHi), 4);

        String clockSeqHiStr = leftZeroPadString(Long.toHexString(clockSeqHi), 2);
        String clockSeqLowStr = leftZeroPadString(Long.toHexString(clockSeqLow), 2);

        StringBuffer result = new StringBuffer(36);
        result.append(timeLowStr).append("-");
        result.append(timeMidStr).append("-");
        result.append(timeHiStr).append("-");
        result.append(clockSeqHiStr).append(clockSeqLowStr);
        result.append("-").append(nodeStr);

        return result.toString();
    }

    private static long getBitsValue(long value, int startBit, int bitLen) {
        return ((value << (64-startBit)) >>> (64-bitLen));
    }

    private static final String leftZeroPadString(String bitString, int len) {
        if (bitString.length() < len) {
            int nbExtraZeros = len - bitString.length();
            StringBuffer extraZeros = new StringBuffer();
            for (int i = 0; i < nbExtraZeros; i++) {
                extraZeros.append("0");
            }
            extraZeros.append(bitString);
            bitString = extraZeros.toString();
        }
        return bitString;
    }

}
