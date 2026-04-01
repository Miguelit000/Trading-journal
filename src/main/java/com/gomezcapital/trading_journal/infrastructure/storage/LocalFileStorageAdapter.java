package com.gomezcapital.trading_journal.infrastructure.storage;

import com.gomezcapital.trading_journal.domain.ports.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component

public class LocalFileStorageAdapter implements StoragePort {

    // Carpeta donde se guardaran las imagenes
    private final String UPLOAD_DIR = "uploads/trades/";

    public LocalFileStorageAdapter() {

        //Si la carpeta no existe al iniciar la app la crea automaticamente
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
            log.info("Directorio de almacenamiento creado: {}", UPLOAD_DIR);
        }
    }

    @Override
    public String uploadTradeImage(String tradeId, MultipartFile file) {

        try{

            // Extraer la extension original (ej: .png, .jpg)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            // Crear un nombre unico para evitar que se sobreescriban
            String newFileName = tradeId + "_" + UUID.randomUUID().toString().substring(0,8) + extension;


            // Guardar fisicamente
            Path filePath = Paths.get(UPLOAD_DIR + newFileName);
            Files.write(filePath, file.getBytes());

            log.info("Imagen guardada exitosamente en: {}", filePath);

            // Devolver la ruta relativa
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error al guardar la imagen del trade {}", tradeId, e);
            throw new RuntimeException("No se pudo procesar la subida de la imagen.");
        }
    }

    @Override
    public Resource loadTradeImage(String filename) {

        try {

            // Ruta exacta del archivo
            Path file = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            // verificar si el archivo existe y si tenemos permiso
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                log.error("No se pudo leer el archivo: {}", filename);
                throw new RuntimeException("No se pudo leer el archivo: " + filename);   
            }
        
        } catch (Exception e) {
            log.error("Error al recuperar la imagen: {}", filename, e);
            throw new RuntimeException("Error al recuperar la imagen", e);
        } 
    }


    
}
