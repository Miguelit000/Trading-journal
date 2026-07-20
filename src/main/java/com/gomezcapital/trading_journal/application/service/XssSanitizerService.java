package com.gomezcapital.trading_journal.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XssSanitizerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sanitizeEditorJsJson(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return rawJson;
        }

        try {
            // 1. Convertimos el String a un Árbol JSON
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode blocks = root.path("blocks");

            // 2. Iteramos solo por los bloques de Editor.js
            if (blocks.isArray()) {
                for (JsonNode block : blocks) {
                    JsonNode data = block.path("data");
                    // 3. Si el bloque tiene un campo "text", lo sanitizamos
                    if (data.has("text")) {
                        String unsafeText = data.get("text").asText();
                        // Safelist.relaxed() permite negritas, listas y enlaces, pero ELIMINA scripts y onclicks
                        String safeText = Jsoup.clean(unsafeText, Safelist.relaxed());
                        ((ObjectNode) data).put("text", safeText);
                    }
                }
            }
            // 4. Devolvemos el JSON seguro
            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Error sanitizando el JSON del editor. Aplicando limpieza estricta de emergencia.", e);
            // Si el JSON falla, aplicamos una limpieza agresiva a todo el texto por seguridad
            return Jsoup.clean(rawJson, Safelist.basic());
        }
    }
}