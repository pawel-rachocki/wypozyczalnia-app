package com.wypozyczalnia.car_rental_backend.service;
import com.wypozyczalnia.car_rental_backend.model.entity.Client;
import com.wypozyczalnia.car_rental_backend.model.exception.ClientNotFoundException;
import com.wypozyczalnia.car_rental_backend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional
    public Client save(Client client) {
        validateClient(client);

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new IllegalArgumentException(
                    String.format("Client with email %s already exists", client.getEmail()));
        }

        client.setFirstName(normalizeText(client.getFirstName()));
        client.setLastName(normalizeText(client.getLastName()));
        client.setEmail(client.getEmail().toLowerCase().trim());

        Client saved = clientRepository.save(client);

        return saved;
    }

    @Transactional
    public Client update(Long id, Client clientUpdate) {

        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        validateClient(clientUpdate);

        if (!existing.getEmail().equalsIgnoreCase(clientUpdate.getEmail())) {
            if (clientRepository.existsByEmail(clientUpdate.getEmail())) {
                throw new IllegalArgumentException(
                        String.format("Client with email %s already exists", clientUpdate.getEmail()));
            }
        }

        existing.setFirstName(normalizeText(clientUpdate.getFirstName()));
        existing.setLastName(normalizeText(clientUpdate.getLastName()));
        existing.setEmail(clientUpdate.getEmail().toLowerCase().trim());

        Client updated = clientRepository.save(existing);

        return updated;
    }

    @Transactional
    public void delete(Long id) {
        if (clientRepository.hasActiveRentals(id)) {
            throw new IllegalStateException("Cannot delete client with active rentals");
        }

        clientRepository.deleteById(id);
    }

    private void validateClient(Client client) {

        if (client.getFirstName() == null || client.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (client.getFirstName().trim().length() < 2) {
            throw new IllegalArgumentException("First name must be at least 2 characters long");
        }

        if (client.getFirstName().trim().length() > 50) {
            throw new IllegalArgumentException("First name cannot be longer than 50 characters");
        }

        if (client.getLastName() == null || client.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (client.getLastName().trim().length() < 2) {
            throw new IllegalArgumentException("Last name must be at least 2 characters long");
        }

        if (client.getLastName().trim().length() > 50) {
            throw new IllegalArgumentException("Last name cannot be longer than 50 characters");
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!EMAIL_PATTERN.matcher(client.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Email has invalid format");
        }

        if (client.getEmail().trim().length() > 255) {
            throw new IllegalArgumentException("Email cannot be longer than 255 characters");
        }

        if (containsInvalidCharacters(client.getFirstName()) ||
                containsInvalidCharacters(client.getLastName())) {
            throw new IllegalArgumentException("First name and last name can only contain letters, spaces, hyphens and apostrophes");
        }
    }

    private String normalizeText(String text) {
        if (text == null) return null;

        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return normalized;

        return normalized.substring(0, 1).toUpperCase() +
                normalized.substring(1).toLowerCase();
    }

    private boolean containsInvalidCharacters(String text) {
        if (text == null) return false;

        return !text.matches("[\\p{L}\\s\\-']+");
    }
}

