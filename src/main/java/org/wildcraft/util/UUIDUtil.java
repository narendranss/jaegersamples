package org.wildcraft.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;


public class UUIDUtil {

    public static Pair<Long, Long> convertToLongPair(String UUIDString) {
        UUID uniqueID = UUID.fromString(UUIDString);
        return Pair.of(uniqueID.getMostSignificantBits(), uniqueID.getLeastSignificantBits());
    }

    public static String convertToUUID(Pair<Long, Long> uuidPair) {
        return new UUID(uuidPair.getLeft(), uuidPair.getRight()).toString();
    }

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;
    }

    public static void main(String[] args) {

        //RANDCOM GENERATION
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        System.out.println("Random UUID String = " + randomUUIDString);
        System.out.println("UUID version       = " + uuid.version());
        System.out.println("UUID variant       = " + uuid.variant());


        Pair<Long, Long> pair = convertToLongPair(randomUUIDString);
        System.out.println(pair);

        String uuidBack = convertToUUID(pair);
        System.out.println(uuidBack);
    }
}
