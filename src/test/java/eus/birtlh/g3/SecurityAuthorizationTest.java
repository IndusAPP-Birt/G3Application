package eus.birtlh.g3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rechazaConsultaDePreventivosDelInspectorSinToken() throws Exception {
        mockMvc.perform(get("/api/preventives/me"))
                .andExpect(status().isUnauthorized());
    }
}
