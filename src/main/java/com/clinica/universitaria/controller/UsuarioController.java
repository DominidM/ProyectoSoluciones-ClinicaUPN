package com.clinica.universitaria.controller;

import com.clinica.universitaria.dto.UsuarioFormDTO;
import com.clinica.universitaria.model.NombreRol;
import com.clinica.universitaria.service.RolService;
import com.clinica.universitaria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;

    @GetMapping("/admin/usuarios")
    public String index(@RequestParam(required = false) String q,
                        @RequestParam(required = false) NombreRol rol,
                        @RequestParam(required = false) Boolean estado,
                        Model model) {
        model.addAttribute("pageTitle", "Gestión de Usuarios");
        model.addAttribute("pageDescription", "Gestiona todos los usuarios del sistema.");
        model.addAttribute("usuarios", usuarioService.listar(q, rol, estado));
        model.addAttribute("roles", rolService.listarActivos());
        model.addAttribute("filtroQ", q);
        model.addAttribute("filtroRol", rol);
        model.addAttribute("filtroEstado", estado);
        return "admin/usuarios/index";
    }

    @GetMapping("/admin/usuarios/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new UsuarioFormDTO(), "Nuevo usuario", "/admin/usuarios/guardar", false);
        return "admin/usuarios/form";
    }

    @PostMapping("/admin/usuarios/guardar")
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioFormDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Nuevo usuario", "/admin/usuarios/guardar", false);
            return "admin/usuarios/form";
        }
        try {
            usuarioService.crearDesdeFormulario(dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Usuario creado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Nuevo usuario", "/admin/usuarios/guardar", false);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/usuarios/form";
        }
    }

    @GetMapping("/admin/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var usuario = usuarioService.obtenerPorId(id);
        UsuarioFormDTO dto = new UsuarioFormDTO();
        dto.setDni(usuario.getDni());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setCorreo(usuario.getCorreo());
        dto.setTelefono(usuario.getTelefono());
        dto.setRol(usuario.getRol().getNombre());
        dto.setEstado(usuario.getEstado());
        cargarFormulario(model, dto, "Editar usuario", "/admin/usuarios/actualizar/" + id, true);
        model.addAttribute("usuarioId", id);
        return "admin/usuarios/form";
    }

    @PostMapping("/admin/usuarios/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("usuarioForm") UsuarioFormDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarFormulario(model, dto, "Editar usuario", "/admin/usuarios/actualizar/" + id, true);
            model.addAttribute("usuarioId", id);
            return "admin/usuarios/form";
        }
        try {
            usuarioService.actualizar(id, dto);
            redirectAttributes.addFlashAttribute("toastMensaje", "Usuario actualizado correctamente");
            redirectAttributes.addFlashAttribute("toastTipo", "success");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException ex) {
            cargarFormulario(model, dto, "Editar usuario", "/admin/usuarios/actualizar/" + id, true);
            model.addAttribute("usuarioId", id);
            model.addAttribute("errorGeneral", ex.getMessage());
            return "admin/usuarios/form";
        }
    }

    @PostMapping("/admin/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.desactivar(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Usuario eliminado correctamente");
        redirectAttributes.addFlashAttribute("toastTipo", "success");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/usuarios/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Detalle de Usuario");
        model.addAttribute("pageDescription", "Vista consolidada del usuario.");
        model.addAttribute("usuario", usuarioService.obtenerPorId(id));
        return "admin/usuarios/detail";
    }

    private void cargarFormulario(Model model, UsuarioFormDTO dto, String titulo, String action, boolean modoEdicion) {
        model.addAttribute("pageTitle", titulo);
        model.addAttribute("pageDescription", "Formulario de gestión de usuarios.");
        model.addAttribute("usuarioForm", dto);
        model.addAttribute("roles", rolService.listarActivos());
        model.addAttribute("formAction", action);
        model.addAttribute("modoEdicion", modoEdicion);
    }
}
