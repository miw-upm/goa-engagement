package es.upm.api.domain.services;

import es.upm.api.domain.model.HojaEncargo;
import es.upm.api.domain.persistence.HojaEncargoPersistence;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HojaEncargoService {

    private final HojaEncargoPersistence hojaEncargoPersistence;
    private final UserWebClient userWebClient;

    @Autowired
    public HojaEncargoService(HojaEncargoPersistence hojaEncargoPersistence,
                              UserWebClient userWebClient) {
        this.hojaEncargoPersistence = hojaEncargoPersistence;
        this.userWebClient = userWebClient;
    }

    public HojaEncargo readById(UUID id) {
        HojaEncargo hojaEncargo = this.hojaEncargoPersistence.readById(id);
        hojaEncargo.setPropietario(
                this.userWebClient.readUserById(hojaEncargo.getPropietario().getId())
        );
        hojaEncargo.getAdjuntos().replaceAll(usuario ->
                this.userWebClient.readUserById(usuario.getId())
        );
        return hojaEncargo;
    }

}