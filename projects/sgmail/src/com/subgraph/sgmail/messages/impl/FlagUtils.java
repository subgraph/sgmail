package com.subgraph.sgmail.messages.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.subgraph.sgmail.messages.StoredMessage;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

public class FlagUtils {

    static boolean isFlagBitSet(long flagBits, long flag) {
        return((flagBits & flag) == flag);
    }

    private final static BiMap<Flags.Flag, Long> flagMap = new ImmutableBiMap.Builder<Flags.Flag, Long>()
            .put(Flags.Flag.ANSWERED, StoredMessage.FLAG_ANSWERED)
            .put(Flags.Flag.DELETED, StoredMessage.FLAG_DELETED)
            .put(Flags.Flag.DRAFT, StoredMessage.FLAG_DRAFT)
            .put(Flags.Flag.FLAGGED, StoredMessage.FLAG_FLAGGED)
            .put(Flags.Flag.RECENT, StoredMessage.FLAG_RECENT)
            .put(Flags.Flag.SEEN, StoredMessage.FLAG_SEEN)
            .build();



    public static long getFlagsFromMessage(Message message) throws MessagingException {
        return getFlagBitsFromFlags(message.getFlags());
    }

    public static long getFlagBitFromFlag(Flags.Flag flag) {
        if(flagMap.containsKey(flag)) {
            return flagMap.get(flag);
        } else {
            return 0;
        }
    }
    public static long getFlagBitsFromFlags(Flags flags) {
        long flagBits = 0;
        for(Flags.Flag f: flagMap.keySet()) {
            if(flags.contains(f)) {
                flagBits |= flagMap.get(f);
            }
        }
        return flagBits;
    }
}
