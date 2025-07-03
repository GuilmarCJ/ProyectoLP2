package com.turestaurante.reservas.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import com.google.firebase.cloud.FirestoreClient;
import com.turestaurante.reservas.model.Reserva;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Controller

@SessionAttributes("correo")
public class ReservaController {

    @GetMapping("/reservar")
    public String mostrarFormulario() {
        return "reservar";
    }

    @PostMapping("/reservar")
    public String guardarReserva(@RequestParam String nombre,
                                 @RequestParam String correo,
                                 @RequestParam String fecha,
                                 @RequestParam String hora,
                                 @RequestParam int personas,
                                 Model model) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Validar si ya existe una reserva activa
            ApiFuture<QuerySnapshot> future = db.collection("reservas")
                .whereEqualTo("email", correo)
                .whereNotEqualTo("estado", "cancelado")  // Solo considera las activas
                .get();

            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            if (!docs.isEmpty()) {
                model.addAttribute("error", "Ya tienes una reserva activa. Cancélala antes de crear una nueva.");
                return "reservar";
            }

            // Si no tiene reservas activas, crear la reserva
            Map<String, Object> reserva = new HashMap<>();
            reserva.put("nombre", nombre);
            reserva.put("email", correo);
            reserva.put("fecha", fecha);
            reserva.put("hora", hora);
            reserva.put("personas", personas);
            reserva.put("estado", "en espera");

            db.collection("reservas").add(reserva).get(); // esperar confirmación
            return "redirect:/inicio?email=" + correo;

        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar la reserva: " + e.getMessage());
            return "reservar";
        }
    }


    
    
    @GetMapping("/inicio")
    public String mostrarInicioCliente(@RequestParam String email, Model model) {
        model.addAttribute("correo", email); // Guarda en la sesión
        try {
            Firestore db = FirestoreClient.getFirestore();
            List<Reserva> misReservas = new ArrayList<>();

            ApiFuture<QuerySnapshot> future = db.collection("reservas")
                .whereEqualTo("email", email)
                .get();

            for (DocumentSnapshot doc : future.get().getDocuments()) {
                Reserva r = new Reserva();
                r.setId(doc.getId());
                r.setCorreo(doc.getString("email"));
                r.setNombre(doc.getString("nombre"));
                r.setFecha(doc.getString("fecha"));
                r.setHora(doc.getString("hora"));
                r.setPersonas(doc.getLong("personas") != null ? doc.getLong("personas").intValue() : 0);
                r.setEstado(doc.getString("estado"));
                misReservas.add(r);
            }

            model.addAttribute("misReservas", misReservas);
            return "inicio";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudieron cargar tus reservas");
            return "inicio";
        }
    }


    
    @PostMapping("/mis-reservas/cancelar")
    public String cancelarReservaCliente(@RequestParam String id, @RequestParam String email) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> data = new HashMap<>();
            data.put("estado", "cancelado");
            db.collection("reservas").document(id).update(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/inicio?email=" + email;
    }


    

}