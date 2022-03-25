package com.exchangeClient.wp.service;

import com.exchangeClient.wp.domain.User;
import com.exchangeClient.wp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(final String name) {
        userRepository.save(new User(name));
    }

    public Optional<User> findOneByName(final String name) {
        List<User> found = userRepository.findByName(name);
        if (found.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(found.get(0));
        }
    }

    public int updateBalance(String name, int balance) {
        List<User> users = userRepository.findByName(name);
        users.forEach(user -> user.changeBalance(balance));
        userRepository.saveAll(users);
        return users.size();
    }

    public void clear() {
        userRepository.deleteAll();
    }

    public void updateStockCount(String username, String stockName, int count) {
        List<User> users = userRepository.findByName(username);
        users.forEach(user -> user.changeStockCount(stockName, count));
        userRepository.saveAll(users);
    }
}
