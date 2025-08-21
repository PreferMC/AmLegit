package space.commandf1.amlegit.check.impl.packet.badpacket;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import space.commandf1.amlegit.check.defaults.AlertDescription;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.config.check.DefaultDisableCheck;
import space.commandf1.amlegit.data.PlayerData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@DefaultDisableCheck
public class BadPacketsC extends Check {

    @CheckConfigHandler(name = "max-delayed")
    @AlertDescription(name = "MaxDelayed")
    /* 若每秒62个transaction，最高允许30秒延迟；对于每个玩家，最大消耗约206K内存 */
    private int maxDelayed = 30 * 62;

    private final Map<PlayerData, LRUCache<Short>> transactions = new ConcurrentHashMap<>();

    public BadPacketsC(Plugin plugin) {
        super("BadPackets", 10, "TransactionOrder", "C", plugin);
        this.addAllowedPackets(PacketType.Play.Client.WINDOW_CONFIRMATION, PacketType.Play.Server.WINDOW_CONFIRMATION);
    }

    private LRUCache<Short> getCache(@NotNull CheckHandler handler) {
        return transactions.computeIfAbsent(
                handler.getPlayerData(),
                k -> new LRUCache<>(this.maxDelayed)
        );
    }

    @Override
    public void onCheck(@NotNull CheckHandler handler) {
        if (handler.getEvent() instanceof PacketSendEvent event
                && event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            LRUCache<Short> cache = getCache(handler);
            WrapperPlayServerWindowConfirmation packet = new WrapperPlayServerWindowConfirmation(event);
            cache.put(packet.getActionId());
        }

        if (handler.getEvent() instanceof PacketReceiveEvent event
                && event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            LRUCache<Short> cache = getCache(handler);
            short actionId = new WrapperPlayClientWindowConfirmation(event).getActionId();
            short expected = cache.getLeastUsed();
            if (expected == actionId) {
                cache.remove(expected);
                return;
            }

            if (!cache.contains(actionId)) {
                // invalid packet
                handler.alert("Invalid Transaction");
                handler.punish("Invalid Transaction");
                return;
            }

            handler.fail();
            // fix the order
            do {
                cache.remove(cache.getLeastUsed());
            } while (cache.getLeastUsed() == actionId);
        }
    }

    public static final class LRUCache<E> {
        private final int sizeLimit;
        private int size;
        private Element<E> head;
        private Element<E> tail;
        private final Map<E, Element<E>> map;

        public LRUCache(int sizeLimit) {
            this.sizeLimit = sizeLimit;
            this.map = new ConcurrentHashMap<>();
        }

        private void moveToTheHead(@NotNull Element<E> var1) {
            if (var1 != this.head) {
                if (var1 == this.tail) {
                    this.tail = var1.prev;
                    this.tail.next = this.tail;
                } else {
                    var1.next.prev = var1.prev;
                    var1.prev.next = var1.next;
                }

                var1.prev = var1;
                var1.next = this.head;
                this.head.prev = var1;
                this.head = var1;
            }
        }

        private void addToTheHead(E var1) {
            if (this.size == 0) {
                this.head = this.tail = new Element<>(var1);
                this.map.put(var1, this.head);
                this.head.prev = this.head;
                this.tail.next = this.tail;
                ++this.size;
            } else if (this.size < this.sizeLimit) {
                var var2 = new Element<E>(var1);
                this.map.put(var1, var2);
                var2.prev = var2;
                var2.next = this.head;
                this.head.prev = var2;
                this.head = var2;
                ++this.size;
            } else {
                this.map.put(var1, this.tail);
                this.map.remove(this.tail.data);
                this.tail.data = var1;
                this.moveToTheHead(this.tail);
            }

        }

        public synchronized boolean contains(E var1) {
            return this.map.containsKey(var1);
        }

        public synchronized void put(E object) {
            Element<E> element = this.map.get(object);
            if (element != null) {
                this.moveToTheHead(element);
            } else {
                this.addToTheHead(object);
            }
        }

        public synchronized void remove(E object) {
            Element<E> element = this.map.remove(object);
            if (element == null) {
                return;
            }
            element.prev.next = element.next;
            element.next.prev = element.prev;

            if (--size == 0) {
                head = null;
                tail = null;
                return;
            }

            if (element == head) {
                head = element.next;
            }
            if (element == tail) {
                tail = element.prev;
            }
        }

        public synchronized E getLeastUsed() {
            return this.tail.data;
        }

        public synchronized E getPrevUsed() {
            return this.head.data;
        }

        public synchronized void clear() {
            size = 0;
            head = null;
            tail = null;
            map.clear();
        }

        public synchronized int size() {
            return size;
        }

        public synchronized boolean isEmpty() {
            return size == 0;
        }

        public static final class Element<E> {
            public E data;
            Element<E> next;
            Element<E> prev;

            public Element(E var1) {
                this.data = var1;
                this.next = null;
                this.prev = null;
            }
        }
    }
}
