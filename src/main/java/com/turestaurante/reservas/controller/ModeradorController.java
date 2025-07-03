package com.turestaurante.reservas.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


//@Controller

public class ModeradorController {

    // Login GET
    @GetMapping("/admin-login")
    public String loginModeradorForm() {
        return "admin-login";
    }

    // Login POST
    @PostMapping("/admin-login")
    public String loginModeradorFirebase(
            @RequestParam String username,
            @RequestParam String password,
            Model model
    ) {
        try {
            // 1. Usamos Firebase REST API para autenticar
            String idToken = autenticarConFirebase(username, password);
            if (idToken == null) {
                model.addAttribute("error", "❌ Usuario o contraseña incorrectos.");
                return "admin-login";
            }

            // 2. Verificamos que sea moderador en Firestore
            Firestore db = FirestoreClient.getFirestore();
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(username);

            DocumentSnapshot snapshot = db.collection("usuarios").document(user.getUid()).get().get();

            if (snapshot.exists() && "moderador".equals(snapshot.getString("rol"))) {
                // 3. Cargar reservas
                ApiFuture<QuerySnapshot> future = db.collection("reservas").get();
                List<QueryDocumentSnapshot> documentos = future.get().getDocuments();

                List<com.turestaurante.reservas.model.Reserva> reservas = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documentos) {
                    com.turestaurante.reservas.model.Reserva reserva = new com.turestaurante.reservas.model.Reserva();
                    reserva.setId(doc.getId());
                    reserva.setNombre((String) doc.getString("nombre"));
                    reserva.setCorreo((String) doc.getString("email"));
                    reserva.setFecha((String) doc.getString("fecha"));
                    reserva.setHora((String) doc.getString("hora"));
                    reserva.setPersonas(doc.getLong("personas") != null ? doc.getLong("personas").intValue() : 0);
                    reserva.setEstado(doc.contains("estado") ? doc.getString("estado") : "en espera");
                    reservas.add(reserva);
                }
                model.addAttribute("reservas", reservas);

                
                return "admin-reservas";
            } else {
                model.addAttribute("error", "❌ No tienes permisos de moderador.");
                return "admin-login";
            }

        } catch (Exception e) {
            model.addAttribute("error", "❌ Error: " + e.getMessage());
            return "admin-login";
        }
    }

    
    //Autenticador
    private String autenticarConFirebase(String email, String password) {
        try {
            String apiKey = "AIzaSyBYfe8pAQo9Tr6FD6apnxjs9f-WvEq164k"; // Reemplaza esto con tu API Key de Firebase
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

            String jsonPayload = "{"
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + password + "\","
                    + "\"returnSecureToken\":true"
                    + "}";

            java.net.URL endpoint = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) endpoint.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Extraer idToken
                String responseStr = response.toString();
                if (responseStr.contains("idToken")) {
                    int start = responseStr.indexOf("idToken") + 10;
                    int end = responseStr.indexOf("\"", start);
                    return responseStr.substring(start, end);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en autenticación Firebase REST: " + e.getMessage());
        }
        return null;
    }

    
    @PostMapping("/admin/reservas/eliminar")
    public String eliminarReserva(@RequestParam String id, Model model) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("reservas").document(id).delete();

            // Volver a cargar la lista de reservas como objetos Reserva
            List<QueryDocumentSnapshot> documentos = db.collection("reservas").get().get().getDocuments();
            List<com.turestaurante.reservas.model.Reserva> reservas = new ArrayList<>();

            for (QueryDocumentSnapshot doc : documentos) {
                com.turestaurante.reservas.model.Reserva reserva = new com.turestaurante.reservas.model.Reserva();
                reserva.setId(doc.getId());
                reserva.setNombre(doc.getString("nombre"));
                reserva.setCorreo(doc.getString("email"));
                reserva.setFecha(doc.getString("fecha"));
                reserva.setHora(doc.getString("hora"));
                reserva.setPersonas(doc.getLong("personas") != null ? doc.getLong("personas").intValue() : 0);
                reserva.setEstado(doc.contains("estado") ? doc.getString("estado") : "en espera");
                reservas.add(reserva);
            }

            model.addAttribute("reservas", reservas);
            return "admin-reservas";

        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar: " + e.getMessage());
            return "admin-reservas";
        }
    }


  

    @PostMapping("/admin/reservas/cambiar-estado")
    public String cambiarEstadoReserva(@RequestParam String id,
                                       @RequestParam String estadoActual,
                                       Model model) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Determinar el siguiente estado
            String nuevoEstado;
            switch (estadoActual) {
                case "en espera":
                    nuevoEstado = "recibido";
                    break;
                case "recibido":
                    nuevoEstado = "cancelado";
                    break;
                default:
                    nuevoEstado = "en espera";
            }

            // Actualizar estado en Firestore
            DocumentReference docRef = db.collection("reservas").document(id);
            Map<String, Object> update = new HashMap<>();
            update.put("estado", nuevoEstado);
            docRef.update(update);

            List<QueryDocumentSnapshot> documentos = db.collection("reservas").get().get().getDocuments();
            List<com.turestaurante.reservas.model.Reserva> reservas = new ArrayList<>();

            for (QueryDocumentSnapshot doc : documentos) {
                com.turestaurante.reservas.model.Reserva reserva = new com.turestaurante.reservas.model.Reserva();
                reserva.setId(doc.getId());
                reserva.setNombre(doc.getString("nombre"));
                reserva.setCorreo(doc.getString("email"));
                reserva.setFecha(doc.getString("fecha"));
                reserva.setHora(doc.getString("hora"));
                reserva.setPersonas(doc.getLong("personas") != null ? doc.getLong("personas").intValue() : 0);
                reserva.setEstado(doc.contains("estado") ? doc.getString("estado") : "en espera");
                reservas.add(reserva);
            }

            model.addAttribute("reservas", reservas);
            return "admin-reservas";


        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar estado: " + e.getMessage());
            return "admin-reservas";
        }
    }

    
    //usuario moderador
    @GetMapping("/crear-moderador")
    @ResponseBody
    public String crearModerador() {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("moderador@email.com")
                .setPassword("mod123456")
                .setDisplayName("Moderador");

            UserRecord user = FirebaseAuth.getInstance().createUser(request);

            // Guardamos rol en Firestore
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> data = new HashMap<>();
            data.put("rol", "moderador");
            db.collection("usuarios").document(user.getUid()).set(data);

            return "✅ Moderador creado: " + user.getEmail();
        } catch (Exception e) {
            return "❌ Error al crear moderador: " + e.getMessage();
        }
    }

    
}
