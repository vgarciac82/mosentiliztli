package com.tlamatinisoft.mosentiliztli.service;

import com.opencsv.CSVReader;
import com.tlamatinisoft.mosentiliztli.model.Guest;
import com.tlamatinisoft.mosentiliztli.model.GuestStatus;
import com.tlamatinisoft.mosentiliztli.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GuestImportService {

    private final GuestRepository guestRepository;
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    public GuestImportService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional
    public List<Guest> importGuestsFromCsv(MultipartFile file) throws Exception {
        List<Guest> guests = new ArrayList<>();
        
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {
             
            String[] line;
            boolean isFirstLine = true;
            
            while ((line = csvReader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Saltar cabecera
                }
                
                if (line.length < 6) {
                    continue; // Saltar lineas incompletas
                }
                
                String familia = line[0] != null ? line[0].trim() : "";
                String pasesStr = line[1] != null ? line[1].trim() : "";
                String celular = line[2] != null ? line[2].trim() : "";
                String nombreInvitado = line[3] != null ? line[3].trim() : "";
                String grupo = line[4] != null ? line[4].trim() : "";
                String notas = line[5] != null ? line[5].trim() : "";
                
                int pasesAsignados = 1;
                try {
                    if (!pasesStr.isEmpty()) {
                        pasesAsignados = Integer.parseInt(pasesStr);
                    }
                } catch (NumberFormatException e) {
                    // Predeterminado a 1 si falla
                }
                
                Guest guest = new Guest();
                guest.setFamilia(familia);
                guest.setPasesAsignados(pasesAsignados);
                guest.setCelular(celular);
                guest.setNombreInvitado(nombreInvitado);
                guest.setGrupo(grupo);
                guest.setNotas(notas);
                guest.setEstatus(GuestStatus.PENDIENTE);
                
                guest.setToken(UUID.randomUUID().toString());
                guest.setCodigoInvitacion(generateUniqueCode());
                
                guests.add(guest);
            }
        }
        
        return guestRepository.saveAll(guests);
    }
    
    private String generateUniqueCode() {
        String code;
        boolean exists = true;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            code = sb.toString();
            exists = guestRepository.findByCodigoInvitacion(code).isPresent();
        } while (exists);
        
        return code;
    }
}
