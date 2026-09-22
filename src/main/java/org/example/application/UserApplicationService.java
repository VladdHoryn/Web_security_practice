package org.example.application;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  UserApplicationService
  @version  1.0.0 
  @since 22.09.2026 - 13.55
*/
@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Отримати всіх користувачів
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Знайти користувача за ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Користувача з ID " + id + " не знайдено"));
    }

    /**
     * Знайти користувача за email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Користувача з email " + email + " не знайдено"));
    }

    /**
     * Додати нового користувача
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Користувач з email " + user.getEmail() + " вже існує");
        }
        return userRepository.save(user);
    }

    /**
     * Оновити дані користувача (ім'я та/або email)
     */
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email " + updatedUser.getEmail() + " вже використовується іншим користувачем");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());

        return userRepository.save(existingUser);
    }

    /**
     * Видалити користувача за ID
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Неможливо видалити: користувача з ID " + id + " не знайдено");
        }
        userRepository.deleteById(id);
    }
}