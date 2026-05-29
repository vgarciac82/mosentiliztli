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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            Set<String> processedPhones = new HashSet<>();
            
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
                String celularRaw = line[2] != null ? line[2].trim() : "";
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
                
                // Normalizar celular
                String celular = celularRaw.replaceAll("[^0-9+]", "");
                if (celular.length() == 10 && !celular.startsWith("+")) {
                    celular = "+52" + celular;
                } else if (celular.startsWith("52") && celular.length() == 12) {
                    celular = "+" + celular;
                } else if (!celular.startsWith("+") && !celular.isEmpty()) {
                    celular = "+" + celular;
                }
                
                // Idempotencia: Saltar si ya existe en la BD o en este mismo archivo
                if (celular.isEmpty() || processedPhones.contains(celular) || guestRepository.findByCelular(celular).isPresent()) {
                    continue;
                }
                processedPhones.add(celular);
                
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

    @Transactional
    public Guest createSingleGuest(String nombreInvitado, String celularRaw, int pasesAsignados, String familia, String grupo) {
        // Normalizar celular
        String celular = celularRaw != null ? celularRaw.replaceAll("[^0-9+]", "") : "";
        if (celular.length() == 10 && !celular.startsWith("+")) {
            celular = "+52" + celular;
        } else if (celular.startsWith("52") && celular.length() == 12) {
            celular = "+" + celular;
        } else if (!celular.startsWith("+") && !celular.isEmpty()) {
            celular = "+" + celular;
        }

        if (celular.isEmpty() || guestRepository.findByCelular(celular).isPresent()) {
            throw new IllegalArgumentException("Celular inválido o ya registrado.");
        }

        Guest guest = new Guest();
        guest.setNombreInvitado(nombreInvitado != null ? nombreInvitado.trim() : "");
        guest.setCelular(celular);
        guest.setPasesAsignados(pasesAsignados);
        guest.setFamilia(familia != null ? familia.trim() : "");
        guest.setGrupo(grupo != null ? grupo.trim() : "");
        guest.setEstatus(GuestStatus.PENDIENTE);
        
        guest.setToken(UUID.randomUUID().toString());
        guest.setCodigoInvitacion(generateUniqueCode());

        return guestRepository.save(guest);
    }
}
