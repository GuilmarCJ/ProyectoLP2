/*package com.turestaurante.reservas.controller;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test-firebase")
    public String testFirebase() {
        try {
            FirebaseAuth.getInstance().getUser("test-user-id");
            return "✅ Conexión a Firebase funcionando";
        } catch (Exception e) {
            return "❌ Error en Firebase: " + e.getMessage();
        }
    }
}*/