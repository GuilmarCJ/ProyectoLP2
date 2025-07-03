package com.turestaurante.reservas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String home() {
        return "🚀 Proyecto desplegado correctamente en Azure Web App";
    }
}
