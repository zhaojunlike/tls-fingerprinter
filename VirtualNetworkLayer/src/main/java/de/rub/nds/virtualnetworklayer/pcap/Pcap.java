package de.rub.nds.virtualnetworklayer.pcap;

import de.rub.nds.virtualnetworklayer.connection.pcap.ConnectionHandler;
import de.rub.nds.virtualnetworklayer.pcap.structs.bpf_program;
import de.rub.nds.virtualnetworklayer.pcap.structs.pcap_dumper_t;
import de.rub.nds.virtualnetworklayer.pcap.structs.pcap_if;
import de.rub.nds.virtualnetworklayer.pcap.structs.pcap_t;
import de.rub.nds.virtualnetworklayer.util.Util;
import org.bridj.Pointer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Pcap wrapper
 * <p>
 * To create a new instance use one of the following factory methods:
 * <ul>
 * <li>live capturing: {@link #openLive()}, {@link #openLive(Device)}, {@link #openLive(Device, java.util.Set)}</li>
 * <li>opening an pcap dump: {@link #openOffline(java.io.File)}</li>
 * <li>opening standard input as pcap: {@link #openOfflineStdin()}</li>
 * <li>radio frequence monitoring: {@link #openRadioFrequencyMonitor()}, {@link #openRadioFrequencyMonitor(Device)}</li>
 * </ul>
 * The wrapper does reference counting, so an instance might be also looked up by
 * address {@link #getInstance(byte[])}.
 * If an instance was not closed or garbage collected, when the Java virtual machine is
 * shutting down, {@link Pcap.GarbageCollector} kicks in.
 * <p>
 * Register a callback {@link PcapHandler} with {@link #loopAsynchronous(PcapHandler)} or {@link #loop(PcapHandler)}.
 *
 * @author Marco Faltermeier <faltermeier@me.com>
 * @see Runtime#addShutdownHook(Thread)
 * @see PcapLibrary
 */
public class Pcap {
    private static Pointer<Byte> errbuf = Pointer.allocateBytes(256);
    private Pointer<Integer> pcap_datalink = Pointer.allocateInt();
    private pcap_t pcap_t;
    private Status status = Status.Success;
    private Loop loop;
    private File file;
    private Device device;
    private int referenceCount = 0;
    private int referencePosition = 0;
    private String filter = "";
    private List<WeakReference<PcapDumper>> dumperReferences = new LinkedList<>();

    private static int snaplen = 65535;
    private static int mode = 0;
    private static int timeout = 250;
    private static List<WeakReference<Pcap>> references = new LinkedList<>();
    private static Device liveDevice;

    private class Loop implements Runnable {
        private PcapHandler handler;
        private boolean asynchronous;

        private Loop(PcapHandler handler, boolean asynchronous) {
            this.handler = handler;
            this.asynchronous = asynchronous;
        }

        @Override
        public void run() {
            setStatus(PcapLibrary.pcap_loop(pcap_t, 0, Pointer.pointerTo(handler), pcap_datalink));
        }

        public PcapHandler getHandler() {
            return handler;
        }
    }

    private static class GarbageCollector implements Runnable {
        @Override
        public void run() {
            for (WeakReference<Pcap> reference : new LinkedList<>(references)) {
                Pcap instance = reference.get();
                if (instance != null) {
                    try {
                        Thread.sleep(10L);
                        instance.finalize();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new GarbageCollector()));
    }

    public enum Status {
        Success(0),
        Failure(-1),
        Terminated(-2),
        NotActivated(-3),
        AlreadyActivated(-4),
        NoSuchDevice(-5),
        RFMonNotSupported(-6);

        private int code;

        private Status(int code) {
            this.code = code;
        }

        public static Status valueOf(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }

            return null;
        }
    }

    public enum OpenFlag {
        Promiscuous(1),
        DataxUdp(2),
        NoCaptureRPcap(4),
        NoCaptureLocal(8),
        MaxResponsiveness(16);

        private int position;

        private OpenFlag(int position) {
            this.position = position;
        }
    }

    /**
     * @see <a href="http://www.tcpdump.org/linktypes.html">tcpdump.org/linktypes.html</a>
     */
    public enum DataLinkType {
        Null(0),
        Ethernet(1),
        PPP(9),
        PPPoE(51),
        Raw(101),
        IEEE802_11(105),
        Sll(113),
        PfLog(117),
        Prism(119),
        Radiotap(127),
        Avs(163);

        private int id;

        private DataLinkType(int id) {
            this.id = id;
        }

        public static DataLinkType valueOf(int id) {
            for (DataLinkType dlt : values()) {
                if (dlt.id == id) {
                    return dlt;
                }
            }

            return null;
        }
    }

    /**
     * Creates a pcap instance in radio frequency mode with {@link #getLiveDevice} and
     * default {@link OpenFlag} (all false).
     *
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if device was not found or device does not support rf mode.
     */
    public static Pcap openRadioFrequencyMonitor() {
        Device liveDevice = getLiveDevice();

        if (liveDevice == null) {
            throw new IllegalArgumentException("no live device found");
        }

        return openRadioFrequencyMonitor(liveDevice);
    }

    /**
     * Creates a pcap instance in radio frequency mode with specified {@link Device} and
     * default {@link OpenFlag} (all false).
     *
     * @param device
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if device was not found or device does not support rf mode.
     */
    public static Pcap openRadioFrequencyMonitor(Device device) {
        pcap_t pcap_t = PcapLibrary.pcap_create(Pointer.pointerToCString(device.getName()), errbuf);

        if (pcap_t == null) {
            throw new IllegalArgumentException(errbuf.getCString());
        }

        if (Status.valueOf(PcapLibrary.pcap_set_rfmon(pcap_t, 1)) != Status.Success) {
            throw new IllegalArgumentException();
        }

        PcapLibrary.pcap_set_snaplen(pcap_t, snaplen);
        PcapLibrary.pcap_set_promisc(pcap_t, mode);
        PcapLibrary.pcap_set_timeout(pcap_t, timeout);

        PcapLibrary.pcap_activate(pcap_t);

        return new Pcap(pcap_t, device);
    }

    /**
     * Creates a pcap instance in live mode with specified {@link Device}
     * and {@link OpenFlag}.
     *
     * @param device
     * @param flags
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if device was not found
     */
    public static Pcap openLive(Device device, Set<OpenFlag> flags) {
        for (OpenFlag flag : flags) {
            mode += 1 << (flag.position - 1);
        }

        return openLive(device);
    }

    /**
     * Creates a pcap instance in live mode with specified {@link Device} and
     * default {@link OpenFlag} (all false).
     *
     * @param device
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if device was not found
     */
    public static Pcap openLive(Device device) {
        pcap_t pcap_t = PcapLibrary.pcap_open_live(Pointer.pointerToCString(device.getName()), snaplen, mode, timeout, errbuf);

        if (pcap_t == null) {
            throw new IllegalArgumentException(errbuf.getCString());
        }

        return new Pcap(pcap_t, device);
    }

    /**
     * Creates a pcap instance in live mode with {@link #getLiveDevice} device and
     * default {@link OpenFlag} (all false).
     *
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if device was not found
     */
    public static Pcap openLive() {
        Device liveDevice = getLiveDevice();

        if (liveDevice == null) {
            throw new IllegalArgumentException("no live device found");
        }
        
        return openLive(liveDevice);
    }

    /**
     * Creates a pcap instance in offline mode with specified {@link File}.
     *
     * @param file pcap dump
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException if file could not be opened
     */
    public static Pcap openOffline(File file) {
        pcap_t pcap_t = PcapLibrary.pcap_open_offline(Pointer.pointerToCString(file.getAbsolutePath()), errbuf);

        if (pcap_t == null) {
            throw new IllegalArgumentException(errbuf.getCString());
        }

        return new Pcap(pcap_t, file);
    }

    /**
     * Create a pcap instance in offline mode reading from Standard Input
     * @return instance of {@link Pcap}
     * @throws IllegalArgumentException on failure
     */
    public static Pcap openOfflineStdin() {
        pcap_t pcap_t = PcapLibrary.pcap_open_offline(
                Pointer.pointerToCString("-"), errbuf);
        if(pcap_t == null) {
            throw new IllegalArgumentException(errbuf.getCString());
        }

        return new Pcap(pcap_t);
    }

    /**
     * Looks up a pcap instance by address,
     * if none was found a new pcap instance is created.
     *
     * @param address device address
     * @return pcap instance
     * @throws IllegalArgumentException if none device is bound to address
     */
    public static Pcap getInstance(byte[] address) {
        for (WeakReference<Pcap> reference : references) {
            Pcap instance = reference.get();

            if (instance != null && instance.getDevice() != null && instance.getDevice().isBound(address)
                    && instance.filter.isEmpty() && instance.getHandler() instanceof ConnectionHandler) {
                instance.referenceCount++;

                return instance;
            }
        }

        for (Device device : Pcap.getDevices()) {
            if (device.isBound(address)) {
                return Pcap.openLive(device);
            }
        }

        return Pcap.openLive();
    }
    
    private static Device getDeviceForHost(String host) {
        String defaultRoute = Util.getDefaultRoute(host);

        System.err.println("default route is " + defaultRoute);
        for (Device device : Pcap.getDevices()) {
            if (device.getName().equals(defaultRoute)) {
                return device;
            }
        }
        
        throw new InternalError("could not find a Device for " + host + " route was " + defaultRoute);
    }
    
    public static Pcap getInstanceForRemoteHost(String host) {
    	Device d = getDeviceForHost(host);
    	System.err.println("trying to get a device for host " + host + " and found device " + d.toString());
    	for (WeakReference<Pcap> reference : references) {
            Pcap instance = reference.get();

            if (instance != null && instance.getDevice() != null && instance.getDevice().equals(d)
                    && instance.filter.isEmpty() && instance.getHandler() instanceof ConnectionHandler) {
                instance.referenceCount++;

                return instance;
            }
        }
    	
    	return Pcap.openLive(d);
    	

    }

    private Pcap(pcap_t pcap_t) {
        this.pcap_t = pcap_t;
        pcap_datalink.set(PcapLibrary.pcap_datalink(pcap_t));

        referencePosition = references.size();
        references.add(new WeakReference<>(this));
    }

    private Pcap(pcap_t pcap_t, Device device) {
        this(pcap_t);

        this.device = device;
    }

    private Pcap(pcap_t pcap_t, File file) {
        this(pcap_t);

        this.file = file;
    }

    /**
     * example: {@code libpcap version 1.1.1}
     *
     * @return version string
     */
    public static String getVersion() {
        return PcapLibrary.pcap_lib_version().getCString();
    }

    /**
     * Sets and activates a packet filter
     *
     * @param filter
     * @return {@link Status}
     * @see <a href="http://www.cs.ucr.edu/~marios/ethereal-tcpdump.pdf">cs.ucr.edu/~marios/ethereal-tcpdump.pdf</a>
     */
    public Status filter(String filter) {
        this.filter = filter;
        Pointer<bpf_program> bpf_program = Pointer.allocate(bpf_program.class);

        if (Status.valueOf(PcapLibrary.pcap_compile(pcap_t, bpf_program, Pointer.pointerToCString(filter), 0, 0)) == Status.Success) {
            return setStatus(PcapLibrary.pcap_setfilter(pcap_t, bpf_program));
        } else {
            throw new IllegalArgumentException("error while parsing " + filter);
        }
    }

    public void breakloop() {
        if (loop != null) {
            PcapLibrary.pcap_breakloop(pcap_t);

            while (loop.asynchronous && status != Status.Terminated) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        loop = null;
    }

    /**
     * Convenient method for {@link #loop(PcapHandler, boolean)}
     * {@code asynchronous = false}.
     *
     * @param handler
     * @return {@link Status}
     */
    public Status loop(PcapHandler handler) {
        return loop(handler, false);
    }

    /**
     * Convenient method for {@link #loop(PcapHandler, boolean)} with
     * {@code asynchronous = true}.
     *
     * @param handler
     * @return {@link Status}
     */
    public Status loopAsynchronous(PcapHandler handler) {
        return loop(handler, true);
    }

    public Status loop(final PcapHandler handler, boolean asynchronous) {
        if (loop != null) {
            return Status.AlreadyActivated;
        }

        loop = new Loop(handler, asynchronous);

        if (!asynchronous) {
            loop.run();
        } else {
            Thread thread = new Thread(loop);
            thread.setName("Pcap");
            thread.start();
        }

        referenceCount++;

        return status;
    }

    /**
     * Gets the live device by choosing the device for the route to 8.8.8.8
     * which is usually the default route.
     * 
     * @return The device with the default route.
     */
    public static Device getLiveDevice() {
    	return getLiveDevice("8.8.8.8");
    }
    
    /**
     * Tries to find live device.
     *
     * @return found device otherwise default device
     * @see InetAddress#getLocalHost()
     * @see #getDefaultDevice()
     * @see de.rub.nds.virtualnetworklayer.util.Util#getDefaultRoute(String)
     */
    public static Device getLiveDevice(String host) {
        if (liveDevice != null) {
            return liveDevice;
        }

        final NetworkInterface defaultInterface = Util.getDefaultDevice(host);
        List<byte[]> defaultInterfaceAddresses = new LinkedList<>();
        final Enumeration<InetAddress> _addresses = defaultInterface.getInetAddresses();
        while(_addresses.hasMoreElements())
            defaultInterfaceAddresses.add(_addresses.nextElement().getAddress());

        System.err.println("default route is: " + defaultInterface);
        for (Device device : Pcap.getDevices()) {
            for(final byte[] deviceAddress : device.getAddresses())
            for(final byte[] defaultInterfaceAddress : defaultInterfaceAddresses) {
                if (Arrays.equals(deviceAddress, defaultInterfaceAddress)) {
                    // found a match of "bound" addresses
                    return device;
                }
            }
        }
        System.err.println("did not find a matching device");

        return getDefaultDevice();
    }

    public static void setLiveDevice(Device liveDevice) {
        Pcap.liveDevice = liveDevice;
    }

    /**
     * Returns the first device pcap can find (if any) except the loopback device.
     *
     * @return device otherwise null
     */
    public static Device getDefaultDevice() {
        Pointer<Byte> dev = PcapLibrary.pcap_lookupdev(errbuf);
        if(dev == null) {
            throw new IllegalArgumentException(errbuf.getCString());
        }
        String name = dev.getCString();

        for (Device device : getDevices()) {
            if (device.getName().equals(name)) {
                return device;
            }
        }

        return null;
    }

    /**
     * @return a list of all {@link Device} from this host
     */
    public static List<Device> getDevices() {
        Pointer<Pointer<pcap_if>> pcap_if = Pointer.allocatePointer(pcap_if.class);
        List<Device> devices = new ArrayList<Device>();

        try {
            if (Status.valueOf(PcapLibrary.pcap_findalldevs(pcap_if, errbuf)) == Status.Success) {
                Pointer<pcap_if> device = pcap_if.get();

                while (device != Pointer.NULL) {
                    Pointer<Integer> netp = Pointer.allocateInt();
                    Pointer<Integer> maskp = Pointer.allocateInt();

                    int net = 0;
                    int mask = 0;

                    if (Status.valueOf(PcapLibrary.pcap_lookupnet(device.get().name(), netp, maskp, errbuf)) == Status.Success) {
                        net = netp.getInt();
                        mask = maskp.getInt();
                    } else {
                        // errbuf.getCString() has error message
                    }

                    netp.release();
                    maskp.release();

                    devices.add(new Device(device.get(), net, mask));

                    device = device.get().next();
                }

            } else {
                throw new IllegalArgumentException(errbuf.getCString());
            }
        } finally {
            PcapLibrary.pcap_freealldevs(pcap_if.get());
        }

        return devices;
    }

    private Status setStatus(int code) {
        this.status = Status.valueOf(code);
        return this.status;
    }

    /**
     * Open a file to which to write packets
     *
     * @param pathname Name of the file to open.
     * @return A {@link PcapDumper} suitable to dump packets
     */
    public PcapDumper openDump(File pathname) {
        pcap_dumper_t dumper = PcapLibrary.pcap_dump_open(pcap_t,
                Pointer.pointerToCString(pathname.getAbsolutePath()));

        if(dumper == null)
            throw new IllegalArgumentException(getLastError());
        PcapDumper pcapDumper = new PcapDumper(dumper);

        dumperReferences.add(new WeakReference<PcapDumper>(pcapDumper));
        return pcapDumper;
    }

    /**
     * @return The last occurred error. Use for functions without errbuf parameter.
     */
    public String getLastError() {
        Pointer<Byte> err = PcapLibrary.pcap_geterr(pcap_t);
        if(err == null)
            return null;

        return err.getCString();
    }

    /**
     * Returns last pcap status.
     *
     * @return status
     * @see Status
     */
    public Status getStatus() {
        return status;
    }

    public PcapHandler getHandler() {
        return loop.getHandler();
    }

    public File getFile() {
        return file;
    }

    public Device getDevice() {
        return device;
    }

    public static void setSnaplen(int snaplen) {
        Pcap.snaplen = snaplen;
    }

    public static void setTimeout(int timeout) {
        Pcap.timeout = timeout;
    }

    public void close() {
        referenceCount--;

        if (referenceCount <= 0) {
            for (WeakReference<PcapDumper> reference : dumperReferences) {
                PcapDumper dumper = reference.get();
                if(dumper != null)
                    try {
                        dumper.close();
                    } catch (Throwable throwable) {
                        //
                    }
            }
            dumperReferences.clear();


            if (loop != null) {
                breakloop();
            }

            if (pcap_t != null) {
                PcapLibrary.pcap_close(pcap_t);
            }

            if (referencePosition < references.size()) {
                references.remove(referencePosition);
                //FIXME: invalidates referencePosition in all "later" instances
            }
        }
    }

    @Override
    protected void finalize() {
        referenceCount = 0;
        close();
    }

    @Override
    public String toString() {
        return getVersion();
    }
}
