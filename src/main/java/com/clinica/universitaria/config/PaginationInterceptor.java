package com.clinica.universitaria.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class PaginationInterceptor implements HandlerInterceptor {

    private static final int PAGE_SIZE = 10;

    private static final Map<String, String> PAGINATED_VIEWS = new LinkedHashMap<>();

    static {
        PAGINATED_VIEWS.put("admin/usuarios/index", "usuarios");
        PAGINATED_VIEWS.put("admin/pacientes/index", "pacientes");
        PAGINATED_VIEWS.put("admin/doctores/index", "doctores");
        PAGINATED_VIEWS.put("admin/practicantes/index", "practicantes");
        PAGINATED_VIEWS.put("admin/especialidades/index", "especialidades");
        PAGINATED_VIEWS.put("admin/consultorios/index", "consultorios");
        PAGINATED_VIEWS.put("admin/horarios/index", "horarios");
        PAGINATED_VIEWS.put("admin/citas/index", "citas");
        PAGINATED_VIEWS.put("admin/atenciones/index", "atenciones");
        PAGINATED_VIEWS.put("admin/evaluaciones/index", "evaluaciones");
        PAGINATED_VIEWS.put("doctor/mis-citas", "citas");
        PAGINATED_VIEWS.put("doctor/atenciones", "atenciones");
        PAGINATED_VIEWS.put("doctor/mis-pacientes", "pacientes");
        PAGINATED_VIEWS.put("doctor/practicantes-asignados", "practicantes");
        PAGINATED_VIEWS.put("doctor/evaluar-practicante", "evaluaciones");
        PAGINATED_VIEWS.put("paciente/mis-citas", "citas");
        PAGINATED_VIEWS.put("paciente/especialidades", "especialidades");
        PAGINATED_VIEWS.put("practicante/atenciones-participadas", "atenciones");
        PAGINATED_VIEWS.put("practicante/mis-evaluaciones", "evaluaciones");
        PAGINATED_VIEWS.put("practicante/mis-asignaciones", "asignaciones");
        PAGINATED_VIEWS.put("public/especialidades", "especialidades");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        if (modelAndView == null || modelAndView.getViewName() == null
                || !"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String collectionName = PAGINATED_VIEWS.get(modelAndView.getViewName());
        Object value = collectionName == null ? null : modelAndView.getModel().get(collectionName);
        if (!(value instanceof List<?> items)) {
            return;
        }

        int totalRecords = items.size();
        int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        int requestedPage = parsePage(request.getParameter("page"));
        int currentPage = totalPages == 0 ? 0 : Math.min(requestedPage, totalPages - 1);
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalRecords);

        modelAndView.addObject(collectionName, new ArrayList<>(items.subList(fromIndex, toIndex)));
        modelAndView.addObject("paginaActual", currentPage);
        modelAndView.addObject("totalPaginas", totalPages);
        modelAndView.addObject("totalRegistros", totalRecords);
        modelAndView.addObject("registroDesde", totalRecords == 0 ? 0 : fromIndex + 1);
        modelAndView.addObject("registroHasta", toIndex);
        modelAndView.addObject("paginasVisibles", visiblePages(currentPage, totalPages));
        modelAndView.addObject("paginacionBaseUrl", buildBaseUrl(request));
    }

    private int parsePage(String page) {
        try {
            return Math.max(0, Integer.parseInt(page));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private List<Integer> visiblePages(int currentPage, int totalPages) {
        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages, currentPage + 3);
        return IntStream.range(start, end).boxed().toList();
    }

    private String buildBaseUrl(HttpServletRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        request.getParameterMap().forEach((name, values) -> {
            if (!"page".equals(name)) {
                for (String value : values) {
                    builder.queryParam(name, value);
                }
            }
        });
        String url = builder.build().encode().toUriString();
        return url + (url.contains("?") ? "&" : "?");
    }
}
