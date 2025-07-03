package com.turestaurante.reservas.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.turestaurante.reservas.model.Reserva;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FirebaseReservaService {

    private static final String COLLECTION_NAME = "reservas";

    public List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                Reserva reserva = doc.toObject(Reserva.class);
                reserva.setId(doc.getId());
                reservas.add(reserva);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservas;
    }

    public void eliminarReserva(String id) {
        try {
            FirestoreClient.getFirestore().collection(COLLECTION_NAME).document(id).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarEstadoReserva(String id, String nuevoEstado) {
        try {
            FirestoreClient.getFirestore().collection(COLLECTION_NAME).document(id)
                    .update("estado", nuevoEstado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
