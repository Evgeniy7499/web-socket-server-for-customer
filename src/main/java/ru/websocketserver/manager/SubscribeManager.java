package ru.websocketserver.manager;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.websocketserver.domain.Message;
import ru.websocketserver.service.Person;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscribeManager {

    private final Map<String, Map<String, Person>> personsByMacDevice = new ConcurrentHashMap<>();

    public void subscribe(Person person) {
        person.getPanels()
                .forEach(mac -> subscribePersonOnDevice(mac, person));
    }

    public void unsubscribePersonForAll(Person person) {
        String sessionId = person.getSession().getId();
        person.getPanels().forEach(panel -> {
            Map<String, Person> subscribers = personsByMacDevice.get(panel);
            subscribers.remove(sessionId);
        });
    }

    public void sendAllMessageDeviceSubscribers(String macDevice, Message message) {
        if (personsByMacDevice.containsKey(macDevice)) {
            List<Person> subscribers = personsByMacDevice.get(macDevice).values().stream().toList();
            subscribers.forEach(person -> person.sendMessage(message));
        }
    }

    private void subscribePersonOnDevice(String mac, Person person) {
        Map<String, Person> subscribers;
        if (personsByMacDevice.containsKey(mac)) {
            subscribers = personsByMacDevice.get(mac);
        } else {
            subscribers = new ConcurrentHashMap<>();
            personsByMacDevice.put(mac, subscribers);
        }
        subscribers.put(person.getSession().getId(), person);
    }

    public List<String> getEmptySubscriptionMac() {
        return personsByMacDevice.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
    }

    public void removeSubscription(@NonNull String key) {
        personsByMacDevice.remove(key);
    }

}