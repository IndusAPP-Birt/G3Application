package eus.birtlh.g3.config;

import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.dto.CreateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.PreventiveGenerationService;
import eus.birtlh.g3.preventive.PreventivePlanAssignmentService;
import eus.birtlh.g3.preventive.PreventivePlanService;
import eus.birtlh.g3.preventive.PreventiveService;
import eus.birtlh.g3.preventive.dto.assignment.CreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.execution.AssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.plan.CreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.plan.PreventivePlanResponse;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserService;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class SecurityAuthorizationTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
    private static final LocalDate NEXT_DUE_DATE = TODAY.plusDays(5);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private MachineService machineService;

    @Autowired
    private PreventivePlanService preventivePlanService;

    @Autowired
    private PreventivePlanAssignmentService preventivePlanAssignmentService;

    @Autowired
    private PreventiveGenerationService preventiveGenerationService;

    @Autowired
    private PreventiveRepository preventiveRepository;

    @Autowired
    private PreventiveService preventiveService;

    /**
     * Comprueba que la bandeja personal de preventivos no se expone a una petición sin JWT.
     */
    @DisplayName("GET /api/preventives/me sin token devuelve 401")
    @Test
    void rechazaConsultaDePreventivosDelInspectorSinToken() throws Exception {

        // Consultamos la bandeja personal sin aportar una autenticación
        mockMvc.perform(get("/api/preventives/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Comprueba que solo el rol de inspector puede acceder a su bandeja personal de preventivos.
     *
     * @param role rol que no está autorizado a consultar esta ruta
     */
    @DisplayName("GET /api/preventives/me rechaza roles distintos de inspector")
    @ParameterizedTest(name = "[{index}] El rol {0} recibe 403")
    @EnumSource(value = Role.class, names = {"ROLE_ADMINISTRATOR", "ROLE_MAINTENANCE_TECHNICIAN"})
    void rechazaConsultaDePreventivosDelInspectorConRolNoAutorizado(Role role) throws Exception {

        // Consultamos la bandeja personal con un rol distinto de inspector
        mockMvc.perform(get("/api/preventives/me")
                        .with(authenticatedAs(role, "unauthorized@test.local")))
                .andExpect(status().isForbidden());
    }

    /**
     * Comprueba que un inspector autenticado puede consultar su propia bandeja de preventivos.
     */
    @DisplayName("GET /api/preventives/me permite al inspector autenticado")
    @Test
    void permiteConsultaDePreventivosDelInspectorAutenticado() throws Exception {

        // Creamos el inspector identificado por el sujeto del JWT de prueba
        UserResponse inspector = createInspector("inspector.inbox@test.local", "Bandeja");

        // Consultamos su bandeja personal con el rol de inspector
        mockMvc.perform(get("/api/preventives/me")
                        .with(authenticatedAs(Role.ROLE_INSPECTOR, inspector.email())))
                .andExpect(status().isOk());
    }

    /**
     * Comprueba que el inicio de un preventivo exige una autenticación JWT.
     */
    @DisplayName("PUT /api/preventives/{id}/start sin token devuelve 401")
    @Test
    void rechazaInicioDePreventivoSinToken() throws Exception {

        // Intentamos iniciar un preventivo sin aportar una autenticación
        mockMvc.perform(put("/api/preventives/{id}/start", 1L))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Comprueba que el inicio de un preventivo está reservado al rol de inspector.
     *
     * @param role rol que no está autorizado a iniciar preventivos
     */
    @DisplayName("PUT /api/preventives/{id}/start rechaza roles distintos de inspector")
    @ParameterizedTest(name = "[{index}] El rol {0} recibe 403")
    @EnumSource(value = Role.class, names = {"ROLE_ADMINISTRATOR", "ROLE_MAINTENANCE_TECHNICIAN"})
    void rechazaInicioDePreventivoConRolNoAutorizado(Role role) throws Exception {

        // Intentamos iniciar un preventivo con un rol distinto de inspector
        mockMvc.perform(put("/api/preventives/{id}/start", 1L)
                        .with(authenticatedAs(role, "unauthorized@test.local")))
                .andExpect(status().isForbidden());
    }

    /**
     * Comprueba que un inspector autenticado puede iniciar el preventivo que tiene asignado.
     */
    @DisplayName("PUT /api/preventives/{id}/start permite al inspector asignado")
    @Test
    void permiteInicioDePreventivoAlInspectorAsignado() throws Exception {

        // Preparamos un preventivo ASSIGNED para el inspector autenticado
        UserResponse inspector = createInspector("inspector.start-http@test.local", "Inicio HTTP");
        long preventiveId = createPreventiveAssignedTo(inspector);

        // El inspector inicia su preventivo mediante la ruta protegida
        mockMvc.perform(put("/api/preventives/{id}/start", preventiveId)
                        .with(authenticatedAs(Role.ROLE_INSPECTOR, inspector.email())))
                .andExpect(status().isOk());
    }

    /**
     * Comprueba que crear una asignación preventiva exige una autenticación JWT.
     */
    @DisplayName("POST /api/preventive-plan-assignments sin token devuelve 401")
    @Test
    void rechazaCreacionDeAsignacionPreventivaSinToken() throws Exception {

        // Intentamos crear una asignación sin aportar una autenticación
        mockMvc.perform(post("/api/preventive-plan-assignments"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Comprueba que crear asignaciones preventivas está reservado al rol de administrador.
     *
     * @param role rol que no está autorizado a crear asignaciones
     */
    @DisplayName("POST /api/preventive-plan-assignments rechaza roles no administradores")
    @ParameterizedTest(name = "[{index}] El rol {0} recibe 403")
    @EnumSource(value = Role.class, names = {"ROLE_INSPECTOR", "ROLE_MAINTENANCE_TECHNICIAN"})
    void rechazaCreacionDeAsignacionPreventivaConRolNoAutorizado(Role role) throws Exception {

        // Intentamos crear una asignación con un rol distinto de administrador
        mockMvc.perform(post("/api/preventive-plan-assignments")
                        .with(authenticatedAs(role, "unauthorized@test.local")))
                .andExpect(status().isForbidden());
    }

    /**
     * Comprueba que un administrador puede crear una asignación con una plantilla y una máquina
     * activas.
     */
    @DisplayName("POST /api/preventive-plan-assignments permite al administrador")
    @Test
    void permiteCreacionDeAsignacionPreventivaAlAdministrador() throws Exception {

        // Creamos una máquina y una plantilla activas para la asignación solicitada por HTTP
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-HTTP-ASSIGNMENT-001",
                        "Máquina de prueba HTTP",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-HTTP-ASSIGNMENT-001",
                        "Laboratorio",
                        TODAY
                )
        );
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba HTTP",
                        "Instrucciones para probar una asignación por HTTP."
                )
        );

        // El administrador crea la asignación mediante la ruta protegida
        mockMvc.perform(post("/api/preventive-plan-assignments")
                        .with(authenticatedAs(Role.ROLE_ADMINISTRATOR, "administrator@test.local"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignmentRequestBody(preventivePlan.id(), machine.id())))
                .andExpect(status().isCreated());
    }

    /**
     * Comprueba que el listado administrativo de preventivos exige una autenticación JWT.
     */
    @DisplayName("GET /api/preventives sin token devuelve 401")
    @Test
    void rechazaListadoDePreventivosSinToken() throws Exception {

        // Consultamos el listado administrativo sin aportar una autenticación
        mockMvc.perform(get("/api/preventives"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Comprueba que el listado administrativo de preventivos rechaza roles no administradores.
     *
     * @param role rol que no está autorizado a consultar el listado
     */
    @DisplayName("GET /api/preventives rechaza roles no administradores")
    @ParameterizedTest(name = "[{index}] El rol {0} recibe 403")
    @EnumSource(value = Role.class, names = {"ROLE_INSPECTOR", "ROLE_MAINTENANCE_TECHNICIAN"})
    void rechazaListadoDePreventivosConRolNoAutorizado(Role role) throws Exception {

        // Consultamos el listado administrativo con un rol distinto de administrador
        mockMvc.perform(get("/api/preventives")
                        .with(authenticatedAs(role, "unauthorized@test.local")))
                .andExpect(status().isForbidden());
    }

    /**
     * Comprueba que un administrador autenticado puede consultar el listado de preventivos.
     */
    @DisplayName("GET /api/preventives permite al administrador")
    @Test
    void permiteListadoDePreventivosAlAdministrador() throws Exception {

        // El administrador consulta el listado mediante la ruta protegida
        mockMvc.perform(get("/api/preventives")
                        .with(authenticatedAs(Role.ROLE_ADMINISTRATOR, "administrator@test.local")))
                .andExpect(status().isOk());
    }

    /**
     * Crea un inspector habilitado para usarlo como sujeto de un JWT de prueba.
     *
     * @param email correo que se utilizará como sujeto del JWT
     * @param lastName apellidos que distinguen el dato de prueba
     * @return inspector persistido
     */
    private UserResponse createInspector(String email, String lastName) {
        return userService.createUser(
                new CreateUserRequest(
                        "Inspector",
                        lastName,
                        email,
                        "Password123!",
                        Role.ROLE_INSPECTOR
                )
        );
    }

    /**
     * Prepara un preventivo en estado ASSIGNED para el inspector indicado.
     *
     * @param inspector inspector al que se asignará el preventivo generado
     * @return identificador del preventivo asignado
     */
    private long createPreventiveAssignedTo(UserResponse inspector) {
        MachineResponse machine = machineService.createMachine(
                new CreateMachineRequest(
                        "TEST-HTTP-START-001",
                        "Máquina de prueba de inicio HTTP",
                        "Fabricante de prueba",
                        "Modelo de prueba",
                        "SN-TEST-HTTP-START-001",
                        "Laboratorio",
                        TODAY
                )
        );
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(
                new CreatePreventivePlanRequest(
                        "Plan de prueba de inicio HTTP",
                        "Instrucciones para probar el inicio de un preventivo por HTTP."
                )
        );
        preventivePlanAssignmentService.createPreventivePlanAssignment(
                new CreatePreventivePlanAssignmentRequest(
                        preventivePlan.id(),
                        machine.id(),
                        30,
                        7,
                        NEXT_DUE_DATE
                )
        );
        preventiveGenerationService.generatePendingPreventives(TODAY);

        Preventive preventive = preventiveRepository
                .findByPreventivePlanAssignmentPreventivePlanId(
                        preventivePlan.id(),
                        PageRequest.of(0, 1)
                )
                .getContent()
                .getFirst();

        preventiveService.assignInspectorToPreventive(
                preventive.getId(),
                new AssignInspectorToPreventiveRequest(inspector.id())
        );

        return preventive.getId();
    }

    /**
     * Construye un JWT simulado con el sujeto y la autoridad requeridos por la ruta que se prueba.
     *
     * @param role rol que se expondrá como autoridad de Spring Security
     * @param subject correo que se expondrá como sujeto del JWT
     * @return modificador de petición autenticada para {@link MockMvc}
     */
    private RequestPostProcessor authenticatedAs(Role role, String subject) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(subject).claim("role", role.name()))
                .authorities(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Construye el cuerpo JSON válido para crear una asignación preventiva.
     *
     * @param preventivePlanId identificador de la plantilla activa
     * @param machineId identificador de la máquina activa
     * @return JSON que representa una solicitud de creación válida
     */
    private String assignmentRequestBody(long preventivePlanId, long machineId) {
        return """
                {
                  "preventivePlanId": %d,
                  "machineId": %d,
                  "intervalDays": 30,
                  "noticeDays": 7,
                  "nextDueDate": "%s"
                }
                """.formatted(preventivePlanId, machineId, NEXT_DUE_DATE);
    }
}
