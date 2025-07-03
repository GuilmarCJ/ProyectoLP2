package com.turestaurante.reservas.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            // 1. Cargar el archivo de configuración
            InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream("firebase-config.json");

            if (serviceAccount == null) {
                throw new RuntimeException("❌ No se encontró firebase-config.json en resources/");
            }

            // 2. Configurar Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            // 3. Inicializar Firebase
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase inicializado correctamente");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            throw new RuntimeException("Error crítico: No se pudo inicializar Firebase");
        }
    }
}
