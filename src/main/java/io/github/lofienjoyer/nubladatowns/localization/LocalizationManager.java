package io.github.lofienjoyer.nubladatowns.localization;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {

    private Map<String, Component> messages;

    public LocalizationManager() {
        reloadConfig();
    }

    public void reloadConfig() {
        this.messages = loadMessages(NubladaTowns.getInstance());
    }

    private Map<String, Component> loadMessages(NubladaTowns instance) {
        var messagesFile = new File(instance.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            instance.saveResource("messages.yml", false);
        }

        var mm = MiniMessage.miniMessage();
        var messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        var defaultMessagesConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(instance.getResource("messages.yml")));

        var messagesMap = new HashMap<String, Component>();
        for (String key : defaultMessagesConfig.getKeys(true)) {
            var message = messagesConfig.getString(key, defaultMessagesConfig.getString(key));
            messagesConfig.set(key, message);
            messagesMap.put(key, mm.deserialize(message));
        }
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messagesMap;
    }

    public Component getMessage(String key) {
        return getMessage(key, false);
    }

    public Component getMessage(String key, boolean prefix) {
        if (prefix) {
            return messages.get("prefix").append(messages.get(key));
        } else {
            return messages.get(key);
        }
    }

}
