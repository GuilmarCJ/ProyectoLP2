package com.turestaurante.reservas.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @GetMapping("/")
    public String paginaPrincipal() {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
        @RequestParam String email,
        @RequestParam String password,
        Model model
    ) {
        try {
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            model.addAttribute("usuario", user.getDisplayName() != null ? user.getDisplayName() : email);
            return "redirect:/inicio?email=" + user.getEmail(); // Cambiado a email
        } catch (Exception e) {
            model.addAttribute("error", "❌ Usuario no encontrado o error en Firebase.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam String nombre,
        Model model
    ) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(nombre);

            FirebaseAuth.getInstance().createUser(request);

            model.addAttribute("mensaje", "✅ Registro exitoso. Ahora puedes iniciar sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error al registrarse: " + e.getMessage());
            return "register";
        }
    }
}