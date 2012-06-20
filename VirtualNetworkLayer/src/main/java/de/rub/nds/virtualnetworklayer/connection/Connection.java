package de.rub.nds.virtualnetworklayer.connection;

import de.rub.nds.virtualnetworklayer.packet.Packet;

import java.io.Closeable;
import java.io.IOException;

public interface Connection extends Closeable {

    public static abstract class Trace<T extends Packet> implements Iterable<T> {

        public abstract T get(int position);

        public abstract int size();

    }

    public int available() throws IOException;

    public Packet read(int timeout) throws IOException;

    public Packet write(byte[] data) throws IOException;

    public Trace getTrace();

    public void close();
}