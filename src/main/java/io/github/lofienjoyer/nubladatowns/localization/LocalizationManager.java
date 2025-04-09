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
import java.util.Objects;

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
        
        // Asegurarse de que el directorio de datos existe
        if (!instance.getDataFolder().exists()) {
            instance.getDataFolder().mkdirs();
        }
        
        // Copiar el archivo predeterminado si no existe
        if (!messagesFile.exists()) {
            try {
                instance.saveResource("messages.yml", false);
            } catch (Exception e) {
                instance.getLogger().severe("Failed to save default messages.yml file: " + e.getMessage());
                throw new RuntimeException("Failed to save default messages.yml file", e);
            }
        }

        var mm = MiniMessage.miniMessage();
        var messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Cargar el archivo predeterminado del jar
        var defaultMessagesConfig = YamlConfiguration.loadConfiguration(
            new InputStreamReader(Objects.requireNonNull(instance.getResource("messages.yml")))
        );

        var messagesMap = new HashMap<String, Component>();
        for (String key : defaultMessagesConfig.getKeys(true)) {
            var message = messagesConfig.getString(key, defaultMessagesConfig.getString(key));
            if (message == null) {
                instance.getLogger().warning("Missing message key: " + key);
                continue;
            }
            messagesConfig.set(key, message);
            try {
                messagesMap.put(key, mm.deserialize(message));
            } catch (Exception e) {
                instance.getLogger().warning("Failed to parse message for key " + key + ": " + e.getMessage());
            }
        }
        
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            instance.getLogger().severe("Failed to save messages.yml: " + e.getMessage());
            throw new RuntimeException("Failed to save messages.yml", e);
        }
        
        return messagesMap;
    }

    public Component getMessage(String key) {
        return getMessage(key, false);
    }

    public Component getMessage(String key, boolean prefix) {
        if (prefix) {
            var prefixMessage = messages.get("prefix");
            var message = messages.get(key);
            if (prefixMessage == null || message == null) {
                return Component.text("Missing message: " + key);
            }
            return prefixMessage.append(message);
        } else {
            var message = messages.get(key);
            return message != null ? message : Component.text("Missing message: " + key);
        }
    }
}
